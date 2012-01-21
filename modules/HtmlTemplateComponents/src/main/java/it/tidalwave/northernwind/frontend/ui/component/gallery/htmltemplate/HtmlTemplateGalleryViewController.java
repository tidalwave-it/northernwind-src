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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
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
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.spi.GalleryAdapterContext;
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
    
    private GalleryAdapter galleryAdapter;

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
        String pathParams = requestHolder.get().getPathParams(siteNode);
        pathParams = pathParams.replace("/", "");
        log.debug(">>>> pathParams: {}", pathParams);
        final TextHolder textHolder = (TextHolder)view;
        
        if (!"".equals(pathParams))
          {
            try 
              {
                String images = siteNode.getProperties().getProperty(new Key<String>(pathParams));
                textHolder.setTemplate("$content$\n");
                textHolder.setContent(images);
                textHolder.setMimeType("text/xml");
                return;
              }
            catch (NotFoundException e) 
              {
                throw new HttpStatusException(404);
              }
            catch (IOException e) 
              {
                throw new HttpStatusException(404);
              }
          }
        
        textHolder.addAttribute("title", "StoppingDown");
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
    
    @Nonnull
    private GalleryAdapter getGalleryAdapter()
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
