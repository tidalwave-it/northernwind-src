/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import it.tidalwave.util.Key;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
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
        final String s = builder.toString();
        
        if ("ComponentStructure".equals(name))
          {
            try
              {
                new LayoutConverter(s, dateTime, path + "Layout_" + language + ".xml", properties).process();
              }
            catch (Exception e)
              {
                  System.err.println("ERROR: " + e + " ON " + builder);                        
              }
          }
        else if (!Arrays.asList("attributes", "article").contains(name) &!s.equals("_Standard Pages"))
          {
            properties.put(new Key<Object>(name), s); 
          }
      }

    @Override
    protected void finish() 
      throws IOException
      {
        dumpProperties("Properties_" + language);
      }
  }
