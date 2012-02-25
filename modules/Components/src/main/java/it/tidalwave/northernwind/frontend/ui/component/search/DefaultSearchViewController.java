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
package it.tidalwave.northernwind.frontend.ui.component.search;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultSearchViewController implements SearchViewController
  {
    @Nonnull
    private final SearchView view;
    
//    @Nonnull
//    private final SiteNode siteNode;
//    
//    @Nonnull
//    private final Site site;
//
//    @Nonnull
//    private final RequestLocaleManager requestLocaleManager;
    
    @Nonnull
    private final RequestHolder requestHolder;

    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    protected void initialize()
      throws Exception
      {
        final String q = requestHolder.get().getParameter("q") + "%20site:stoppingdown.net";
        final String key = "AIzaSyCo3J58UXPdNM7xHwja8ffkcfNuvK-h2Cc";
        final String cx = "013036536707430787589:_pqjad5hr1a";
        final String query = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&alt=atom";
        // botrona%20site:stoppingdown.net
        final URL url = new URL(String.format(query, key, cx, q));
        view.setContent(IOUtils.toString(url.openStream()));
      }
  }
