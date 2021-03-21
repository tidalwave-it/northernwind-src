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
import java.util.List;
import java.util.Optional;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

/***********************************************************************************************************************
 *
 * A resource is the basic entity of NorthernWind. It's something located in the filesystem and represented by a file
 * or a folder, with a bag of properties.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface Resource extends As
  {
    public static final Class<Resource> _Resource_ = Resource.class;

    /*******************************************************************************************************************
     *
     * A builder of a {@link Resource}.
     *
     ******************************************************************************************************************/
    @AllArgsConstructor(access = AccessLevel.PRIVATE) @RequiredArgsConstructor
    @Getter @ToString(exclude = "callBack")
    public final class Builder
      {
        // Workaround for a Lombok limitation with Wither and subclasses
        @FunctionalInterface
        public static interface CallBack
          {
            @Nonnull
            public Resource build (@Nonnull Builder builder);
          }

        @Nonnull
        private final ModelFactory modelFactory;

        @Nonnull
        private final CallBack callBack;

        @With
        private ResourceFile file;

        @Nonnull
        public Resource build()
          {
            return callBack.build(this);
          }
      }

    /** The local portion of relativeUri by which a resource is exposed to the web. If this property is not
     *  defined, the resource uses a reasonable default. */
    public static final Key<String> P_EXPOSED_URI = Key.of("exposedUri", String.class);

    /** This property, controls whether this resource is a placeholder. See {@link #isPlaceHolder} for more information
     */
    public static final Key<Boolean> P_PLACE_HOLDER = Key.of("placeHolder", Boolean.class);

    /*******************************************************************************************************************
     *
     * Returns the file backing this resource. It can be a plain file or a directory in function of the resource type.
     *
     * @return  the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFile getFile();

    /*******************************************************************************************************************
     *
     * Returns the properties of this resource.
     *
     * @return  the properties
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getProperties();

    /*******************************************************************************************************************
     *
     * Shortcut for {@code getProperties().getProperty(key)}.
     *
     * @param   key     the key
     * @return          the property
     *
     ******************************************************************************************************************/
    @Nonnull
    public default <T> Optional<T> getProperty (@Nonnull Key<T> key)
      {
        return getProperties().getProperty(key);
      }

    /*******************************************************************************************************************
     *
     * Shortcut for {@code getProperties().getProperty(keys)}.
     *
     * @param   keys    the keys
     * @return          the property
     *
     ******************************************************************************************************************/
    @Nonnull
    public default <T> Optional<T> getProperty (@Nonnull List<Key<T>> keys)
      {
        return getProperties().getProperty(keys);
      }

    /*******************************************************************************************************************
     *
     * Returns the property group of this resources with the given id. Empty properties are returned when id doesn't
     * match.
     *
     * @param   id                  the id of the property group
     * @return                      the properties
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getPropertyGroup (@Nonnull Id id);

    /*******************************************************************************************************************
     *
     * A placeholder resource doesn't contain anything, it just provides a placeholder for a path element. For instance,
     * if in the pair parent/child child is a placeholder, the relative URI /parent/child will be mapped to parent
     * (which supposedly manages path params). This is useful for processing REST path params, for instance.
     *
     * @return  {@code true} if this resource is a placeholder
     *
     ******************************************************************************************************************/
    public boolean isPlaceHolder();
  }
