/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.io.IOException;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Identifiable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * A bag of properties for a {@link Resource}s.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ResourceProperties extends As, Identifiable
  {
    /*******************************************************************************************************************
     *
     * A builder of a {@link ResourceProperties}.
     *
     ******************************************************************************************************************/
    @AllArgsConstructor(access = AccessLevel.PRIVATE) @RequiredArgsConstructor
    @Getter @ToString(exclude = "callBack")
    public final class Builder
      {
        // Workaround for a Lombok limitation with Wither and subclasses
        public static interface CallBack
          {
            @Nonnull
            public ResourceProperties build (@Nonnull Builder builder);
          }

        @Nonnull
        private final ModelFactory modelFactory;

        @Nonnull
        private final CallBack callBack;

        @Wither
        private Id id = new Id("");

        @Wither
        private Map<Key<?>, Object> values = Collections.emptyMap();

        @Wither
        private PropertyResolver propertyResolver = PropertyResolver.DEFAULT;

        @Nonnull
        public ResourceProperties build()
          {
            return callBack.build(this);
          }
      }

    public static interface PropertyResolver // FIXME: drop this
      {
        public static PropertyResolver DEFAULT = new PropertyResolver()
          {
            @Override
            public <Type> Type resolveProperty (@Nonnull Id propertyGroupId, @Nonnull Key<Type> key)
              throws NotFoundException, IOException
              {
                throw new NotFoundException();
              }
          };

        @Nonnull
        public <Type> Type resolveProperty (@Nonnull Id propertyGroupId, @Nonnull Key<Type> key)
          throws NotFoundException, IOException;
      }

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
      throws NotFoundException, IOException;

    /*******************************************************************************************************************
     *
     * Retrieves a property, eventually returning a default value.
     *
     * @param   key                 the property key
     * @param   defaultValue        the default value to return when the property doesn't exist
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    public <Type> Type getProperty (@Nonnull Key<Type> key, @Nonnull Type defaultValue)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves a property, eventually returning a default value.
     *
     * @param   key                 the property key
     * @param   defaultValue        the default value to return when the property doesn't exist
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull // FIXME: should be Key<Integer>
    public int getIntProperty (@Nonnull Key<String> key, @Nonnull int defaultValue)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Retrieves a subgroup of properties.
     *
     * @param   id                  the id
     * @return                      the property group
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getGroup (@Nonnull Id id);

    /*******************************************************************************************************************
     *
     * Retrieves the collection of property keys.
     *
     * @return                      the property keys
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<Key<?>> getKeys();

    /*******************************************************************************************************************
     *
     * Retrieves the collection of ids of groups.
     *
     * @return                      the group ids
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<Id> getGroupIds();

    /*******************************************************************************************************************
     *
     * Returns a new instance with an additional property.
     *
     ******************************************************************************************************************/
    @Nonnull
    public <T> ResourceProperties withProperty (@Nonnull Key<T> key, @Nonnull T value);

    /*******************************************************************************************************************
     *
     * Returns a new instance without a property.
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties withoutProperty (@Nonnull Key<?> key);

    /*******************************************************************************************************************
     *
     * Returns a new instance with an additional property group.
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties withProperties (@Nonnull ResourceProperties properties);

    /*******************************************************************************************************************
     *
     * Returns a new instance which is the logical merge with other properties.
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties merged (@Nonnull ResourceProperties properties);

    /*******************************************************************************************************************
     *
     * Returns a clone with a new id.
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties withId (@Nonnull Id id);
  }
