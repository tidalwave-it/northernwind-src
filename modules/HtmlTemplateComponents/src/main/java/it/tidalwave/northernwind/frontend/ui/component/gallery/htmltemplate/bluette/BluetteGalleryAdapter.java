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
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterSupport;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class BluetteGalleryAdapter extends GalleryAdapterSupport
  {
    private static final Key<String> P_COPYRIGHT = new Key<String>("copyright") {};

    private static final Key<ResourcePath> P_BLUETTE_TEMPLATE_PATH = new Key<ResourcePath>("bluettePath") {};

    private static final Key<ResourcePath> P_BLUETTE_FALLBACK_TEMPLATE_PATH = new Key<ResourcePath>("bluetteFallbackPath") {};

    private static final Key<ResourcePath> P_BLUETTE_LIGHTBOX_FALLBACK_TEMPLATE_PATH = new Key<ResourcePath>("bluetteLightboxFallbackPath") {};

    @Nonnull
    private final Site site;

    private final Template galleryTemplate;

    private final Template fallbackTemplate;

    private final Template lightboxFallbackTemplate;

    private String copyright = "";

    private final ResourceProperties bluetteConfiguration;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public BluetteGalleryAdapter (final @Nonnull SiteNode siteNode,
                                  final @Nonnull GalleryView view,
                                  final @Nonnull ModelFactory modelFactory)
      {
        super(siteNode, view, modelFactory);
        this.site = siteNode.getSite();
        bluetteConfiguration = siteNode.getPropertyGroup(new Id("bluetteConfiguration"));

        galleryTemplate          = loadTemplate(P_BLUETTE_TEMPLATE_PATH, "bluette.txt");
        fallbackTemplate         = loadTemplate(P_BLUETTE_FALLBACK_TEMPLATE_PATH, "bluetteFallback.txt");
        lightboxFallbackTemplate = loadTemplate(P_BLUETTE_LIGHTBOX_FALLBACK_TEMPLATE_PATH, "bluetteLightboxFallback.txt");
        copyright = bluetteConfiguration.getProperty(P_COPYRIGHT).orElse("");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getInlinedScript()
      {
        final StringBuilder builder = new StringBuilder();
        final String link = site.createLink(siteNode.getRelativeUri().appendedWith("images.xml"));

        builder.append("<script type=\"text/javascript\">\n//<![CDATA[\n");
        builder.append(String.format("var bluetteCatalogUrl = \"%s\";%n", link));

        // FIXME: since key doesn't have dynamic type, we can't properly escape strings.
        for (final Key<?> key : bluetteConfiguration.getKeys())
          {
            if (key.stringValue().startsWith("bluette") || key.stringValue().equals("logging"))
              {
                bluetteConfiguration.getProperty(key).ifPresent(value -> builder.append(String.format("var %s = %s;%n", key.stringValue(), value)));
              }
          }

        builder.append("//]]>\n</script>\n");

        return builder.toString();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderCatalog (final @Nonnull List<GalleryItem> items)
      throws HttpStatusException
      {
        final StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<gallery>\n");

        for (final GalleryItem item : items)
          {
            builder.append(String.format("    <stillImage id=\"%s\" title=\"%s\" />%n",
                                         item.getId(), item.getDescription()));
          }

        builder.append("</gallery>\n");

        final TextHolder textHolder = (TextHolder)view;
        textHolder.setTemplate("$content$\n");
        textHolder.setContent(builder.toString());
        textHolder.setMimeType("text/xml");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderGallery (final @Nonnull List<GalleryItem> items)
      {
        final GalleryItem item     = items.get(0);
        final int count            = items.size();
        final int index            = items.indexOf(item);
        final int prevIndex        = (index - 1 + count) % count;
        final int nextIndex        = (index + 1) % count;
        final ResourcePath baseUrl = siteNode.getRelativeUri().prependedWith(site.getContextPath());

        final String previousUrl   = site.createLink(baseUrl.appendedWith(items.get(prevIndex).getId().stringValue()));
        final String nextUrl       = site.createLink(baseUrl.appendedWith(items.get(nextIndex).getId().stringValue()));
        final String lightboxUrl   = site.createLink(baseUrl.appendedWith("lightbox"));

        galleryTemplate.addAttribute("caption",   item.getDescription())
                       .addAttribute("previous",  previousUrl)
                       .addAttribute("next",      nextUrl)
                       .addAttribute("lightbox",  lightboxUrl)
                       .addAttribute("home",      "/blog") // FIXME
                       .addAttribute("copyright", copyright);

        final TextHolder textHolder = (TextHolder)view;
        textHolder.addAttribute("content", galleryTemplate.render());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderItem (final @Nonnull GalleryItem item, final @Nonnull List<GalleryItem> items)
      {
        final int count            = items.size();
        final int index            = items.indexOf(item);
        final int prevIndex        = (index - 1 + count) % count;
        final int nextIndex        = (index + 1) % count;
        final ResourcePath baseUrl = siteNode.getRelativeUri().prependedWith(site.getContextPath());

        final String imageId       = item.getId().stringValue();
        final String redirectUrl   = site.createLink(baseUrl.appendedWith("#!").appendedWith(imageId)).replaceAll("/$", "");
        final String previousUrl   = site.createLink(baseUrl.appendedWith(items.get(prevIndex).getId().stringValue()));
        final String nextUrl       = site.createLink(baseUrl.appendedWith(items.get(nextIndex).getId().stringValue()));
        final String lightboxUrl   = site.createLink(baseUrl.appendedWith("lightbox"));
        
        final String imageUrl      = "/media/stillimages/800/" + imageId + ".jpg"; // FIXME: parametrise size
        final String redirectScript = "<script type=\"text/javascript\">\n"
                                    + "//<![CDATA[\n"
                                    + "window.location.replace('" + redirectUrl + "');\n"
                                    + "//]]>\n"
                                    + "</script>\n";

        fallbackTemplate.addAttribute("caption",   item.getDescription())
                        .addAttribute("previous",  previousUrl)
                        .addAttribute("next",      nextUrl)
                        .addAttribute("lightbox",  lightboxUrl)
                        .addAttribute("home",      "/blog") // FIXME
                        .addAttribute("imageId",   imageId)
                        .addAttribute("imageUrl",  imageUrl)
                        .addAttribute("copyright", copyright);

        final TextHolder textHolder = (TextHolder)view;
        textHolder.addAttribute("content",        fallbackTemplate.render());
        // FIXME: it would be better to change the properties rather than directly touch the template attributes
        textHolder.addAttribute("description",    item.getDescription());
        textHolder.addAttribute("inlinedScripts", redirectScript);
        textHolder.addAttribute("imageId",        imageId);
        textHolder.addAttribute("imageUrl",       imageUrl);
        textHolder.addAttribute("scripts",        "");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderLightbox (final @Nonnull List<GalleryItem> items)
      {
        final ResourcePath baseUrl = siteNode.getRelativeUri().prependedWith(site.getContextPath());
        final StringBuilder builder = new StringBuilder();

        for (final GalleryItem item : items)
          {
            final String id   = item.getId().stringValue();
            final String link = site.createLink(baseUrl.appendedWith(id));
            builder.append(String.format("<a href=\"%s\"><img src=\"/media/stillimages/100/%s.jpg\"/></a>%n", link, id));
          }

        final String redirectUrl = site.createLink(baseUrl.appendedWith("#!").appendedWith("lightbox")).replaceAll("/$", "");
        final String redirectScript = "<script type=\"text/javascript\">\n"
                                    + "//<![CDATA[\n"
                                    + "window.location.replace('" + redirectUrl + "');\n"
                                    + "//]]>\n"
                                    + "</script>\n";
        final TextHolder textHolder = (TextHolder)view;
        lightboxFallbackTemplate.addAttribute("content", builder.toString())
                                .addAttribute("copyright", copyright);
        textHolder.addAttribute("content", lightboxFallbackTemplate.render());
        textHolder.addAttribute("inlinedScripts", redirectScript);
        textHolder.addAttribute("scripts", "");
      }
  }
