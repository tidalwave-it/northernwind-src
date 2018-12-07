/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate;

import javax.annotation.Nonnull;
import java.io.IOException;
import org.springframework.beans.factory.BeanFactory;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.gallery.DefaultGalleryViewController;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette.BluetteGalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterContext;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.PROPERTY_TITLE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
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
                                              final @Nonnull RequestLocaleManager requestLocaleManager,
                                              final @Nonnull ModelFactory modelFactory,
                                              final @Nonnull BeanFactory beanFactory)
      throws IOException
      {
        super(view, siteNode, site, requestLocaleManager, beanFactory);
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

        galleryAdapter = new BluetteGalleryAdapter(site, modelFactory, context); // FIXME: get implementation from configuration
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc }
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        super.renderView(context);
        final String param = getParam().replaceAll("^/", "").replaceAll("/$", "");
        log.info(">>>> pathParams: *{}*", param);
        final TextHolder textHolder = (TextHolder)view;
        final String siteNodeTitle = siteNode.getProperty(PROPERTY_TITLE).orElse("");

        switch (param)
          {
            case "":
                galleryAdapter.renderGallery(view, items);
                textHolder.addAttribute("title", siteNodeTitle);
                break;

            case "images.xml":
                galleryAdapter.renderCatalog(view, items);
                break;

            case "lightbox":
                galleryAdapter.renderLightbox(view, items);
                textHolder.addAttribute("title", siteNodeTitle);
                break;

            default: // id of the photo item to render
                final Id id = new Id(param);
                final GalleryItem item = itemMapById.get(id);

                if (item == null)
                  {
                    log.warn("Gallery item not found: {}", id);
                    log.debug("Gallery item not found: {}, available: {}", id, itemMapById.keySet());
                    throw new HttpStatusException(404);
                  }

                galleryAdapter.renderItem(view, item, items);
                textHolder.addAttribute("title", item.getDescription());
                break;
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
