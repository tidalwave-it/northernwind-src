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
package it.tidalwave.northernwind.frontend.model;

import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
        
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
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
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public <T> T getProperty (@Nonnull Key<T> key)
      throws NotFoundException, IOException
      {
        return getProperties().get(key);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public <T> T getProperty (final @Nonnull Key<T> key, final @Nonnull T defaultValue)
      throws IOException
      {
        return getProperties().get(key, defaultValue);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getProperties()
      throws IOException
      {
        log.info("getProperties()");
        final Properties properties = new Properties();
        File f = new File(file, "Resource_en.properties");
        
        if (!f.exists())
          {
            f = new File(file, "OverrideResource_en.properties");
          }
        
        if (!f.exists())
          {
            log.warn("No properties for {}", file);
          }
        else
          {
            log.info(">>>> reading properties from {}...", f.getAbsolutePath());
            @Cleanup final Reader r = new FileReader(f);
            properties.load(r);
            r.close();        

            log.info(">>>> properties: {}", properties);
          }
        
        final Map<Key<?>, Object> map = new HashMap<Key<?>, Object>();
        
        for (final Entry<Object, Object> entry : properties.entrySet())
          {
            map.put(new Key<Object>(entry.getKey().toString()), entry.getValue());
          }
        
        return new ResourceProperties(map);
      }
  }
