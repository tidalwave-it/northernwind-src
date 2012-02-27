/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader;

import java.util.List;
import javax.annotation.Nonnull;
import java.util.ArrayList;
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
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.Item;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Specific for the format of SlideShowPro Player for Lightroom 1.9.8.5
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class SlideShowProPlayerGalleryLoader implements GalleryLoader
  {
    private static final String XPATH_IMG = "/gallery/album/img";
    
    @Override @Nonnull
    public List<Item> loadGallery (final @Nonnull SiteNode siteNode) 
      {
        final List<Item> items = new ArrayList<Item>();
        
        try 
          {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // FIXME: inject
            final DocumentBuilder db = dbf.newDocumentBuilder(); // FIXME: inject
            final XPathFactory xPathFactory = XPathFactory.newInstance(); // FIXME: inject
            
            final String s = siteNode.getProperties().getProperty(new Key<String>("images.xml"));
            final Document document = db.parse(new InputSource(new StringReader(s)));
            final XPath xPath = xPathFactory.newXPath();
            final XPathExpression jx1 = xPath.compile(XPATH_IMG);            
            final NodeList nodes = (NodeList)jx1.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < nodes.getLength(); i++)
              {
                final Node node = nodes.item(i);
                final String src = node.getAttributes().getNamedItem("src").getNodeValue().replaceAll("\\.jpg$", "");
                final String title = node.getAttributes().getNamedItem("title").getNodeValue();
//                final String caption = node.getAttributes().getNamedItem("caption").getNodeValue();
                items.add(new Item(src, title));
              }
          }
        catch (Exception e)
          {
            log.warn("", e);
          }
            
        return items;
      }
  }
