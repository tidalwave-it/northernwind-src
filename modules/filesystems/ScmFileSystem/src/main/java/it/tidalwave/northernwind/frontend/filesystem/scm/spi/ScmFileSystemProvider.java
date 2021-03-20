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
 * A {@code ResourceFileSystemProvider} based on a SCM. This is an abstract support class that needs to be extended
 * by concrete implementations (such as Git or Mercurial).
 * This provider polls for changes in the SCM that are made available with a new tag named
 * {@code published-<version>} and fetches them. In order to atomically expose changes, in spite of the fact that the
 * underlying operation might require some time to update all files, two working directories are used:
 *
 * <ol>
 *     <li>the {@code exposedWorkingDirectory} is exposed and not affected by next change;</li>
 *     <li>the {@code alternateWorkingDirectory} is kept behind the scenes and updated; when its update is completed, the
 *     two repositories are swapped and a {@link ResourceFileSystemChangedEvent} is fired.</li>
 * </ol>
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public abstract class ScmFileSystemProvider implements ResourceFileSystemProvider
  {
    /** The URL of the remote repository. */
    @Getter @Setter
    private String remoteRepositoryUrl;

    /** The folder of the local work area. */
    @Getter @Setter
    private String folderAsString;

    /** The path of the working directory. */
    private Path folder;

    /** The file system used to map the local work area. */
    /* visible for tests */ final LocalFileSystem fileSystemDelegate = new LocalFileSystem();

    @Getter
    private final ResourceFileSystem fileSystem = new ResourceFileSystemNetBeansPlatform(fileSystemDelegate);

    private final ScmWorkingDirectory[] workingDirectories = new ScmWorkingDirectory[2];

    /** The exposed working directory. */
    /* visible for tests */ ScmWorkingDirectory exposedWorkingDirectory;

    /** The alternate working directory. */
    /* visible for tests */ ScmWorkingDirectory alternateWorkingDirectory;

    /** The index of the exposed repository (0 or 1). */
    private int repositorySelector;

    /** A counter of swaps, used for testing. */
    /* visible for tests */int swapCounter;

    @Inject
    private BeanFactory beanFactory;

    /** The message bus where to fire events. */
    //    @Inject @Named("applicationMessageBus") FIXME doesn't work in the test
    private MessageBus messageBus;

    /*******************************************************************************************************************
     *
     * Makes sure both repository repositories are populated and activates one of them.
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void initialize()
            throws IOException, PropertyVetoException, URISyntaxException, InterruptedException
      {
        folder = new File(folderAsString).toPath();

        for (int i = 0; i < 2; i++)
          {
            workingDirectories[i] = createWorkingDirectory(folder.resolve("" + (i + 1)));

            if (workingDirectories[i].isEmpty())
              {
                workingDirectories[i].cloneFrom(new URI(remoteRepositoryUrl));
              }
          }

        messageBus = beanFactory.getBean("applicationMessageBus", MessageBus.class); // FIXME

        swapRepositories(); // initialization
        swapCounter = 0;
      }

    /*******************************************************************************************************************
     *
     * Checks whether there are incoming changes. See the class' documentation for more information.
     *
     ******************************************************************************************************************/
    public void checkForUpdates()
      {
        try
          {
            final Optional<Tag> newTag = fetchChangesetsAndSearchForNewTag();

            if (!newTag.isPresent())
              {
                log.info(">>>> no changes");
              }
            else
              {
                final Tag tag = newTag.get();
                log.info(">>>> new tag: {}", tag);
                alternateWorkingDirectory.checkOut(tag);
                swapRepositories();
                messageBus.publish(new ResourceFileSystemChangedEvent(this, ZonedDateTime.now()));
                alternateWorkingDirectory.fetchChangesets();
                alternateWorkingDirectory.checkOut(tag);
              }
          }
        catch (ProcessExecutorException e)
          {
            log.warn(">>>> error when checking for updates in {}: exit code is {}",
                     alternateWorkingDirectory.getFolder(),
                     e.getExitCode());
            e.getStdout().forEach(s -> log.warn(">>>> STDOUT: {}", s));
            e.getStderr().forEach(s -> log.warn(">>>> STDERR: {}", s));
          }
        catch (Exception e)
          {
            log.warn(">>>> error when checking for updates in " + alternateWorkingDirectory.getFolder(), e);
          }
      }

    /*******************************************************************************************************************
     *
     * Creates a new {@link ScmWorkingDirectory} at the given path.
     *
     * @param path the path of the repository.
     * @return a {@code ScmWorkingDirectory}
     *
     ******************************************************************************************************************/
    @Nonnull
    abstract public ScmWorkingDirectory createWorkingDirectory (@Nonnull Path path);

    /*******************************************************************************************************************
     *
     * Swaps the repositories.
     *
     * @throws IOException              in case of error
     * @throws PropertyVetoException    in case of error
     *
     ******************************************************************************************************************/
    private void swapRepositories()
            throws IOException, PropertyVetoException
      {
        exposedWorkingDirectory = workingDirectories[repositorySelector];
        repositorySelector = (repositorySelector + 1) % 2;
        alternateWorkingDirectory = workingDirectories[repositorySelector];
        fileSystemDelegate.setRootDirectory(exposedWorkingDirectory.getFolder().toFile());
        swapCounter++;

        log.info("New exposed working directory:   {}", exposedWorkingDirectory.getFolder());
        log.info("New alternate working directory: {}", alternateWorkingDirectory.getFolder());
      }

    /*******************************************************************************************************************
     *
     * Fetches changesets from the repository and searches for a new tag.
     *
     * @return the new tag
     * @throws IOException              in case of error
     * @throws InterruptedException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<Tag> fetchChangesetsAndSearchForNewTag()
            throws IOException, InterruptedException
      {
        log.info("Checking for updates in {} ...", alternateWorkingDirectory.getFolder());

        alternateWorkingDirectory.fetchChangesets();
        final Optional<Tag> latestTag = alternateWorkingDirectory.getLatestTagMatching("^published-.*");
        final Optional<Tag> currentTag = exposedWorkingDirectory.getCurrentTag();

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
