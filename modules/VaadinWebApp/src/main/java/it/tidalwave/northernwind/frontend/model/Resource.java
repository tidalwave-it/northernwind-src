/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.frontend.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import javax.annotation.Nonnull;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class Resource 
  {
    @Nonnull
    private final File path;    
    
    @Nonnull
    public <Type> Type get (final @Nonnull String attribute, final @Nonnull Class<Type> type)
      throws IOException
      {
        log.info("get({}, {})", attribute, type);
        final File file = new File(path, attribute);
        log.info(">>>> reading from {}", file.getAbsolutePath());
        @Cleanup final FileReader fr = new FileReader(file);
        final char[] chars = new char[(int)file.length()];
        fr.read(chars);
        fr.close();
        
        return (Type)new String(chars);
      }  
    
    @Nonnull
    public Properties getProperties()
      throws IOException
      {
        final Properties properties = new Properties();
        File file = new File(path, "Resource_en.properties");
        
        if (!file.exists())
          {
            file = new File(path, "OverrideResource_en.properties");
          }
        
        log.info(">>>> reading properties from {}...", file.getAbsolutePath());
        @Cleanup final Reader r = new FileReader(file);
        properties.load(r);
        r.close();        
        
        return properties;
      }
  }
