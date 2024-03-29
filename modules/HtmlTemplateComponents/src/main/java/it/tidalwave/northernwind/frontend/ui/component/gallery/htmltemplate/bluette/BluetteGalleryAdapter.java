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
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterSupport;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class BluetteGalleryAdapter extends GalleryAdapterSupport
  {
    private static final Key<String> P_COPYRIGHT = Key.of("copyright", String.class);

    private static final Key<ResourcePath> P_BLUETTE_TEMPLATE_PATH = Key.of("bluettePath", ResourcePath.class);

    /** The path to the template for fallback rendering of a single gallery page, when JavaScript is not available. */
    private static final Key<ResourcePath> P_BLUETTE_FALLBACK_TEMPLATE_PATH = Key.of("bluetteFallbackPath", ResourcePath.class);

    /** The path to the template for fallback rendering of the light box, when JavaScript is not available. */
    private static final Key<ResourcePath> P_BLUETTE_LIGHTBOX_FALLBACK_TEMPLATE_PATH = Key.of("bluetteLightboxFallbackPath", ResourcePath.class);

    /** The path to the template for the script that redirects a page to the dynamic counterpart (with #!). */
    private static final Key<ResourcePath> P_BLUETTE_REDIRECT_SCRIPT_TEMPLATE_PATH = Key.of("bluetteRedirectScriptPath", ResourcePath.class);

    /** The path to the template for the image catalog. */
    private static final Key<ResourcePath> P_BLUETTE_CATALOG_TEMPLATE_PATH = Key.of("bluetteCatalogPath", ResourcePath.class);

    private static final Key<ResourcePath> P_BLUETTE_VARIABLES_TEMPLATE_PATH = Key.of("bluetteVariablesPath", ResourcePath.class);

    private static final Key<String> P_CATALOG_URL = Key.of("bluetteCatalogUrl", String.class);

    @Nonnull
    private final Site site;

    @Nonnull
    private final Template defaultTemplate;

    @Nonnull
    private final Template fallbackTemplate;

    @Nonnull
    private final Template lightboxFallbackTemplate;

    @Nonnull
    private final Template redirectScriptTemplate;

    @Nonnull
    private final Template catalogTemplate;

    @Nonnull
    private final Template variablesTemplate;

    @Nonnull
    private final ResourceProperties bluetteConfiguration;

    @Nonnull
    private final ResourcePath baseUrl;

    @Nonnull
    private final String copyright;

    private final int fallbackImageSize = 1280; // FIXME: parametrise size

    private final int fallbackThumbnailSize = 100; // FIXME: parametrise size

    private final ResourcePath lightboxSegmentUri = ResourcePath.of("lightbox");

    @Nonnull
    private final String homeUrl;

    private String rendered = "";

    private boolean catalog;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public BluetteGalleryAdapter (@Nonnull final SiteNode siteNode,
                                  @Nonnull final GalleryView view,
                                  @Nonnull final ModelFactory modelFactory)
      {
        super(siteNode, view, modelFactory);
        this.site = siteNode.getSite();
        bluetteConfiguration = siteNode.getPropertyGroup(new Id("bluetteConfiguration"));
        baseUrl              = siteNode.getRelativeUri().prependedWith(site.getContextPath());
        homeUrl              = site.createLink(ResourcePath.of("/blog")); // FIXME
        copyright            = bluetteConfiguration.getProperty(P_COPYRIGHT).orElse("");

        defaultTemplate          = loadTemplate(P_BLUETTE_TEMPLATE_PATH,                   "Default.st");
        fallbackTemplate         = loadTemplate(P_BLUETTE_FALLBACK_TEMPLATE_PATH,          "Fallback.st");
        lightboxFallbackTemplate = loadTemplate(P_BLUETTE_LIGHTBOX_FALLBACK_TEMPLATE_PATH, "LightBoxFallback.st");
        redirectScriptTemplate   = loadTemplate(P_BLUETTE_REDIRECT_SCRIPT_TEMPLATE_PATH,   "RedirectScript.st");
        catalogTemplate          = loadTemplate(P_BLUETTE_CATALOG_TEMPLATE_PATH,           "Catalog.st");
        variablesTemplate        = loadTemplate(P_BLUETTE_VARIABLES_TEMPLATE_PATH,         "Variables.st");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getInlinedScript()
      {
        final var link = site.createLink(siteNode.getRelativeUri().appendedWith("images.xml"));
        final var properties = bluetteConfiguration.withProperty(P_CATALOG_URL, "'" + link + "'");
        // FIXME: since key doesn't have dynamic type, we can't properly escape strings.
        final var variables = properties.getKeys().stream()
                                        .filter(k -> k.getName().startsWith("bluette") || "logging".equals(k.getName()))
                                        .flatMap(k -> toAggregate(properties, k).stream())
                                        .collect(toAggregates("entries"));

        return variablesTemplate.render(variables) + "\n" + redirectScriptTemplate.render();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareCatalog (@Nonnull final List<? extends GalleryItem> items)
      {
        final var entries = items.stream().map(this::toAggregate).collect(toAggregates("entries"));
        rendered = catalogTemplate.render(entries);
        catalog = true;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // TODO: Could we manage #! params here and select the proper item?
    @Override
    public void prepareGallery (@Nonnull final GalleryItem item, @Nonnull final List<? extends GalleryItem> items)
      {
        prepare(item, items, defaultTemplate);
        rendered = defaultTemplate.render();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareFallbackGallery (@Nonnull final GalleryItem item, @Nonnull final List<? extends GalleryItem> items)
      {
        prepare(item, items, fallbackTemplate);
        rendered = fallbackTemplate.render();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareFallbackLightbox (@Nonnull final List<? extends GalleryItem> items)
      {
        final var entries = items.stream().map(this::toAggregate).collect(toAggregates("entries"));
        final var redirectUrl = site.createLink(baseUrl.appendedWith("#!").appendedWith(lightboxSegmentUri)).replaceAll("/$", "");
        redirectScriptTemplate.addAttribute("redirectUrl", redirectUrl);
        lightboxFallbackTemplate.addAttribute("copyright", copyright);
        rendered = lightboxFallbackTemplate.render(entries);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void render (@Nonnull final RenderContext context)
      {
        final var textHolder = (TextHolder)view;

        if (!catalog)
          {
            textHolder.addAttribute("content", rendered);
          }
        else
          {
            textHolder.setTemplate("$content$\n");
            textHolder.setContent(rendered);
            textHolder.setMimeType("text/xml");
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void prepare (// final @Nonnull RenderContext context,
                          @Nonnull final GalleryItem item,
                          @Nonnull final List<? extends GalleryItem> items,
                          @Nonnull final Template template)
      {
        final var count          = items.size();
        final var index          = items.indexOf(item);
        final var prevIndex      = (index - 1 + count) % count;
        final var nextIndex      = (index + 1) % count;

        final var imageId     = item.getId().stringValue();
        final var imageUrl    = createImageLink(imageId, fallbackImageSize);

        final var redirectUrl = site.createLink(baseUrl.appendedWith("#!").appendedWith(imageId)).replaceAll("/$", "");
        final var previousUrl = site.createLink(baseUrl.appendedWith(items.get(prevIndex).getId().stringValue()));
        final var nextUrl     = site.createLink(baseUrl.appendedWith(items.get(nextIndex).getId().stringValue()));
        final var lightboxUrl = site.createLink(baseUrl.appendedWith(lightboxSegmentUri));

        template.addAttribute("caption",   item.getDescription())
                .addAttribute("previous",  previousUrl)
                .addAttribute("next",      nextUrl)
                .addAttribute("lightbox",  lightboxUrl)
                .addAttribute("home",      homeUrl)
                .addAttribute("imageId",   imageId)
                .addAttribute("imageUrl",  imageUrl)
                .addAttribute("copyright", copyright);

        if (template != defaultTemplate)
          {
            redirectScriptTemplate.addAttribute("redirectUrl", redirectUrl);

    //        context.setDynamicNodeProperty(PD_IMAGE_ID, imageId);
            // FIXME: these should be dynamic properties
    //        siteNodeProperties.getProperty().ifPresent(id -> view.addAttribute("imageId", id));
    //        siteNodeProperties.getProperty(PD_URL).ifPresent(id -> view.addAttribute("url", id));
            final var textHolder = (TextHolder)view;
            textHolder.addAttribute("description",    item.getDescription());
            textHolder.addAttribute("imageId",        imageId);
            textHolder.addAttribute("imageUrl",       imageUrl);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (@Nonnull final GalleryItem item)
      {
        final var id   = item.getId().stringValue();
        final var link = site.createLink(baseUrl.appendedWith(id));
        return Aggregate.of("id",    id)
                      .with("link",  link)
                      .with("url",   createImageLink(id, fallbackThumbnailSize))
                      .with("title", item.getDescription().replaceAll("\n+", " "));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Optional<Aggregate> toAggregate (@Nonnull final ResourceProperties properties,
                                                    @Nonnull final Key<?> key)
      {
        // A space in front of [ ] makes them interpreted as a string and not a list.
        return properties.getProperty(key).map(v -> Aggregate.of("name", key.stringValue()).with("value", v.toString().replace("[", " [")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private String createImageLink (@Nonnull final String id, @Nonnegative final int size)
      {
        return site.createLink(ResourcePath.of(String.format("/media/stillimages/%d/%s.jpg", size, id)));
      }
  }
