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
import it.tidalwave.messagebus.MessageBus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for a local {@link FileSystem}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString(of={"zipFilePath", "latestModified", "changeWasDetected"})
public class ZipFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    private String zipFilePath = "";
    
    @Getter @Setter
    private long modificationCheckInterval = 5000;
    
    @CheckForNull
    private JarFileSystem fileSystem;
    
    private DateTime latestModified;
    
    @Inject @Named("applicationMessageBus")
    private MessageBus messageBus;
    
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
            try
              {
                final File zipFile = getFileSystem().getJarFile();
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
                        messageBus.publish(new FileSystemChangedEvent(ZipFileSystemProvider.this, latestModified));
                      }
                  }
              }
            catch (IOException e)
              {
                log.error("Cannot check changes on zip file system", e); 
              }
          }
      };
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized JarFileSystem getFileSystem() 
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