/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Identifiable;
import it.tidalwave.util.TypeSafeMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

/***********************************************************************************************************************
 *
 * A bag of properties for a {@link Resource}s.
 *
 * @author  Fabrizio Giudici
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

        @With
        private Id id = new Id("");

        @With @Deprecated
        private Map<String, Object> values = Collections.emptyMap();

        @With
        private PropertyResolver propertyResolver = PropertyResolver.DEFAULT;

        @Nonnull
        public ResourceProperties build()
          {
            return callBack.build(this);
          }

        /***************************************************************************************************************
         *
         * TODO: deprecate withValues(Map<String, Object>) and rename this to withValues().
         *
         **************************************************************************************************************/
        @Nonnull
        public Builder withSafeValues (final @Nonnull TypeSafeMap values)
          {
            return withValues(values.asMap().entrySet().stream()
                                    .collect(Collectors.toMap(e -> e.getKey().getName(), e -> e.getValue())));
          }
      }

    public static interface PropertyResolver // FIXME: drop this
      {
        public static PropertyResolver DEFAULT = new PropertyResolver()
          {
            @Override
            public <T> T resolveProperty (@Nonnull Id propertyGroupId, @Nonnull Key<T> key)
              throws NotFoundException, IOException
              {
                throw new NotFoundException(key.stringValue());
              }
          };

        @Nonnull
        public <T> T resolveProperty (@Nonnull Id propertyGroupId, @Nonnull Key<T> key)
          throws NotFoundException, IOException;
      }

    /*******************************************************************************************************************
     *
     * Retrieves a property.
     *
     * @param   key                 the property key
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    public <T> Optional<T> getProperty (@Nonnull Key<T> key);

    /*******************************************************************************************************************
     *
     * Retrieves a property, searching through a sequence of keys.
     *
     * @param   keys                the property keys
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    default public <T> Optional<T> getProperty (final @Nonnull List<Key<T>> keys)
      {
        return keys.stream().flatMap(key -> getProperty(key).map(Stream::of).orElseGet(Stream::empty)).findFirst(); // FIXME: simplify in Java 9
      }

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
