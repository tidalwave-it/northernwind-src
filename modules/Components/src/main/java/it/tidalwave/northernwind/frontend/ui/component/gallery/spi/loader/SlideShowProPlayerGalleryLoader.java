/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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

import it.tidalwave.northernwind.core.model.ResourceProperties;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.springframework.beans.factory.BeanFactory;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;
import it.tidalwave.util.NotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

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
    public static final Key<String> P_IMAGES = new Key<String>("images") {};

    private static final String XPATH_IMG = "/gallery/album/img";

    public SlideShowProPlayerGalleryLoader (final @Nonnull BeanFactory beanFactory,
                                            final @Nonnull ResourceProperties properties)
      {
        super(beanFactory, properties);
      }

    @Override @Nonnull
    public List<GalleryItem> loadGallery (final @Nonnull SiteNode siteNode)
      {
        final List<GalleryItem> items = new ArrayList<>();

        try
          {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // FIXME: inject
            final DocumentBuilder db = dbf.newDocumentBuilder(); // FIXME: inject
            final XPathFactory xPathFactory = XPathFactory.newInstance(); // FIXME: inject

            final String s = siteNode.getProperty(P_IMAGES).orElseThrow(NotFoundException::new); // FIXME
            final Document document = db.parse(new InputSource(new StringReader(s)));
            final XPath xPath = xPathFactory.newXPath();
            final XPathExpression jx1 = xPath.compile(XPATH_IMG);
            final NodeList nodes = (NodeList)jx1.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++)
              {
                final Node node = nodes.item(i);
                final String src = node.getAttributes().getNamedItem("src").getNodeValue().replaceAll("_", "-").replaceAll("\\.jpg$", "");
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
