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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.io.IOException;
import org.stringtemplate.v4.ST;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.Item;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterContext;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterSupport;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class BluetteGalleryAdapter extends GalleryAdapterSupport
  {
    private static final Key<String> PROPERTY_COPYRIGHT = new Key<>("copyright");

    @Nonnull
    private final GalleryAdapterContext context;

    @Inject
    private Provider<SiteProvider> siteProvider;

    private final String galleryTemplate;

    private final String fallbackTemplate;

    private final String lightboxFallbackTemplate;

    private String copyright = "";

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public BluetteGalleryAdapter (final @Nonnull GalleryAdapterContext context)
      throws IOException
      {
        this.context = context;
        galleryTemplate = loadTemplate(context, "bluette");
        fallbackTemplate = loadTemplate(context, "bluetteFallback");
        lightboxFallbackTemplate = loadTemplate(context, "bluetteLightboxFallback");
        final ResourceProperties bluetteConfiguration = context.getSiteNode().getPropertyGroup(new Id("bluetteConfiguration"));
        copyright = bluetteConfiguration.getProperty(PROPERTY_COPYRIGHT).orElse("");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderGallery (final @Nonnull GalleryView view, final @Nonnull List<Item> items)
      {
        final Item item = items.get(0);
        final int itemCount = items.size();
        final int index = items.indexOf(item);
        final Site site = siteProvider.get().getSite();
        final ResourcePath baseUrl = context.getSiteNode().getRelativeUri().prependedWith(site.getContextPath());
        final String previousUrl = site.createLink(baseUrl.appendedWith(items.get((index - 1 + itemCount) % itemCount).getId().stringValue()));
        final String nextUrl     = site.createLink(baseUrl.appendedWith(items.get((index + 1) % itemCount).getId().stringValue()));
        final String lightboxUrl = site.createLink(baseUrl.appendedWith("lightbox"));
        final ST t = new ST(galleryTemplate, '$', '$').add("caption", item.getDescription())
                                                      .add("previous", previousUrl)
                                                      .add("next", nextUrl)
                                                      .add("lightbox", lightboxUrl)
                                                      .add("home", "/blog") // FIXME
                                                      .add("copyright", copyright);
        context.addAttribute("content", t.render());
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
        final SiteNode siteNode = context.getSiteNode();
        final String link = siteProvider.get().getSite().createLink(siteNode.getRelativeUri().appendedWith("images.xml"));

        builder.append("<script type=\"text/javascript\">\n//<![CDATA[\n");
        builder.append(String.format("var bluetteCatalogUrl = \"%s\";%n", link));

        final ResourceProperties bluetteConfiguration = siteNode.getPropertyGroup(new Id("bluetteConfiguration"));

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
    public void renderCatalog (final @Nonnull GalleryView view, final @Nonnull List<Item> items)
      throws HttpStatusException
      {
        final StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<gallery>\n");

        for (final Item item : items)
          {
            builder.append(String.format("    <stillImage id=\"%s\" title=\"%s\" />%n", item.getId(), item.getDescription()));
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
    public void renderFallback (final @Nonnull GalleryView view,
                                final @Nonnull Item item,
                                final @Nonnull List<Item> items)
      {
        final TextHolder textHolder = (TextHolder)view;
        final int itemCount = items.size();
        final int index = items.indexOf(item);
        final Site site = siteProvider.get().getSite();
        final ResourcePath baseUrl = context.getSiteNode().getRelativeUri().prependedWith(site.getContextPath());
        final String redirectUrl = site.createLink(baseUrl.appendedWith("#!").appendedWith(item.getId().stringValue())).replaceAll("/$", "");
        final String previousUrl = site.createLink(baseUrl.appendedWith(items.get((index - 1 + itemCount) % itemCount).getId().stringValue()));
        final String nextUrl     = site.createLink(baseUrl.appendedWith(items.get((index + 1) % itemCount).getId().stringValue()));
        final String lightboxUrl = site.createLink(baseUrl.appendedWith("lightbox"));
        final String imageId     = item.getId().stringValue();
        final String imageUrl    = "/media/stillimages/800/" + imageId + ".jpg"; // FIXME: with imageId, this is probably useless, drop it
        final String redirectScript = "<script type=\"text/javascript\">\n"
                                    + "//<![CDATA[\n"
                                    + "window.location.replace('" + redirectUrl + "');\n"
                                    + "//]]>\n"
                                    + "</script>\n";
        final ST t = new ST(fallbackTemplate, '$', '$').add("caption", item.getDescription())
                                                       .add("previous", previousUrl)
                                                       .add("next", nextUrl)
                                                       .add("lightbox", lightboxUrl)
                                                       .add("home", "/blog") // FIXME
                                                       .add("imageId", imageId)
                                                       .add("imageUrl", imageUrl)
                                                       .add("copyright", copyright);
        textHolder.addAttribute("content", t.render());
        // FIXME: it would be better to change the properties rather than directly touch the template attributes
        textHolder.addAttribute("description", item.getDescription());
        textHolder.addAttribute("inlinedScripts", redirectScript);
        textHolder.addAttribute("imageId", imageId);
        textHolder.addAttribute("imageUrl", imageUrl);
        textHolder.addAttribute("scripts", "");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderLightboxFallback (final @Nonnull GalleryView view, final @Nonnull List<Item> items)
      {
        final Site site = siteProvider.get().getSite();
        final ResourcePath baseUrl = context.getSiteNode().getRelativeUri().prependedWith(site.getContextPath());
        final StringBuilder builder = new StringBuilder();

        for (final Item item : items)
          {
            final String id = item.getId().stringValue();
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
        final ST t = new ST(lightboxFallbackTemplate, '$', '$').add("content", builder.toString())
                                                               .add("copyright", copyright);
        textHolder.addAttribute("content", t.render());
        textHolder.addAttribute("inlinedScripts", redirectScript);
        textHolder.addAttribute("scripts", "");
      }
  }
