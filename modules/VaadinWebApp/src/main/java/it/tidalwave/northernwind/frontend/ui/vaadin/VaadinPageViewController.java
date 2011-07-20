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

import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.ui.PageViewController;
import it.tidalwave.northernwind.frontend.model.WebSiteModel;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import it.tidalwave.northernwind.frontend.model.Structure;
import it.tidalwave.northernwind.frontend.ui.component.article.DefaultArticleViewController;
import it.tidalwave.northernwind.frontend.ui.component.article.vaadin.VaadinArticleView;
import java.net.URL;
import java.util.Properties;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class VaadinPageViewController implements PageViewController 
  {
    private final URIHandler uriHandler = new URIHandler()
      {
        @Override
        public DownloadStream handleURI (final @Nonnull URL context,
                                         final @Nonnull String relativeUri) 
          {
            log.info("uri: {}", relativeUri);
            setUri(relativeUri);
            return null; 
          }
      };
    
    @Nonnull
    private final PageView pageView;
    
    private final WebSiteModel webSiteModel = new DefaultWebSiteModel();

    public VaadinPageViewController (final @Nonnull VaadinPageView pageView) 
      {
        log.info("DefaultPageViewController()");
        this.pageView = pageView;
        pageView.addURIHandler(uriHandler);
        log.info(">>>> registered URI handler");
      }
    
    private void setUri (final @Nonnull String uri) 
      {
        try
          {
            log.info("setUri({})", uri);
            final Structure structure = webSiteModel.getStructure(uri);
            final Properties properties = structure.getProperties();
            log.info(">>>> properties: {}", properties);
            final String title = properties.getProperty("Title");
            final String contentUri = properties.getProperty("main.content");
            
            pageView.setCaption(title);
            final VaadinArticleView articleView = new VaadinArticleView("main");
            new DefaultArticleViewController(webSiteModel, articleView, contentUri.replaceAll("/content/document/Mobile", "").replaceAll("/content/document", ""));
            pageView.setContents(articleView);
          }
        catch (Exception e)
          {
            log.error("", e);  
          }
      } 
  }