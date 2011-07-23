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
package it.tidalwave.northernwind.frontend.ui.vaadin;

import it.tidalwave.northernwind.frontend.vaadin.DownloadStreamThreadLocal;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultSiteViewController;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;

/***********************************************************************************************************************
 *
 * The Vaadin specialization of {@link SiteViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class VaadinSiteViewController extends DefaultSiteViewController
  {
    @Nonnull @Inject
    private VaadinSiteView siteView;
    
    @Nonnull @Inject
    private DownloadStreamThreadLocal downloadStreamHolder;

    /*******************************************************************************************************************
     *
     * Tracks the incoming URI.
     *
     ******************************************************************************************************************/
    private final URIHandler uriHandler = new URIHandler()
      {
        @Override
        public DownloadStream handleURI (final @Nonnull URL context, final @Nonnull String relativeUri) 
          {
            try
              {
                downloadStreamHolder.set(null);
                handleUri(context, relativeUri);
                return downloadStreamHolder.get();
              }
            finally
              {
                downloadStreamHolder.set(null);
              }
          }
      };
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void registerUriHandler()
      {
        siteView.addURIHandler(uriHandler);
        // FIXME: seems to be registered twice? See logs
        log.info(">>>> registered URI handler: {}", uriHandler);
      }
  }