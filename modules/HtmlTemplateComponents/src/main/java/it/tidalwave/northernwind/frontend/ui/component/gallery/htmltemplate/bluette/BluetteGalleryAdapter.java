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
package it.tidalwave.northernwind.frontend.ui.component.gallery.htmltemplate.bluette;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterContext;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapterSupport;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import lombok.extern.slf4j.Slf4j;
import org.stringtemplate.v4.ST;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class BluetteGalleryAdapter extends GalleryAdapterSupport
  {
    private GalleryAdapterContext context;
    
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    private final String galleryContent;
    
    private final String fallbackContent;
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public BluetteGalleryAdapter() 
      throws IOException
      {
        galleryContent = loadTemplate("Bluette.txt");    
        fallbackContent = loadTemplate("BluetteFallback.txt");    
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override // FIXME: what about @PostConstruct and injecting the context?
    public void initialize (final @Nonnull GalleryAdapterContext context) 
      {
        this.context = context;
        context.addAttribute("content", galleryContent);        
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
            final String link = siteProvider.get().getSite().createLink(siteNode.getRelativeUri() + "/images.xml");

            builder.append("<script type=\"text/javascript\">\n//<![CDATA[\n");
            builder.append(String.format("var catalogUrl = \"%s\";\n", link));
            
            final ResourceProperties bluetteConfiguration = siteNode.getPropertyGroup(new Id("bluetteConfiguration"));
            
            // FIXME: since key doesn't have dynamic type, we can't properly escape strings.
            for (final Key<?> key : bluetteConfiguration.getKeys())
              {
                final Object value = bluetteConfiguration.getProperty(key);
                builder.append(String.format("var %s = %s;\n", key.stringValue(), value));
              }

            builder.append("//]]>\n</script>\n");
          } 
        catch (NotFoundException e) 
          {
            // ok, no configuration
          }
        catch (IOException e) 
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
    public void createItemCatalog (final @Nonnull GalleryView view, final @Nonnull List<Item> items)
      throws HttpStatusException
      {
        try 
          {
            final TextHolder textHolder = (TextHolder)view;
            textHolder.setTemplate("$content$\n");
            textHolder.setContent(context.getSiteNode().getProperties().getProperty(new Key<String>("images.xml")));
//            textHolder.setContent(context.getSiteNode().getProperties().getProperty(new Key<String>(pathParams)));
            textHolder.setMimeType("text/xml");
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

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void createFallback (final @Nonnull GalleryView view,
                                final @Nonnull String key, 
                                final @Nonnull Item item) 
      {
        final TextHolder textHolder = (TextHolder)view;
        final String redirectUrl = siteProvider.get().getSite().getContextPath() + context.getSiteNode().getRelativeUri() + "/#!/" + key;
        final String redirectScript = "<script type=\"text/javascript\">\n"
                                    + "//<![CDATA[\n"
                                    + "window.location.replace('" + redirectUrl + "');\n"
                                    + "//]]>\n"
                                    + "</script>\n";
        final ST t = new ST(fallbackContent, '$', '$').add("caption", item.getDescription())
                                                      .add("imageUrl", "/media/stillimages/1280/" + key + ".jpg");
        textHolder.addAttribute("content", t.render());
        // FIXME: it would be better to change the properties rather than directly touch the template attributes
        textHolder.addAttribute("description", item.getDescription());
        textHolder.addAttribute("inlinedScripts", redirectScript);
        textHolder.addAttribute("scripts", "");
      }
  }
