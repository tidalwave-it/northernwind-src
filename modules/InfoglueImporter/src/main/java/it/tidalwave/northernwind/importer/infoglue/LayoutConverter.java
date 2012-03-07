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
import java.lang.reflect.Field;
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
    private static final String CONTAINER = "http://northernwind.tidalwave.it/component/Container/#v1.0";
    private static final String BLOG = "http://northernwind.tidalwave.it/component/Blog/#v1.0";
    private static final String CALENDAR = "http://northernwind.tidalwave.it/component/Calendar/#v1.0";

    private static final List<String> PATH_PARAMS_COMPONENTS = Arrays.asList(BLOG, CALENDAR);
    
    private static final List<String> PROPERTIES_REFERRING_RELATIVE_PATHS = Arrays.asList
      (
        "styleSheets", "items", "content", "contents", "rssFeeds", "inlinedScripts"
      );
    
    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>()
      {{
        put(  "7", "http://northernwind.tidalwave.it/component/NodeContainer/#v1.0");
        put( "21", "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0");
        put( "36", "http://northernwind.tidalwave.it/component/HorizontalMenu/#v1.0");
        put( "44", "http://northernwind.tidalwave.it/component/HtmlTextWithTitle/#v1.0");
        put( "67", "http://northernwind.tidalwave.it/component/Sidebar/#v1.0");
        put( "71", "http://northernwind.tidalwave.it/component/RssFeed/#v1.0");
        put( "89", "http://northernwind.tidalwave.it/component/BreadCrumb/#v1.0");
        put("104", BLOG);
        put("853", "http://northernwind.tidalwave.it/component/StatCounter/#v1.0");
        put("873", "http://northernwind.tidalwave.it/component/AddThis/#v1.0");
        put("883", "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0");

        put("509", "http://northernwind.tidalwave.it/component/StoppingDownCSS/#v1.0"); // StoppingDown CSS
        put("513", "http://northernwind.tidalwave.it/component/StatCounter/#v1.0"); 
        put("544", "http://northernwind.tidalwave.it/component/NodeContainer/#v1.0"); // Splash
        put("547", CALENDAR);
        put("572", CONTAINER); // Categories header
        put("577", "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0"); // Nikonian WebRing Badge
        put("602", CONTAINER); // Post Index header
        put("603", "http://northernwind.tidalwave.it/component/Unknown603/#v1.0");
        put("764", "http://northernwind.tidalwave.it/component/LightBoxCSS/#v1.0"); // LightBox CSS
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
            final String contentId = reader.getAttributeValue("", "contentId");
            final String attrTypeValue = TYPE_MAP.get(contentId);
            
            if (attrTypeValue == null)
              {
                log.error("No component for {}", contentId);  
              }
            
//            // FIXME: for StoppingDown
//            if ("544".equals(contentId))
//              {
//                properties.put(new Key<Object>("base.template"), "/Splash");
//              }
//            // END FIXME: for StoppingDown
            
            if (PATH_PARAMS_COMPONENTS.contains(attrTypeValue))
              {
                properties.put(new Key<Object>("managesPathParams"), "true");
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
                    parentLayout = new DefaultLayout(new Id(attrNameValue), CONTAINER); 
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
//            Object propertyType = reader.getAttributeValue("", "type");
            
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
                
                if ("--".equals(propertyValue.toString()))
                  {
                    propertyValue = "";
                  }
              }
            else
              {
                propertyValue = propertyValue.toString().replace("Top, No Local", "Top No Local");
                propertyValue = propertyValue.toString().replace("Top, Local", "Top Local");
                
                // FIXME: for blueBill Mobile
//                propertyValue = propertyValue.toString().replace("blueBill Mobile CSS", "blueBill Mobile.css");
//                propertyValue = propertyValue.toString().replace("blueBill Mobile Main CSS", "blueBill Mobile Main.css");
//                propertyValue = propertyValue.toString().replace("Features Header", "Resources/Features Header");
//                propertyValue = propertyValue.toString().replace("Google Analytics", "Resources/Google Analytics");
//                propertyValue = propertyValue.toString().replace("Mobile News", "Blog RSS Feed");
                // END FIXME: for blueBill Mobile

                propertyValue = propertyValue.toString().replace("Layout Navigation Top", "Layout Navigation Top.css");
                propertyValue = propertyValue.toString().replace("StoppingDown CSS", "StoppingDown.css");
                propertyValue = propertyValue.toString().replace("LightBox Override CSS", "LightBox Override.css");
                propertyValue = propertyValue.toString().replace("Google Analytics", "Resources/Google Analytics");
                propertyValue = propertyValue.toString().replace("Album instructions", "Resources/Album instructions");
                propertyValue = propertyValue.toString().replace("Photographers", "Resources/Photographers");
                propertyValue = propertyValue.toString().replace("Blog RSS Feed", "RSS Feeds/Blog RSS Feed");
                propertyValue = propertyValue.toString().replace("News RSS Feed", "RSS Feeds/News RSS Feed");
//                propertyValue = propertyValue.toString().replace("News RSS Feed", "Resources/Feed Panel");
                propertyValue = propertyValue.toString().replace("Nikonian WebRing badge", "Resources/Nikonian WebRing badge");
                
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
//                        spl = spl.replaceAll("/Mobile", "/"); // blueBill Mobile
                        
                        if ("content3-3".equals(componentId.stringValue())) // StoppingDown
                          {
                            spl = "/Blog" + spl;  
                          }

                        if ("styleSheets".equals(propertyName))
                          {
                            spl = spl.replace(" ", "-").replace("(", "").replace(")", "");
                          }
                        
                        values.add(spl);
                      }

                    propertyValue = values;
                  }
                
                // FIXME: for StoppingDown
//                if (!componentId.stringValue().contains("main") && path.contains("/Blog") && !path.contains("/Blog/"))
//                  {
//                    properties.put(new Key<Object>(componentId + "." + "index"), "true");
//                  }
//                if (componentId.stringValue().contains("main") && path.contains("/Blog") && !path.contains("/Blog/"))
//                  {
//                    final List<String> scripts = Arrays.asList
//                      (
//                        "/js/prototype.js",
//                        "/js/scriptaculous.js?load=effects,builder",
//                        "/js/lightbox.js"
//                      );
//                    final List<String> css = Arrays.asList
//                      (
//                        "/css/Layout.css",
//                        "/css/Typography.css",
//                        "/css/Forms.css",
//                        "/css/Tools.css",
//                        "/css/Horizontal-Navigation.css",
//                        "/css/Layout-Navigation-Top-Local-Right.css",
//                        "/css/StoppingDown.css",
//                        "/css/lightbox.css",
//                        "/css/LightBox-Override.css"
//                      );
//
//                    properties.put(new Key<Object>("base.scripts"), scripts);
//                    properties.put(new Key<Object>("base.screenStyleSheets"), css);
//                  }
//                // END FIXME: for StoppingDown

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

            if (!StructureParser.IGNORED_PROPERTIES.contains(propertyName))
              { 
                // FIXME: for StoppingDown
                if ("content1-6".equals(componentId.stringValue()) && "contents".equals(propertyName))
                  {
                    propertyValue = Arrays.asList("/Resources/Feed Panel");  
                  }
                // END FIXME
                
                properties.put(new Key<Object>(componentId + "." + propertyName), propertyValue);
              }
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
            // blueBill Mobile
//            try
//              {                
//                rootComponent.findSubComponentById(new Id("main"))
//                             .findSubComponentById(new Id("main-8"));
//                properties.put(new Key<Object>("main-8.contents"), Arrays.asList("/Resources/Top 1000 Ranking"));
//              }
//            catch (NotFoundException e)
//              {
//                // ok  
//              }
//            try
//              {                
//                rootComponent.findSubComponentById(new Id("main"))
//                             .findSubComponentById(new Id("main-9"));
//                properties.put(new Key<Object>("main-9.url"), "http://bluebill.tidalwave.it/mobile/");
//                properties.put(new Key<Object>("main-9.userName"), "fabriziogiudici");
//              }
//            catch (NotFoundException e)
//              {
//                // ok  
//              }
//            try
//              {                
//                rootComponent.findSubComponentById(new Id("footer"))
//                             .findSubComponentById(new Id("footer-7"));
//                properties.put(new Key<Object>("footer-7.project"), "5834368");
//                properties.put(new Key<Object>("footer-7.security"), "91675212");
//              }
//            catch (NotFoundException e)
//              {
//                // ok  
//              }
            // end blueBill Mobile

            try
              {                
                rootComponent.findSubComponentById(new Id("local"))
                             .findSubComponentById(new Id("local-5"))
                             .findSubComponentById(new Id("content1"))
                             .findSubComponentById(new Id("content1-9"));
                properties.put(new Key<Object>("content1-9.project"), "4204333");
                properties.put(new Key<Object>("content1-9.security"), "b11c31c8");
                properties.put(new Key<Object>("content1-9.invisible"), "false");
                properties.put(new Key<Object>("content1-9.partition"), "48");
                properties.put(new Key<Object>("content1-9.clickStat"), "1");
                properties.put(new Key<Object>("content1-9.text"), "4");
                properties.put(new Key<Object>("content1-9.message"), "{0} visits since November 2008.");
              }
            catch (NotFoundException e)
              {
                // ok  
              }
            try
              {                
                rootComponent.findSubComponentById(new Id("local"))
                             .findSubComponentById(new Id("local-2"))
                             .findSubComponentById(new Id("content5"))
                             .findSubComponentById(new Id("content5-7"));
                properties.put(new Key<Object>("content5-7.title"), "Post index");
              }
            catch (NotFoundException e)
              {
                // ok  
              }
            try
              {                
                rootComponent.findSubComponentById(new Id("local"))
                             .findSubComponentById(new Id("local-2"))
                             .findSubComponentById(new Id("content3"))
                             .findSubComponentById(new Id("content3-3"));
                properties.put(new Key<Object>("content3-3.title"), "Categories");
              }
            catch (NotFoundException e)
              {
                // ok  
              }
//            try
//              {                
//                rootComponent.findSubComponentById(new Id("local"))
//                             .findSubComponentById(new Id("local-5"))
//                             .findSubComponentById(new Id("content6"))
//                             .findSubComponentById(new Id("content6-13"));
//                properties.put(new Key<Object>("content6-13.contents"), Arrays.asList("/Resources/Nikonian WebRing badge"));
//              }
//            catch (NotFoundException e)
//              {
//                // ok  
//              }
            try // move footer at the bottom of base
              {                
                final Layout footer = rootComponent.findSubComponentById(new Id("footer"));
                final Field children = DefaultLayout.class.getDeclaredField("children");
                final Field childrenMapById = DefaultLayout.class.getDeclaredField("childrenMapById");
                children.setAccessible(true);
                childrenMapById.setAccessible(true);
                ((List)children.get(rootComponent)).remove(footer);
                ((Map)childrenMapById.get(rootComponent)).remove("footer");
                ((DefaultLayout)rootComponent).add(footer);
              }
            catch (NotFoundException e)
              {
                // ok  
              }
            
            if (path.contains("/Blog/"))
              {
                return;
              }  
            
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
