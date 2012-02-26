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
package it.tidalwave.northernwind.frontend.ui.component.gallery;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter.Item;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerView;
import it.tidalwave.util.Key;
import java.io.StringReader;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultGalleryViewController extends DefaultNodeContainerViewController implements GalleryViewController
  {
    @Nonnull
    private final SiteNode siteNode;
    
    @Nonnull
    private final NodeContainerView view;
    
    protected GalleryAdapter galleryAdapter;
    
    protected final List<Item> items = new ArrayList<Item>();
   
    protected final SortedMap<String, Item> itemMapByKey = new TreeMap<String, Item>();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultGalleryViewController (final @Nonnull NodeContainerView view, 
                                         final @Nonnull SiteNode siteNode,
                                         final @Nonnull Site site, 
                                         final @Nonnull RequestLocaleManager requestLocaleManager)
      {
        super(view, siteNode, site, requestLocaleManager);
        this.siteNode = siteNode;
        this.view = view;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void initialize()
      {
        loadItems();  
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<SiteNode> findChildrenSiteNodes()
      {
        return new SimpleFinderSupport<SiteNode>()
          {
            @Override
            protected List<? extends SiteNode> computeResults()
              {
                log.info("findChildrenSiteNodes()");
                final List<SiteNode> results = new ArrayList<SiteNode>();

                for (final Item item : itemMapByKey.values())
                  {
                    final String relativeUri = siteNode.getRelativeUri() + "/"  + item.getRelativePath() + "/";
                    results.add(new ChildSiteNode(siteNode, relativeUri, siteNode.getProperties()));
                  }

                log.info(">>>> returning: {}", results);

                return results;
              }
          };
      }
    
    /*******************************************************************************************************************
     *
     * FIXME: specific for the format of SlideShowPro Player for Lightroom 1.9.8.5
     *
     ******************************************************************************************************************/
    private void loadItems()
      {
        try 
          {
            final String s = siteNode.getProperties().getProperty(new Key<String>("images.xml"));
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.parse(new InputSource(new StringReader(s)));
            final XPathFactory xPathFactory = XPathFactory.newInstance();
            final XPath xPath = xPathFactory.newXPath();

            final String jq1 = "/gallery/album/img";
            final XPathExpression jx1 = xPath.compile(jq1);            
            final NodeList nodes = (NodeList)jx1.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < nodes.getLength(); i++)
              {
                final Node node = nodes.item(i);
                final String src = node.getAttributes().getNamedItem("src").getNodeValue().replaceAll("\\.jpg$", "");
                final String title = node.getAttributes().getNamedItem("title").getNodeValue();
//                final String caption = node.getAttributes().getNamedItem("caption").getNodeValue();
                final Item item = new Item(src, title);
                items.add(item);
                itemMapByKey.put(src, item);
              }
          }
        catch (Exception e)
          {
            log.warn("", e);
          }
      }
  }
