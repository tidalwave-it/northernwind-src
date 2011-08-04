/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.role.annotation.RoleImplementation;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.spi.Unmarshallable;
import it.tidalwave.northernwind.frontend.ui.Layout;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * JAXP is a PITA, but the document structure is simple and so we don't require further dependencies.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RoleImplementation(ownerClass=Layout.class) @ToString @Slf4j
public class LayoutXmlUnmarshallable implements Unmarshallable // TODO: reimplement with JAXB, rename to LayourJaxbUnmarshallable
  {
    @Nonnull
    private final Layout layout;
    
    @Inject @Nonnull
    private ModelFactory modelFactory;
    
    private Document document;

    public LayoutXmlUnmarshallable (final @Nonnull Layout layout) 
      {
        this.layout = layout;
      }
    
    @Override @Nonnull
    public Layout unmarshal (final @Nonnull InputStream is)
      throws IOException
      {  
        try 
          {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(is);
            
            final Element layoutElement = document.getDocumentElement();
            
            if (!"layout".equals(layoutElement.getNodeName()))
              {
                throw new IOException("Syntax error: 'layout' expected");
              }
            // TODO: check document version (attribute of layout)
            
            final List<Node> componentNodes = getChildComponentNodes(layoutElement);
            
            if (componentNodes.size() != 1)
              {
                throw new IOException("Syntax error: expected one 'component' element, were " + componentNodes.size());  
              }
            
             return parseComponent(componentNodes.get(0));
          }
        catch (ParserConfigurationException e)
          {
            throw new IOException("", e);
          }
        catch (SAXException e)
          {
            throw new IOException("", e);
          }
      }
    
    @Nonnull
    private Layout parseComponent (final @Nonnull Node componentNode)
      {
        final Id id = new Id(componentNode.getAttributes().getNamedItem("id").getNodeValue());
        final String type = componentNode.getAttributes().getNamedItem("type").getNodeValue();
        
        Layout layout2 = modelFactory.createLayout(id, type);
        
        for (final Node node : getChildComponentNodes(componentNode))
          {
            layout2 = layout2.withLayout(parseComponent(node));                
          }
        
        return layout2;
      }
    
    @Nonnull
    private static List<Node> getChildComponentNodes (final @Nonnull Node parentNode)
      {
        final List<Node> nodes = new ArrayList<Node>();
        final NodeList childElements = parentNode.getChildNodes();

        for (int i = 0; i < childElements.getLength(); i++)
          {
            final Node childElement = childElements.item(i);

            if ("component".equals(childElement.getNodeName()))
              {
                nodes.add(childElement);  
              }
          }

        return nodes;
      }
  }
