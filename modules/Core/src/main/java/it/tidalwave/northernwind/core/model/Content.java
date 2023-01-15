/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Optional;
import java.util.List;
import java.time.ZonedDateTime;
import it.tidalwave.util.Key;
import it.tidalwave.role.SimpleComposite;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

/***********************************************************************************************************************
 *
 * A piece of content to be used by something.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface Content extends Resource, SimpleComposite<Content>
  {
    @SuppressWarnings("squid:S1700")
    public static final Class<Content> _Content_ = Content.class;

    /** The title of this {@code Content}. */
    public static final Key<String> P_TITLE = Key.of("title", String.class);

    /** The unique id of this {@code Content}. */
    public static final Key<String> P_ID = Key.of("id", String.class);

    /** The full text contained in this {@code Content}. */
    public static final Key<String> P_FULL_TEXT = Key.of("fullText", String.class);

    /** A shortened text contained in this {@code Content}. */
    public static final Key<String> P_LEADIN_TEXT = Key.of("leadinText", String.class);

    /** A description this {@code Content}. */
    public static final Key<String> P_DESCRIPTION = Key.of("description", String.class);

    /** A contained that works as a template for something. */
    public static final Key<String> P_TEMPLATE = Key.of("template", String.class);

    /** The creation date of this {@code Content}. */
    public static final Key<ZonedDateTime> P_CREATION_DATE = Key.of("creationDateTime", ZonedDateTime.class);

    /** The latest modification date of this {@code Content}. */
    public static final Key<ZonedDateTime> P_LATEST_MODIFICATION_DATE = Key.of("latestModificationDateTime", ZonedDateTime.class);

    /** The publishing date of this {@code Content}. */
    public static final Key<ZonedDateTime> P_PUBLISHING_DATE = Key.of("publishingDateTime", ZonedDateTime.class);

    /** A collection of tags associated with this {@code Content}. */
    public static final Key<List<String>> P_TAGS = new Key<>("tags") {};

    /*******************************************************************************************************************
     *
     * A builder of a {@link Content}.
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
            public Content build (@Nonnull Builder builder);
          }

        @Nonnull
        private final ModelFactory modelFactory;

        @Nonnull
        private final CallBack callBack;

        @With
        private ResourceFile folder;

        @Nonnull
        public Content build()
          {
            return callBack.build(this);
          }
      }

    /*******************************************************************************************************************
     *
     * Returns the exposed URI mapped to this resource.
     *
     * @return  the exposed URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<ResourcePath> getExposedUri();
  }
