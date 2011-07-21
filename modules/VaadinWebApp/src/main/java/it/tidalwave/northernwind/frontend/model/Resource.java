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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.TypeSafeHashMap;
import it.tidalwave.util.TypeSafeMap;
import org.springframework.beans.factory.annotation.Configurable;
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
@Configurable @RequiredArgsConstructor @Slf4j // @ToString
public class Resource 
  {
    @Nonnull @Getter
    private final File file;    
    
    @CheckForNull
    private transient TypeSafeMap properties;

    /*******************************************************************************************************************
     *
     * Retrieves a property.
     * 
     * @param   key                 the property key
     * @return                      the property value
     * @throws  NotFoundException   if the property doesn't exist
     *
     ******************************************************************************************************************/
    @Nonnull
    public <Type> Type getProperty (@Nonnull Key<Type> key)
      throws NotFoundException, IOException
      {
        try
          { 
            return properties.get(key);
          }
        catch (NotFoundException e)
          {
            return (Type)getFileBasedProperty(key.stringValue());
          }
      }

    /*******************************************************************************************************************
     *
     * Retrieves a property, eventualyl returning a default value.
     * 
     * @param   key                 the property key
     * @param   defaultValue        the default value to return when the property doesn't exist
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    public <Type> Type getProperty (final @Nonnull Key<Type> key, final @Nonnull Type defaultValue)
      throws IOException
      {
        try
          { 
            return getProperty(key);
          }
        catch (NotFoundException e)
          {
            return defaultValue;
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getFileBasedProperty (final @Nonnull String attribute)
      throws NotFoundException, IOException
      {
        log.info("getFileBasedProperty({})", attribute);
        final File f = new File(file, attribute);
        
        if (!f.exists())
          {
            throw new NotFoundException(file.getAbsolutePath());  
          }
        
        log.info(">>>> reading from {}", f.getAbsolutePath());
        @Cleanup final FileReader fr = new FileReader(f);
        final char[] chars = new char[(int)f.length()];
        fr.read(chars);
        fr.close();
        
        return new String(chars);
      }  
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void loadProperties()
      throws IOException
      {
        log.info("loadProperties()");
        final Properties tempProperties = new Properties();
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
            tempProperties.load(r);
            r.close();        
            log.info(">>>> properties: {}", tempProperties);
          }

        final Map<Key<?>, Object> map = new HashMap<Key<?>, Object>();

        for (final Entry<Object, Object> entry : tempProperties.entrySet())
          {
            map.put(new Key<Object>(entry.getKey().toString()), entry.getValue());
          }

        properties = new TypeSafeHashMap(map);
      }
  }