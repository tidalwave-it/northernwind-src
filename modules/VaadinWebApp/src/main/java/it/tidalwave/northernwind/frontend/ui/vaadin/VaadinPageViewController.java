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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.URL;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.model.Node;
import it.tidalwave.northernwind.frontend.model.WebSiteModel;
import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.ui.PageViewController;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The Vaadin specialization of {@link PageViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class VaadinPageViewController implements PageViewController 
  {
    /*******************************************************************************************************************
     *
     * Tracks the incoming URI.
     *
     ******************************************************************************************************************/
    private final URIHandler uriHandler = new URIHandler()
      {
        @Override
        public DownloadStream handleURI (final @Nonnull URL context,
                                         final @Nonnull String relativeUri) 
          {
            log.info("uri: {}", relativeUri);
            setContentsByUri("/" + relativeUri);    
            return null; 
          }
      };
    
    @Nonnull
    private final PageView pageView;

    @Nonnull @Inject
    private WebSiteModel webSiteModel;

    /*******************************************************************************************************************
     *
     * Creates a new instance bound to the given view.
     * 
     * @param  pageView   the view
     *
     ******************************************************************************************************************/
    public VaadinPageViewController (final @Nonnull VaadinPageView pageView) 
      {
        log.info("DefaultPageViewController()");
        this.pageView = pageView;
        pageView.addURIHandler(uriHandler);
        log.info(">>>> registered URI handler");
      }
    
    /*******************************************************************************************************************
     *
     * Changes the contents in function of the given uri.
     * 
     * @param  relativeUri   the uri
     *
     ******************************************************************************************************************/
    private void setContentsByUri (final @Nonnull String relativeUri) 
      {
        try
          {
            log.info("setUri({})", relativeUri);
            final Node structure = webSiteModel.findNodeByUri(relativeUri);            
//            pageView.setCaption(structure.getProperties().getProperty("Title")); TODO
            pageView.setContents(structure.createContents());
          }
        catch (Exception e)
          {
            log.error("", e);  
          }
      } 
  }