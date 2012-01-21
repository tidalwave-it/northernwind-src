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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.basic;

import it.tidalwave.northernwind.core.filesystem.FileSystemChangedEvent;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.joda.time.DateTime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.JarFileSystem;
import it.tidalwave.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for a local {@link FileSystem}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ZipFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    private String zipFilePath = "";
    
    @Getter @Setter
    private long modificationCheckInterval = 5000;
    
    @CheckForNull
    private JarFileSystem fileSystem;
    
    private DateTime latestModified;
    
    @Inject @Named("applicationEventBus")
    private EventBus eventBus;
    
    private final Timer timer = new Timer("ZipFileSystemProvider.modificationTracker"); 
            
    private boolean changeWasDetected = false;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private final TimerTask zipFileModificationTracker = new TimerTask() 
      {
        @Override
        public void run() 
          {
            final File zipFile = fileSystem.getJarFile();
            final DateTime timestamp = new DateTime(zipFile.lastModified());
//            log.debug(">>>> checking zip file latest modification: was {}, is now {}", latestModified, timestamp);
            
            if (!changeWasDetected)
              {
                if (timestamp.isAfter(latestModified))
                  {
                    latestModified = timestamp;  
                    changeWasDetected = true;
                    log.info("Detected change of {}: last modified time: {} - waiting for it to become stable", zipFile, latestModified);
                  }
              }
            else
              {
                if (timestamp.isAfter(latestModified))
                  {
                    latestModified = timestamp;  
                    log.info("Detected unstable change of {}: last modified time: {} - waiting for it to become stable", zipFile, latestModified);
                  }
                else
                  {
                    latestModified = timestamp;  
                    changeWasDetected = false;
                    log.info("Detected stable change of {}: last modified time: {}", zipFile, latestModified);
                    eventBus.publish(new FileSystemChangedEvent(ZipFileSystemProvider.this, latestModified));
                  }
              }
          }
      };
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized FileSystem getFileSystem() 
      throws IOException
      {
        if (fileSystem == null)
          {
            final File zipFile = new File(zipFilePath);
            fileSystem = new JarFileSystem(zipFile);
            final FileObject rootFolder = fileSystem.getRoot();

            if (rootFolder == null)
              {
                throw new FileNotFoundException(zipFilePath);  
              } 

            log.info(">>>> fileSystem: {}", fileSystem);
            latestModified = new DateTime(zipFile.lastModified());
            timer.scheduleAtFixedRate(zipFileModificationTracker, modificationCheckInterval, modificationCheckInterval);
          }
              
        return fileSystem;  
      }    
  }
