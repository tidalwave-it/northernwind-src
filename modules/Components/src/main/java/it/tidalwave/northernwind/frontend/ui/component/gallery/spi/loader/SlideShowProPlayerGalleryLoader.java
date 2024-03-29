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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.beans.factory.BeanFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Specific for the format of SlideShowPro Player for Lightroom 1.9.8.5
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class SlideShowProPlayerGalleryLoader extends GalleryLoaderSupport
  {
    public static final Key<String> P_IMAGES = Key.of("images", String.class);

    private static final String XPATH_IMG = "/gallery/album/img";

    public SlideShowProPlayerGalleryLoader (@Nonnull final BeanFactory beanFactory,
                                            @Nonnull final ResourceProperties properties)
      {
        super(beanFactory, properties);
      }

    @Override @Nonnull
    public List<GalleryItem> loadGallery (@Nonnull final SiteNode siteNode)
      {
        final List<GalleryItem> items = new ArrayList<>();

        try
          {
            final var dbf = DocumentBuilderFactory.newInstance(); // FIXME: inject
            final var db = dbf.newDocumentBuilder(); // FIXME: inject
            final var xPathFactory = XPathFactory.newInstance(); // FIXME: inject

            final var s = siteNode.getProperty(P_IMAGES).orElseThrow(NotFoundException::new); // FIXME
            final var document = db.parse(new InputSource(new StringReader(s)));
            final var xPath = xPathFactory.newXPath();
            final var jx1 = xPath.compile(XPATH_IMG);
            final var nodes = (NodeList)jx1.evaluate(document, XPathConstants.NODESET);

            for (var i = 0; i < nodes.getLength(); i++)
              {
                final var node = nodes.item(i);
                final var src = node.getAttributes().getNamedItem("src").getNodeValue().replaceAll("_", "-").replaceAll("\\.jpg$", "");
                items.add(createItem(new Id(src)));
              }
          }
        catch (ParserConfigurationException | NotFoundException | IOException |
               SAXException | XPathExpressionException | DOMException e)
          {
            log.warn("", e);
          }

        return items;
      }
  }
