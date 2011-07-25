/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.util.Id;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamReader;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
public class LayoutConverter extends Parser
  {
    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>()
      {{
        put(  "7", "http://northernwind.tidalwave.it/component/NodeContainer");
        put( "67", "http://northernwind.tidalwave.it/component/Sidebar");
        put("104", "http://northernwind.tidalwave.it/component/Blog");
        put( "36", "http://northernwind.tidalwave.it/component/HorizontalMenu");
        put( "21", "http://northernwind.tidalwave.it/component/HtmlFragment");
        put( "44", "http://northernwind.tidalwave.it/component/HtmlTextWithTitle");
        put("853", "http://northernwind.tidalwave.it/component/StatCounter");
        put("883", "http://northernwind.tidalwave.it/component/Top1000Ranking");
        put("873", "http://northernwind.tidalwave.it/component/AddThis");
//        put("", "");
      }};
            
    private final SortedMap<String, String> properties;
    private Id componentId;
    private DefaultLayout rootComponent;
    private final Stack<DefaultLayout> componentStack = new Stack<DefaultLayout>();
    private final Map<String, DefaultLayout> wrapperLayouts = new HashMap<String, DefaultLayout>();

    public LayoutConverter (final @Nonnull String xml, 
                            final @Nonnull DateTime dateTime, 
                            final @Nonnull String path,
                            final @Nonnull SortedMap<String, String> properties) 
      {
        super(xml, path, dateTime);
        this.properties = properties;
      }

    @Override
    protected void processStartElement (final @Nonnull String name,  final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("component".equals(name))
          {
            String attrNameValue = "";
            String attrIdValue = "";
            String attrTypeValue = "";

            for (int i = 0; i < reader.getAttributeCount(); i++) // FIXME: use reader.getAttributeValue(String, String)
              {
                final String attrName = reader.getAttributeName(i).getLocalPart();
                final String attrValue = reader.getAttributeValue(i);

                if ("name".equals(attrName))
                  {
                    attrNameValue = attrValue;
                  }
                else if ("id".equals(attrName))
                  {
                    attrIdValue = attrValue;
                  }
                else if ("contentId".equals(attrName))
                  {
                    attrTypeValue = TYPE_MAP.get(attrValue);
                  }
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
                    parentLayout = new DefaultLayout(new Id(attrNameValue), "http://northernwind.tidalwave.it/component/Container"); 
                    wrapperLayouts.put(attrNameValue, parentLayout);
                    componentStack.peek().add(parentLayout);
                  }
                
                // We can't rearrange ids, as subfolders might override this stuff with non-rearranged ids
//                componentName = attrNameValue + "-" + (parentLayout.getChildren().size() + 1);
                componentId = new Id(attrNameValue + "-" + attrIdValue);
                final DefaultLayout newComponent = new DefaultLayout(componentId, attrTypeValue);
                
                if (!"content3-3".equals(componentId)) // FIXME: temp patch until we recover all the properties so this is a blog navigator...
                  {
                    parentLayout.add(newComponent);
                  }
                componentStack.push(newComponent);
              }

          }
        else if ("property".equals(name))
          {
            for (int i = 0; i < reader.getAttributeCount(); i++)
              {
                final String attrName = reader.getAttributeName(i).getLocalPart();
                final String attrValue = reader.getAttributeValue(i);

                if ("path".equals(attrName))
                  {
                    properties.put(componentId + ".content", attrValue);
                  }
//                else FIXME: there are missing properties (e.g. set Blog max posts, etc...)
//                  {
//                    properties.put(componentName + "." + attrName, attrValue);
//                  }
              }
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
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
        final LayoutXmlMarshaller marshaller = new LayoutXmlMarshaller(rootComponent);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        marshaller.marshall(pw);
        pw.close();
        ResourceManager.addResource(new Resource(dateTime, path, Utilities.dumpXml(sw.getBuffer().toString())));
      }
  }

