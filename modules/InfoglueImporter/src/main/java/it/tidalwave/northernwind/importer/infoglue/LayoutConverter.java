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
package it.tidalwave.northernwind.importer.infoglue;

import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.role.Marshallable;
import it.tidalwave.role.Unmarshallable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LayoutConverter extends Parser
  {
    private static final List<String> PROPERTIES_REFERRING_RELATIVE_PATHS = Arrays.asList
      (
        "styleSheets", "items", "content", "contents", "rssFeeds", "inlinedScripts"
      );
    
    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>()
      {{
        put(  "7", "http://northernwind.tidalwave.it/component/NodeContainer/#v1.0");
        put( "67", "http://northernwind.tidalwave.it/component/Sidebar/#v1.0");
        put( "71", "http://northernwind.tidalwave.it/component/RssFeed/#v1.0");
        put("104", "http://northernwind.tidalwave.it/component/Blog/#v1.0");
        put( "36", "http://northernwind.tidalwave.it/component/HorizontalMenu/#v1.0");
        put( "21", "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0");
        put( "44", "http://northernwind.tidalwave.it/component/HtmlTextWithTitle/#v1.0");
        put("853", "http://northernwind.tidalwave.it/component/StatCounter/#v1.0");
        put("883", "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0");
        put("873", "http://northernwind.tidalwave.it/component/AddThis/#v1.0");
      }};
            
    private final SortedMap<Key<?>, Object> properties;
    private Id componentId;
    private DefaultLayout rootComponent;
    private final Stack<DefaultLayout> componentStack = new Stack<DefaultLayout>();
    private final Map<String, DefaultLayout> wrapperLayouts = new HashMap<String, DefaultLayout>();

    public LayoutConverter (final @Nonnull String xml, 
                            final @Nonnull DateTime modifiedDateTime, 
                            final @Nonnull String path,
                            final @Nonnull SortedMap<Key<?>, Object> properties) 
      throws XMLStreamException 
      {
        super(xml, path, modifiedDateTime, null);
        this.properties = properties;
      }

    @Override
    protected void processStartElement (final @Nonnull String elementName, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        log.trace("processStartElement({})", elementName);
        
        if ("component".equals(elementName))
          {
            final String attrNameValue = reader.getAttributeValue("", "name");
            final String attrIdValue = reader.getAttributeValue("", "id");
            final String xxx = reader.getAttributeValue("", "contentId");
            final String attrTypeValue = TYPE_MAP.get(xxx);
            
            if (attrTypeValue == null)
              {
                log.error("No component for {}", xxx);  
              }
            
            if (componentStack.isEmpty())
              {
                componentId = new Id(attrNameValue);
                final DefaultLayout newComponent = new DefaultLayout(componentId, attrTypeValue);
                componentStack.push(newComponent);
                rootComponent = newComponent;  
              }
            else
              {
                DefaultLayout parentLayout = wrapperLayouts.get(attrNameValue);
                
                if (parentLayout == null)
                  {
                    parentLayout = new DefaultLayout(new Id(attrNameValue), "http://northernwind.tidalwave.it/component/Container/#v1.0"); 
                    wrapperLayouts.put(attrNameValue, parentLayout);
                    componentStack.peek().add(parentLayout);
                  }
                
                // We can't rearrange ids, as subfolders might override this stuff with non-rearranged ids
//                componentName = attrNameValue + "-" + (parentLayout.getChildren().size() + 1);
                componentId = new Id(attrNameValue + "-" + attrIdValue);
                final DefaultLayout newComponent = new DefaultLayout(componentId, attrTypeValue);
                parentLayout.add(newComponent);
                componentStack.push(newComponent);
              }

          }
        else if ("property".equals(elementName))
          {
            String propertyName = toLower(reader.getAttributeValue("", "name"));
            Object propertyValue = reader.getAttributeValue("", "path");
            Object propertyType = reader.getAttributeValue("", "type");
            
            // TODO: if type == contentBinding, you should rather read entityIds from the binding elements as below.
//<property name="styleSheets" path="Layout.css, Typography.css, Forms.css, Tools.css, Horizontal Navigation.css, Layout (Navigation Top, Local Left).css, Tidalwave defaults.css" path_en="Layout.css, Typography.css, Forms.css, Tools.css, Horizontal Navigation.css, Layout (Navigation Top, Local Left).css, Tidalwave defaults.css" type="contentBinding" > 
//    <binding assetKey="" entity="SiteNode" entityId="5" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="6" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="8" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="7" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="9" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="10" > </binding > 
//    <binding assetKey="" entity="SiteNode" entityId="50" > </binding > 
//</property >
            
            if (propertyValue == null)
              {
                propertyValue = reader.getAttributeValue("", "path_en");
              }
            else
              {
                propertyValue = propertyValue.toString().replace("Top, No Local", "Top No Local");
                propertyValue = propertyValue.toString().replace("blueBill Mobile CSS", "blueBill Mobile.css");
                propertyValue = propertyValue.toString().replace("blueBill Mobile Main CSS", "blueBill Mobile Main.css");
                propertyValue = propertyValue.toString().replace("Features Header", "Resources/Features Header");
                propertyValue = propertyValue.toString().replace("Google Analytics", "Resources/Google Analytics");
                propertyValue = propertyValue.toString().replace("Mobile News", "Blog RSS Feed");
                
                if (PROPERTIES_REFERRING_RELATIVE_PATHS.contains(propertyName))
                  {
                    final List<Object> values = new ArrayList<Object>();

                    for (String spl : propertyValue.toString().split(","))
                      {
                        if ("styleSheets".equals(propertyName))
                          {
                            spl = "css/" + spl.trim();  
                          }

                        spl = "/" + spl.trim();
                        spl = spl.replaceAll("/Mobile", "/"); 

                        if ("styleSheets".equals(propertyName))
                          {
                            spl = spl.replace(" ", "-").replace("(", "").replace(")", "");
                          }
                        
                        values.add(spl);
                      }

                    propertyValue = values;
                  }

                if ("styleSheets".equals(propertyName))
                  {
                    propertyName = "screenStyleSheets";  
                  }

                if ("items".equals(propertyName))
                  {
                    propertyName = "links";  
                  }

                if ("content".equals(propertyName))
                  {
                    propertyName = "contents";  
                  }
              }

            properties.put(new Key<Object>(componentId + "." + propertyName), propertyValue);
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        log.trace("processEndElement({})", name);
        
        if ("component".equals(name))
          {
            componentStack.pop();
          }
      }

    @Override
    protected void finish() 
      throws Exception
      {
          // TODO: Infoglue generates a sub-layout even when just properties are changed. We put properties in a
          // separate file, so some of those sub-layouts have to be dropped.
          // Do this:
          //   DefaultLayout parentLayout = ...
          //   DefaultLayout thisLayout = ...
          //   DefaultLayout subLayout = parentLayout.withOverride(thisLayout);
          //   if (parentLayout.equals(subLayout)) then do not produce subLayout
          
        if (rootComponent != null) // might be empty
          {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rootComponent.as(Marshallable.class).marshal(baos);
            baos.close();
            ResourceManager.addCommand(new AddResourceCommand(modifiedDateTime, path, baos.toByteArray(), "No comment"));
            
            if (path.contains("OverrideComponents_"))
              {
                final String nonOverridePath = path.replaceAll("OverrideComponents_", "Components_");
    
                try
                  { 
                    final byte[] nonOverrideComponents  = ResourceManager.findRecentContents(nonOverridePath);
                    log.info("Patching {} with {} ...", nonOverridePath, path);
                    final @Cleanup InputStream is = new ByteArrayInputStream(nonOverrideComponents);
                    Layout nonOverrideLayout = new DefaultLayout(new Id(""), "").as(Unmarshallable.class).unmarshal(is);
                    nonOverrideLayout = (DefaultLayout)nonOverrideLayout.withOverride(rootComponent);
                    final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    rootComponent.as(Marshallable.class).marshal(baos2);
                    baos2.close();
                    ResourceManager.addCommand(new AddResourceCommand(modifiedDateTime, nonOverridePath, baos.toByteArray(), "Patched with OverrideComponents"));
                  }
                catch (NotFoundException e)
                  {
                    log.warn(e.toString());
                  }
              }
          }
      }
  }
