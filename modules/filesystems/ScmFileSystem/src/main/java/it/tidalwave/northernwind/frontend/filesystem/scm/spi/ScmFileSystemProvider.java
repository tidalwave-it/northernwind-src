/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import java.beans.PropertyVetoException;
import org.openide.filesystems.LocalFileSystem;
import org.springframework.beans.factory.BeanFactory;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.impl.ResourceFileSystemNetBeansPlatform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The implementation relies upon two alternate repositories to perform atomic changes:
 *
 * <ol>
 *     <li>the <code>exposedRepository</code> is the one whose contents are used for publishing, and it's never touched
 *     </li>
 *     <li>the <code>alternateRepository</code> is kept behind the scenes and it's used for updates</li>
 * </ol>
 *
 * When there are changes in the <code>alternateRepository</code>, the two repositories are swapped.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public abstract class ScmFileSystemProvider implements ResourceFileSystemProvider
  {
    @Getter @Setter
    private String remoteRepositoryUrl;

    @Getter @Setter
    private String workAreaFolder;

    @Getter // FIXME: public for test
    private final LocalFileSystem fileSystemDelegate = new LocalFileSystem();

    @Getter
    private final ResourceFileSystem fileSystem = new ResourceFileSystemNetBeansPlatform(fileSystemDelegate);

    @Inject
    private BeanFactory beanFactory;

//    @Inject @Named("applicationMessageBus") FIXME doesn't work in the test
    private MessageBus messageBus;

    private Path workArea;

    private final ScmRepository[] repositories = new ScmRepository[2];

    @Getter // FIXME: public for test
    private ScmRepository exposedRepository;

    @Getter // FIXME: public for test
    private ScmRepository alternateRepository;

    private int repositorySelector;

    @Getter
    int swapCounter;

    /*******************************************************************************************************************
     *
     * Makes sure both repository repositories are populated and activates one of them.
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void initialize()
      throws IOException, PropertyVetoException, URISyntaxException, InterruptedException
      {
        workArea = new File(workAreaFolder).toPath();

        for (int i = 0; i < 2; i++)
          {
            repositories[i] = createRepository(workArea.resolve("" + (i + 1)));

            if (repositories[i].isEmpty())
              {
                repositories[i].clone(new URI(remoteRepositoryUrl));
              }
          }

        messageBus = beanFactory.getBean("applicationMessageBus", MessageBus.class); // FIXME

        swapRepositories(); // initialization
        swapCounter = 0;
      }

    /*******************************************************************************************************************
     *
     * Checks whether there are incoming changes. Changes are detected when there's a new tag whose name follows the
     * pattern 'published-<version>'. Changes are pulled in the alternate repository, then repositories are swapped, at
     * last the alternateRepository is updated too.
     *
     ******************************************************************************************************************/
    public void checkForUpdates()
      {
        try
          {
            final Optional<Tag> newTag = findNewTag();

            if (!newTag.isPresent())
              {
                log.info(">>>> no changes");
              }
            else
              {
                final Tag t = newTag.get();
                log.info(">>>> new tag: {}", t);
                alternateRepository.updateTo(t);
                swapRepositories();
                messageBus.publish(new ResourceFileSystemChangedEvent(this, ZonedDateTime.now()));
                alternateRepository.pull();
                alternateRepository.updateTo(t);
              }
          }
        catch (Exception e)
          {
            log.warn(">>>> error when checking for updates in " + alternateRepository.getWorkArea(), e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    abstract public ScmRepository createRepository (@Nonnull Path path)
      throws IOException, InterruptedException;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<Tag> getCurrentTag() // FIXME: public for test
      throws IOException, InterruptedException
      {
        return exposedRepository.getCurrentTag();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Path getCurrentWorkArea() // FIXME: public for test
      {
        return exposedRepository.getWorkArea();
      }

    /*******************************************************************************************************************
     *
     * Swaps the repositories.
     *
     * @throws      IOException              in case of error
     * @throws      PropertyVetoException    in case of error
     *
     ******************************************************************************************************************/
    private void swapRepositories()
      throws IOException, PropertyVetoException
      {
        exposedRepository = repositories[repositorySelector];
        repositorySelector = (repositorySelector + 1) % 2;
        alternateRepository = repositories[repositorySelector];
        fileSystemDelegate.setRootDirectory(exposedRepository.getWorkArea().toFile());
        swapCounter++;

        log.info("New exposed repository:   {}", exposedRepository.getWorkArea());
        log.info("New alternate repository: {}", alternateRepository.getWorkArea());
      }

    /*******************************************************************************************************************
     *
     * Finds a new tag.
     *
     * @return                      the new tag
     * @throws      IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<Tag> findNewTag()
      throws IOException, InterruptedException
      {
        log.info("Checking for updates in {} ...", alternateRepository.getWorkArea());

        alternateRepository.pull();
        final Optional<Tag> latestTag = alternateRepository.getLatestTagMatching("^published-.*");
        final Optional<Tag> currentTag = exposedRepository.getCurrentTag();

        if (!latestTag.isPresent())
          {
            return Optional.empty();
          }

        if (!currentTag.isPresent())
          {
            log.info(">>>> repo must be initialized - latest tag: {}", latestTag.map(Tag::getName).orElse("<none>"));
            return latestTag;
          }

        if (!latestTag.equals(currentTag))
          {
            return latestTag;
          }

        return Optional.empty();
      }
  }
