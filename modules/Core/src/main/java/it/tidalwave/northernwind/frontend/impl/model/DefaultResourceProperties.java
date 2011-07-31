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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.TypeSafeMap;
import it.tidalwave.util.TypeSafeHashMap;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
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
    
    @Nonnull
    private final Id id;
    
    @CheckForNull
    private transient TypeSafeMap properties;

    private final Map<Id, ResourceProperties> groups = new HashMap<Id, ResourceProperties>();
    
    @Nonnull
    private final PropertyResolver propertyResolver;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull Id id,
                                      final @Nonnull Map<Key<?>, Object> map,
                                      final @Nonnull PropertyResolver propertyResolver) 
      {
        this.id = id;
        properties = new TypeSafeHashMap(map);
        this.propertyResolver = propertyResolver;
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
        try
          { 
            return properties.get(key);
          }
        catch (NotFoundException e)
          {
            return propertyResolver.resolveProperty(id, key);
          }
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
    
    @Override @Nonnull
    public ResourceProperties getGroup (final @Nonnull Id id)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(groups.get(id), id.stringValue());
      }
  }
