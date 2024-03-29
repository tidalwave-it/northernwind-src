/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import org.springframework.beans.factory.BeanFactory;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.gallery.DefaultGalleryViewController;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette.BluetteGalleryAdapter;
import lombok.extern.slf4j.Slf4j;
import static javax.servlet.http.HttpServletResponse.*;
import static it.tidalwave.northernwind.core.model.Content.P_TITLE;

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

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public HtmlTemplateGalleryViewController (@Nonnull final GalleryView view,
                                              @Nonnull final SiteNode siteNode,
                                              @Nonnull final RequestLocaleManager requestLocaleManager,
                                              @Nonnull final ModelFactory modelFactory,
                                              @Nonnull final BeanFactory beanFactory)
      {
        super(view, siteNode, requestLocaleManager, beanFactory);
        this.view = view;
        this.siteNode = siteNode;
        galleryAdapter = new BluetteGalleryAdapter(siteNode, view, modelFactory); // FIXME: get implementation from configuration
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareRendering (@Nonnull final RenderContext context)
      throws Exception
      {
        super.prepareRendering(context);
        final var param = getParam(context);
        log.info(">>>> pathParams: *{}*", param);
        final var textHolder = (TextHolder)view;
        final var siteNodeTitle = siteNode.getProperty(P_TITLE).orElse("");

        switch (param.getSegmentCount())
          {
            case 0: // no args, full JS gallery
                galleryAdapter.prepareGallery(items.get(0), items);
                textHolder.addAttribute("title", siteNodeTitle);
                break;

            case 1:
                switch (param.getLeading())
                  {
                    case "images.xml":
                        galleryAdapter.prepareCatalog(items);
                        break;

                    case "lightbox":
                        galleryAdapter.prepareFallbackLightbox(items);
                        textHolder.addAttribute("title", siteNodeTitle);
                        break;

                    default: // id of the gallery item to render, fallback mode
                        final var id = new Id(param.getLeading());
                        final var item = itemMapById.get(id);

                        if (item == null)
                          {
                            log.warn("Gallery item not found: {}", id);
                            log.debug("Gallery item not found: {}, available: {}", id, itemMapById.keySet());
                            throw new HttpStatusException(SC_NOT_FOUND);
                          }

                        galleryAdapter.prepareFallbackGallery(item, items);
                        textHolder.addAttribute("title", item.getDescription());
                        break;
                  }

                break;

            default:
                throw new HttpStatusException(SC_BAD_REQUEST);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (@Nonnull final RenderContext context)
      throws Exception
      {
        super.renderView(context);
        galleryAdapter.render(context);
      }

    @Nonnull
    private ResourcePath getParam (@Nonnull final RenderContext context)
      {
        return context.getQueryParam("_escaped_fragment_").map(ResourcePath::of)
                                                          .orElse(context.getPathParams(siteNode));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected final String computeInlinedScriptsSection()
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
