/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.Properties;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.gallery.DefaultGalleryViewController;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette.BluetteGalleryAdapter;
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
    public HtmlTemplateGalleryViewController (final @Nonnull GalleryView view, 
                                              final @Nonnull SiteNode siteNode, 
                                              final @Nonnull Site site, 
                                              final @Nonnull RequestHolder requestHolder, 
                                              final @Nonnull RequestLocaleManager requestLocaleManager)
      throws IOException
      {
        super(view, siteNode, site, requestLocaleManager);
        this.view = view;
        this.siteNode = siteNode;
        this.requestHolder = requestHolder;
        
        final GalleryAdapterContext context = new GalleryAdapterContext()
          {
            @Override
            public void addAttribute (final @Nonnull String name, final @Nonnull String value) 
              {
                ((TextHolder)view).addAttribute(name, value);
              }

            @Override @Nonnull
            public Site getSite() 
              {
                return site;
              }

            @Override @Nonnull
            public SiteNode getSiteNode() 
              {
                return siteNode;
              }

            @Override @Nonnull
            public GalleryView getView() 
              {
                return view;
              }
          };
    
        galleryAdapter = new BluetteGalleryAdapter(context); // FIXME: get implementation from configuration
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
        final String param = getParam().replaceAll("^/", "").replaceAll("/$", "");
        log.info(">>>> pathParams: *{}*", param);
        final TextHolder textHolder = (TextHolder)view;
        final String siteNodeTitle = siteNode.getProperties().getProperty(Properties.PROPERTY_TITLE, "");
        
        if ("".equals(param))
          {
            galleryAdapter.renderGallery(view, items);
            textHolder.addAttribute("title", siteNodeTitle);
          }
        else if ("images.xml".equals(param))
          {
            galleryAdapter.renderCatalog(view, items);
          }
        else if ("lightbox".equals(param))
          {
            galleryAdapter.renderLightboxFallback(view, items);
            textHolder.addAttribute("title", siteNodeTitle);
          }
        else 
          {
            final Id id = new Id(param);
            final Item item = itemMapById.get(id);
            
            if (item == null)
              {
                log.warn("Gallery item not found: {}, available: {}", id, itemMapById.keySet());
                throw new HttpStatusException(404);  
              }
            
            galleryAdapter.renderFallback(view, item, items);
            textHolder.addAttribute("title", item.getDescription());
          }
      }
    
    @Nonnull
    private String getParam()
      { 
        try 
          {
            return requestHolder.get().getParameter("_escaped_fragment_");
          } 
        catch (NotFoundException ex) 
          {
            return requestHolder.get().getPathParams(siteNode);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected String computeInlinedScriptsSection() 
      {
        return super.computeInlinedScriptsSection() + "\n" + galleryAdapter.getInlinedScript();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected ResourceProperties getViewProperties() 
      {
        return super.getViewProperties().merged(galleryAdapter.getExtraViewProperties(view.getId()));
      } 
  }
