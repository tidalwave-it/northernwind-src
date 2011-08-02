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
package it.tidalwave.northernwind.frontend.impl.model;

import java.util.Collection;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ResourceProperties}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString(exclude={"propertyResolver"})
public class DefaultResourceProperties implements ResourceProperties 
  {
    public static interface PropertyResolver
      {
        @Nonnull
        public <Type> Type resolveProperty (@Nonnull Id propertyGroupId, @Nonnull Key<Type> key)
          throws NotFoundException, IOException;
      }
    
    @Nonnull @Getter
    private final Id id;
    
    private final Map<Key<?>, Object> properties = new HashMap<Key<?>, Object>();

    private final Map<Id, ResourceProperties> groups = new HashMap<Id, ResourceProperties>();
    
    @Nonnull
    private final PropertyResolver propertyResolver;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull Id id,
                                      final @Nonnull PropertyResolver propertyResolver) 
      {
        this.id = id;
        this.propertyResolver = propertyResolver;
      }
    
    /*******************************************************************************************************************
     *
     * Legacy code for converting from flat-style properties.
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull Id id,
                                      final @Nonnull Map<Key<?>, Object> map,
                                      final @Nonnull PropertyResolver propertyResolver) 
      {
        this.id = id;
        this.propertyResolver = propertyResolver;
        
        final Map<Id, Map<Key<?>, Object>> othersMap = new HashMap<Id, Map<Key<?>, Object>>();
        
        for (final Entry<Key<?>, Object> entry : map.entrySet())
          {
            final String s = entry.getKey().stringValue();
            final Object value = entry.getValue();
            
            if (!s.contains("."))
              {
                properties.put(new Key<Object>(s), value);  
              }
            else
              {
                final String[] x = s.split("\\.");
                final Id groupId = new Id(x[0]);
                
                Map<Key<?>, Object> otherMap = othersMap.get(groupId);
                
                if (otherMap == null)
                  {
                    otherMap = new HashMap<Key<?>, Object>();
                    othersMap.put(groupId, otherMap);  
                  }
                
                otherMap.put(new Key<Object>(x[1]), value);
              }
          }
        
        for (final Entry<Id, Map<Key<?>, Object>> entry : othersMap.entrySet())
          {
            groups.put(entry.getKey(), new DefaultResourceProperties(entry.getKey(), entry.getValue(), propertyResolver));
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <Type> Type getProperty (@Nonnull Key<Type> key)
      throws NotFoundException, IOException
      {
        final Type value = (Type)properties.get(key);
        return (value != null) ? value : propertyResolver.resolveProperty(id, key);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getGroup (final @Nonnull Id id)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(groups.get(id), id.stringValue());
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Collection<Key<?>> getKeys() 
      {
        return new CopyOnWriteArrayList<Key<?>>(properties.keySet());
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Collection<Id> getGroupIds() 
      {
        return new CopyOnWriteArrayList<Id>(groups.keySet());
      }
    
    @Nonnull
    public DefaultResourceProperties withProperty (final @Nonnull Key<Object> key, final @Nonnull Object value)
      {
        properties.put(key, value);
        return this; // TODO: should clone
      }
    
    @Nonnull
    public DefaultResourceProperties withProperties (final @Nonnull ResourceProperties properties)
      {
        groups.put(properties.getId(), properties);
        return this; // FIXME: should clone
      }
  }
