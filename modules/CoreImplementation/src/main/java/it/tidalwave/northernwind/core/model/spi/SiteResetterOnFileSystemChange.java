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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.messagebus.MessageBus.Listener;
import it.tidalwave.northernwind.core.filesystem.FileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.Site;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A simple utility that forces a reset of the {@link Site} when a change in the underlying file system is detected.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class SiteResetterOnFileSystemChange 
  {
    @Inject @Nonnull
    private Site site;  
    
    @Inject @Named("applicationMessageBus") @Nonnull
    private MessageBus messageBus;
    
    private final Listener<FileSystemChangedEvent> listener = new Listener<FileSystemChangedEvent>() 
      {
        @Override
        public void notify (final @Nonnull FileSystemChangedEvent event) 
          {
            if (event.getFileSystemProvider() == site.getFileSystemProvider())
              {
                try 
                  {
                    log.info("Detected file change, resetting site...");
                    site.reset();
                  }
                catch (IOException e) 
                  {
                    log.warn("While resetting site: ", e);
                  }
                catch (NotFoundException e) 
                  {
                    log.warn("While resetting site: ", e);
                  }
              }
          }
      };
    
    @PostConstruct
    /* package */ void initialize()
      {
        log.info("SiteResetterOnFileSystemChange installed");
        messageBus.subscribe(FileSystemChangedEvent.class, listener);            
      }
  }
