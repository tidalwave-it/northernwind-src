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
import lombok.Getter;
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
    @Nonnull @Getter
    private final File file;    
        
    @Nonnull
    public <Type> Type get (final @Nonnull String attribute, final @Nonnull Class<Type> type)
      throws IOException
      {
        log.info("get({}, {})", attribute, type);
        final File f = new File(file, attribute);
        log.info(">>>> reading from {}", f.getAbsolutePath());
        @Cleanup final FileReader fr = new FileReader(f);
        final char[] chars = new char[(int)f.length()];
        fr.read(chars);
        fr.close();
        
        return (Type)new String(chars);
      }  
    
    @Nonnull
    public Properties getProperties()
      throws IOException
      {
        final Properties properties = new Properties();
        File f = new File(file, "Resource_en.properties");
        
        if (!f.exists())
          {
            f = new File(f, "OverrideResource_en.properties");
          }
        
        log.info(">>>> reading properties from {}...", f.getAbsolutePath());
        @Cleanup final Reader r = new FileReader(f);
        properties.load(r);
        r.close();        
        
        return properties;
      }
  }
