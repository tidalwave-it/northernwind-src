/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ModifiablePath;
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

    @Inject @Nonnull
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
        galleryTemplate = loadTemplate("Bluette.txt");
        fallbackTemplate = loadTemplate("BluetteFallback.txt");
        lightboxFallbackTemplate = loadTemplate("BluetteLightboxFallback.txt");
        final ResourceProperties bluetteConfiguration = context.getSiteNode().getPropertyGroup(new Id("bluetteConfiguration"));
        copyright = bluetteConfiguration.getProperty(PROPERTY_COPYRIGHT, "");
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
        final ModifiablePath baseUrl = context.getSiteNode().getRelativeUri().prepend(site.getContextPath());
        final String previousUrl = site.createLink(baseUrl.append(items.get((index - 1 + itemCount) % itemCount).getId().stringValue()));
        final String nextUrl     = site.createLink(baseUrl.append(items.get((index + 1) % itemCount).getId().stringValue()));
        final String lightboxUrl = site.createLink(baseUrl.append("lightbox"));
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

        try
          {
            final SiteNode siteNode = context.getSiteNode();
            final String link = siteProvider.get().getSite().createLink(siteNode.getRelativeUri().append("images.xml"));

            builder.append("<script type=\"text/javascript\">\n//<![CDATA[\n");
            builder.append(String.format("var bluetteCatalogUrl = \"%s\";%n", link));

            final ResourceProperties bluetteConfiguration = siteNode.getPropertyGroup(new Id("bluetteConfiguration"));

            // FIXME: since key doesn't have dynamic type, we can't properly escape strings.
            for (final Key<?> key : bluetteConfiguration.getKeys())
              {
                if (key.stringValue().startsWith("bluette") || key.stringValue().equals("logging"))
                  {
                    final Object value = bluetteConfiguration.getProperty(key);
                    builder.append(String.format("var %s = %s;%n", key.stringValue(), value));
                  }
              }

            builder.append("//]]>\n</script>\n");
          }
        catch (NotFoundException | IOException e)
          {
            // ok, no configuration
          }

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
        final ModifiablePath baseUrl = context.getSiteNode().getRelativeUri().prepend(site.getContextPath());
        final String redirectUrl = site.createLink(baseUrl.append("/#!/" + item.getId().stringValue())); // FIXME .replaceAll("/$", ""));
        final String previousUrl = site.createLink(baseUrl.append(items.get((index - 1 + itemCount) % itemCount).getId().stringValue()));
        final String nextUrl     = site.createLink(baseUrl.append(items.get((index + 1) % itemCount).getId().stringValue()));
        final String lightboxUrl = site.createLink(baseUrl.append("lightbox"));
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
                                                       .add("imageUrl", "/media/stillimages/800/" + item.getId().stringValue() + ".jpg")
                                                       .add("copyright", copyright);
        textHolder.addAttribute("content", t.render());
        // FIXME: it would be better to change the properties rather than directly touch the template attributes
        textHolder.addAttribute("description", item.getDescription());
        textHolder.addAttribute("inlinedScripts", redirectScript);
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
        final ModifiablePath baseUrl = context.getSiteNode().getRelativeUri().prepend(site.getContextPath());
        final StringBuilder builder = new StringBuilder();

        for (final Item item : items)
          {
            final String id = item.getId().stringValue();
            final String link = site.createLink(baseUrl.append(id));
            builder.append(String.format("<a href=\"%s\"><img src=\"/media/stillimages/100/%s.jpg\"/></a>%n", link, id));
          }

        final String redirectUrl = site.createLink(baseUrl.append("/#!/lightbox")); // FIXME.replaceAll("/$", "");
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
