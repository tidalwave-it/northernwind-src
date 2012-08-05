/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Provider;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.messagebus.MessageBus.Listener;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
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
public class SiteResetterOnFileSystemChange // TODO: rename to SiteReloaderOnFileSystemChange
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;  
    
    @Inject @Named("applicationMessageBus") @Nonnull
    private MessageBus messageBus;
    
    private final Listener<ResourceFileSystemChangedEvent> listener = new Listener<ResourceFileSystemChangedEvent>() 
      {
        @Override
        public void notify (final @Nonnull ResourceFileSystemChangedEvent event) 
          {
            // FIXME: the originator of the event could be a child filesystem in case of composite filesystems.
            // Either the event is listened by the parent and reposted, or we must be able to find whether a 
            // given filesystem is the parent of another
            //
            // e.g. if (siteProvider.get().getSite().getFileSystemProvider().contains(event.getFileSystemProvider()))
            //
//            log.info("event fileSystemProvider {}", event.getFileSystemProvider());
//            log.info("site fileSystemProvider {}", siteProvider.get().getSite().getFileSystemProvider());
            
//            if (event.getFileSystemProvider() == siteProvider.get().getSite().getFileSystemProvider())
              {
                log.info("Detected file change, resetting site...");
                siteProvider.get().reload();
              }
          }
      };
    
    @PostConstruct
    /* package */ void initialize() // FIXME: unsubscribe on @PreDestroy
      {
        messageBus.subscribe(ResourceFileSystemChangedEvent.class, listener);            
        log.info("SiteResetterOnFileSystemChange installed");
      }
  }
