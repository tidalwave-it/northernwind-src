/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
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
public class ComponentParser extends Parser
  {
    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>()
      {{
        put(  "7", "http://northernwind.tidalwave.it/component/BasePage");
        put( "67", "http://northernwind.tidalwave.it/component/Sidebar");
        put("104", "http://northernwind.tidalwave.it/component/NewsIterator");
        put( "36", "http://northernwind.tidalwave.it/component/HorizontalMenu");
        put( "21", "http://northernwind.tidalwave.it/component/HtmlFragment");
        put( "44", "http://northernwind.tidalwave.it/component/HtmlTextWithTitle");
        put("853", "http://northernwind.tidalwave.it/component/StatCounter");
        put("883", "http://northernwind.tidalwave.it/component/Top1000Ranking");
        put("873", "http://northernwind.tidalwave.it/component/AddThis");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
//        put("", "");
      }};
            
    private final StringBuilder builder = new StringBuilder();
    private final SortedMap<String, String> properties;
    private String componentName = "";
    private Component rootComponent;
    private Stack<Component> componentStack = new Stack<Component>();

    public ComponentParser (final @Nonnull String xml, 
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
        if ("components".equals(name) && (indent == 0))
          {
            builder.append("<components>");
          }
        else if ("components".equals(name) && (indent > 0))
          {
//            builder.append("<component>");
          }
        else if ("component".equals(name))
          {
            builder.append("<component ");
            String attrNameValue = "";
            String attrIdValue = "";
            String attrTypeValue = "";

            for (int i = 0; i < reader.getAttributeCount(); i++)
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
            
            componentName = attrNameValue + attrIdValue;
            builder.append(String.format("name='%s' ", componentName));
            builder.append(String.format("type='%s' ", attrTypeValue));
//            builder.append(String.format("name='%s' ", attrValue));
            builder.append(" >");
            final Component newComponent = new Component(componentName, attrTypeValue);
            
            if (componentStack.isEmpty())
              {
                componentStack.push(rootComponent = newComponent);  
              }
            else
              {
                componentStack.peek().add(newComponent);
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
//                    builder.append(String.format("<property name='content' value='${%s}'/>", beanName + ".content"));
                    properties.put(componentName + ".content", attrValue);
                  }
              }
          }
      }

    @Override
    protected void processAttribute (final @Nonnull String name,  final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if (!Arrays.asList("article").contains(name))
          {
            builder.append(String.format("<property name='%s' value='%s'/>", name, "bwlin"));
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        if ("components".equals(name) && (indent == 0))
          {
            builder.append("</components>");
          }
        else if ("components".equals(name) && (indent > 0))
          {
//            builder.append("</list></property>");
          }
        else if ("component".equals(name))
          {
            builder.append("</component>");
            componentStack.pop();
          }
        else 
          {
//                builder.append("</").append(name).append(">");                   
          }
      }

    @Override
    protected void finish() 
      throws Exception
      {
        final ComponentXmlMarshaller marshaller = new ComponentXmlMarshaller(rootComponent);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        marshaller.marshall(pw);
        pw.close();
        ResourceManager.addResource(new Resource(dateTime, path, Utilities.dumpXml(builder.toString())));
        ResourceManager.addResource(new Resource(dateTime, path + "BIS", Utilities.dumpXml(sw.getBuffer().toString())));
      }
  }

