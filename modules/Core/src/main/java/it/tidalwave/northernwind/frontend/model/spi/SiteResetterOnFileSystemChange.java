/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.eventbus.EventBus;
import it.tidalwave.eventbus.EventBusListener;
import it.tidalwave.northernwind.frontend.filesystem.FileSystemChangedEvent;
import it.tidalwave.northernwind.frontend.model.Site;
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
    
    @Inject @Named("applicationEventBus") @Nonnull
    private EventBus eventBus;
    
    private final EventBusListener<FileSystemChangedEvent> listener = new EventBusListener<FileSystemChangedEvent>() 
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
    private void subscribe()
      {
        eventBus.subscribe(FileSystemChangedEvent.class, listener);            
      }
  }
