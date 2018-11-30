/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.basic;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Timer;
import java.util.TimerTask;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.FileObject;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.impl.ResourceFileSystemNetBeansPlatform;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for a local {@link NwFileSystem}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @ToString(of = { "zipFilePath", "latestModified", "changeWasDetected" })
public class ZipFileSystemProvider implements ResourceFileSystemProvider
  {
    @Getter @Setter @Nonnull
    private String zipFilePath = "";

    @Getter @Setter
    private long modificationCheckInterval = 5000;

    @CheckForNull
    private ResourceFileSystem fileSystem;

    @CheckForNull
    private JarFileSystem fileSystemDelegate;

    private ZonedDateTime latestModified;

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
                getFileSystem(); // force initialization
                final File zipFile = fileSystemDelegate.getJarFile();
                final ZonedDateTime timestamp = Instant.ofEpochMilli(zipFile.lastModified()).atZone(ZoneId.of("GMT"));
    //            log.debug(">>>> checking zip file latest modification: was {}, is now {}", latestModified, timestamp);

                if (!changeWasDetected)
                  {
                    if (timestamp.isAfter(latestModified))
                      {
                        latestModified = timestamp;
                        changeWasDetected = true;
                        log.info("Detected change of {}: last modified time: {} - waiting for it to become stable",
                                 zipFile, latestModified);
                      }
                  }
                else
                  {
                    if (timestamp.isAfter(latestModified))
                      {
                        latestModified = timestamp;
                        log.info("Detected unstable change of {}: last modified time: {} - waiting for it to become stable",
                                 zipFile, latestModified);
                      }
                    else
                      {
                        latestModified = timestamp;
                        changeWasDetected = false;
                        log.info("Detected stable change of {}: last modified time: {}", zipFile, latestModified);
                        messageBus.publish(new ResourceFileSystemChangedEvent(ZipFileSystemProvider.this,
                                                                              latestModified));
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
    public synchronized ResourceFileSystem getFileSystem()
      throws IOException
      {
        if (fileSystem == null)
          {
            final File zipFile = new File(zipFilePath);
            fileSystemDelegate = new JarFileSystem(zipFile);
            final FileObject rootFolder = fileSystemDelegate.getRoot();

            if (rootFolder == null)
              {
                throw new FileNotFoundException(zipFilePath);
              }

            log.info(">>>> fileSystem: {}", fileSystemDelegate);
            latestModified = Instant.ofEpochMilli(zipFile.lastModified()).atZone(ZoneId.of("GMT"));
            timer.scheduleAtFixedRate(zipFileModificationTracker, modificationCheckInterval, modificationCheckInterval);
            fileSystem = new ResourceFileSystemNetBeansPlatform(fileSystemDelegate);
          }

        return fileSystem;
      }
  }
