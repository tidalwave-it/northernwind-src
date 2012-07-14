/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.hg;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.net.URI;
import java.net.URISyntaxException;
import org.joda.time.DateTime;
import org.openide.filesystems.LocalFileSystem;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.northernwind.core.filesystem.FileSystemChangedEvent;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.DefaultMercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.MercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public class MercurialFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter
    private String remoteRepositoryUrl;
    
    @Getter @Setter
    private String workAreaFolder;
    
    @Getter
    private final LocalFileSystem fileSystem = new LocalFileSystem();
   
    @Inject
    private BeanFactory beanFactory;
    
//    @Inject @Named("applicationMessageBus") FIXME doesn't work in the test
    private MessageBus messageBus;
    
    private Path workArea;
        
    private final MercurialRepository[] repositories = new MercurialRepository[2];
    
    private MercurialRepository exposedRepository;
    
    private MercurialRepository alternateRepository;
    
    private int repositorySelector;
    
    /* package */ int swapCounter;
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void initialize() 
      throws IOException, PropertyVetoException, URISyntaxException
      {
        workArea = new File(workAreaFolder).toPath();
        
        for (int i = 0; i < 2; i++)
          {
            repositories[i] = new DefaultMercurialRepository(workArea.resolve("" + (i + 1)));  
            
            if (repositories[i].isEmpty())
              {
                // FIXME: this is inefficient, clones both from the remote repo
                repositories[i].clone(new URI(remoteRepositoryUrl));  
              }
          }
        
        messageBus = beanFactory.getBean("applicationMessageBus", MessageBus.class);
        
        swapRepositories(); // initialization
        swapCounter = 0;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public void checkForUpdates()
      {
        try
          {
            final Tag newTag = findNewTag();
            log.info(">>>> new tag seen: {}", newTag);
            alternateRepository.updateTo(newTag);
            swapRepositories();
            messageBus.publish(new FileSystemChangedEvent(this, new DateTime()));
            alternateRepository.pull();
            alternateRepository.updateTo(newTag);
          }
        catch (NotFoundException e)
          {
            log.info(">>>> no changes");
          }
        catch (Exception e)
          {
            log.warn(">>>> error when checking for updates", e);
          }
      }        
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    /* package */ Tag getCurrentTag() 
      throws IOException, NotFoundException
      {
        return exposedRepository.getCurrentTag();  
      }
    
    @Nonnull
    /* package */ Path getCurrentWorkArea() 
      {
        return exposedRepository.getWorkArea();  
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private void swapRepositories()
      throws IOException, PropertyVetoException
      {
        exposedRepository = repositories[repositorySelector];  
        alternateRepository = repositories[(repositorySelector + 1) % 2]; 
        repositorySelector = (repositorySelector + 1) % 2;
        fileSystem.setRootDirectory(exposedRepository.getWorkArea().toFile());
        swapCounter++;
        
        log.info("New exposed repository:   {}", exposedRepository.getWorkArea());
        log.info("New alternate repository: {}", alternateRepository.getWorkArea());
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private Tag findNewTag() 
      throws NotFoundException, IOException
      {
        log.info("Checking for updates...");
        
        alternateRepository.pull();
        // would throw NotFoundException if no publishing tag
        final Tag latestTag = getLatestPublishingTag(alternateRepository);
        
        try
          {
            if (!latestTag.equals(exposedRepository.getCurrentTag()))
              {
                return latestTag;
              } 
          }
        catch (NotFoundException e) 
          {
            log.info(">>>> repo must be initialized");
            return latestTag;
          }

        throw new NotFoundException();  
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Tag getLatestPublishingTag (final @Nonnull MercurialRepository repository)
      throws IOException, NotFoundException            
      {
        final List<Tag> tags = repository.getTags();
        Collections.reverse(tags);
        
        for (final Tag tag : tags)
          {
            if (tag.getName().startsWith("published-"))
              {
                return tag;
              } 
          }
        
        throw new NotFoundException();
      }
  }
