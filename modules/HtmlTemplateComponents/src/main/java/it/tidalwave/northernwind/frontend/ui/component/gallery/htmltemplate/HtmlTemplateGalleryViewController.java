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
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.gallery.DefaultGalleryViewController;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette.BluetteGalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterContext;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlTemplateGalleryViewController extends DefaultGalleryViewController
  {
    @Nonnull
    private final GalleryView view;
  
    @Nonnull
    private final SiteNode siteNode;
    
    @Nonnull
    private final RequestHolder requestHolder;
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private final GalleryAdapterContext context = new GalleryAdapterContext()
      {
        @Override
        public void addAttribute (final @Nonnull String name, final @Nonnull String value) 
          {
            ((TextHolder)view).addAttribute(name, value);
          }

        @Override @Nonnull
        public SiteNode getSiteNode() 
          {
            return siteNode;
          }
      };
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public HtmlTemplateGalleryViewController (final @Nonnull GalleryView view, 
                                              final @Nonnull SiteNode siteNode, 
                                              final @Nonnull Site site, 
                                              final @Nonnull RequestHolder requestHolder, 
                                              final @Nonnull RequestLocaleManager requestLocaleManager)
      {
        super(view, siteNode, site, requestLocaleManager);
        this.view = view;
        this.siteNode = siteNode;
        this.requestHolder = requestHolder;
      }
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initializeHtmlTemplateGalleryViewController() 
      throws HttpStatusException, IOException
      {
        final String pathParams = requestHolder.get().getPathParams(siteNode).replace("/", "");
        log.debug(">>>> pathParams: {}", pathParams);
        final TextHolder textHolder = (TextHolder)view;
        
        if ("images.xml".equals(pathParams))
          {
            getGalleryAdapter().createItemCatalog(view, items);
          }
        else if (!"".equals(pathParams))
          {
            final String key = pathParams.replaceAll("^/", "").replaceAll("/$", "");
            final Item item = itemMapByKey.get(key);
            
            if (item == null)
              {
                log.warn("Gallery item not found: {}, available: {}", key, itemMapByKey.keySet());
                throw new HttpStatusException(404);  
              }
            
            getGalleryAdapter().createFallback(view, key, item);
          }
        
        textHolder.addAttribute("title", "StoppingDown"); // FIXME
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected String computeInlinedScriptsSection() 
      {
        return super.computeInlinedScriptsSection() + "\n" + getGalleryAdapter().getInlinedScript();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected ResourceProperties getViewProperties() 
      {
        return super.getViewProperties().merged(getGalleryAdapter().getExtraViewProperties(view.getId()));
      } 
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private synchronized GalleryAdapter getGalleryAdapter()
      {
        if (galleryAdapter == null)
          {
            try 
              {
                galleryAdapter = new BluetteGalleryAdapter();
                galleryAdapter.initialize(context);        
              } 
            catch (IOException e) 
              {
                throw new RuntimeException(e);
              }
          }
        
        return galleryAdapter;  
      }
  }
