/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamReader;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
public class ContentParser extends Parser
  {
    private final String language;

    private boolean inAttributes = false;

    public ContentParser (final @Nonnull String xml, final @Nonnull DateTime dateTime, final @Nonnull String path, final @Nonnull String language) 
      {
        super(xml, path, dateTime);
        this.language = language;
      }

    @Override
    protected void processStartElement (final @Nonnull String name, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("attributes".equals(name))
          {
            inAttributes = true;  
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        if ("attributes".equals(name))
          {
            inAttributes = false;  
          }
        else if (inAttributes)
          {
            if (Arrays.asList("FullText", "Template", "Leadin").contains(name))
              {
                // FIXME: format HTML
                ResourceManager.addResource(new Resource(dateTime, path + name + "_" + language + ".html", builder.toString().getBytes("UTF-8")));
//                    addResource(new Resource(dateTime, path + name + ".html", dumpXml("<body>" + builder.toString() + "</body>")));
              }
            else
              {
                properties.put(name, builder.toString()); 
              }
          }
      }        

    @Override
    protected void finish()
      throws UnsupportedEncodingException
      {
        dumpPropertiesAsResourceBundle("Resource_" + language + ".properties");
      }
  }

