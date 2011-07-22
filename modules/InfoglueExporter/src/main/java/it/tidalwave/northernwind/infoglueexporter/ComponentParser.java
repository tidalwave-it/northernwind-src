/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.util.Arrays;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamReader;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
public class ComponentParser extends Parser
  {
    private final StringBuilder builder = new StringBuilder();
    private final SortedMap<String, String> properties;
    private String beanName = "";

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
            builder.append("<beans>");
          }
        else if ("components".equals(name) && (indent > 0))
          {
            builder.append("<property name='subcomponents'><list>");
          }
        else if ("component".equals(name))
          {
            builder.append("<bean ");

            for (int i = 0; i < reader.getAttributeCount(); i++)
              {
                final String attrName = reader.getAttributeName(i).getLocalPart();
                    final String attrValue = reader.getAttributeValue(i);
                    
                    if ("id".equals(attrName))
                      {
                        builder.append(String.format("id='%s' ", attrValue));
                      }
                    else if ("contentId".equals(attrName))
                      {
                        builder.append(String.format("class='%s' ", attrValue));
                      }
                  }
                
                builder.append(" >");
                
                for (int i = 0; i < reader.getAttributeCount(); i++)
                  {
                    final String attrName = reader.getAttributeName(i).getLocalPart();
                    final String attrValue = reader.getAttributeValue(i);
                    
                    if ("name".equals(attrName))
                      {
                        beanName = attrValue;
                      }
                    
                    if (!Arrays.asList("id", "contentId").contains(attrName))
                      {
                        builder.append(String.format("<property name='%s' value='%s'/>", attrName, attrValue));
                      }
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
                        builder.append(String.format("<property name='content' value='${%s}'/>", beanName + ".content"));
                        properties.put(beanName + ".content", "/content/document/" + attrValue);
                      }
                  }
              }
          }
                
        @Override
        protected void processAttribute (final @Nonnull String name,  final @Nonnull XMLStreamReader reader)
          throws Exception
          {
            builder.append(String.format("<property name='%s' value='%s'/>", name, "bwlin"));
          }
        
        @Override
        protected void processEndElement (final @Nonnull String name)
          throws Exception
          {
            if ("components".equals(name) && (indent == 0))
              {
                builder.append("</beans>");
              }
            else if ("components".equals(name) && (indent > 0))
              {
                builder.append("</list></property>");
              }
            else if ("component".equals(name))
              {
                builder.append("</bean>");
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
            ResourceManager.addResource(new Resource(dateTime, path, Utilities.dumpXml(builder.toString())));
          }
      }
    
