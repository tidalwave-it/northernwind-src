/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
public class StructureParser extends Parser
  {
    private final String language;

    public StructureParser (String xml, final @Nonnull DateTime dateTime, String path, String language) 
      throws FileNotFoundException 
      {
        super(xml, path, dateTime);
        this.language = language;
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        if ("ComponentStructure".equals(name))
          {
            try
              {
                new ComponentParser(builder.toString(), dateTime, path + "beans_" + language + ".xml", properties).process();
              }
            catch (Exception e)
              {
                  System.err.println("ERROR: " + e + " ON " + builder);                        
              }
          }
        else if (!"attributes".equals(name))
          {
            properties.put(name, builder.toString()); 
          }
      }

    @Override
    protected void finish() 
      throws UnsupportedEncodingException
      {
        dumpPropertiesAsResourceBundle("Resource_" + language + ".properties");
      }
  }
