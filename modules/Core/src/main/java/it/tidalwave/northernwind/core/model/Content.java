/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Optional;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.SimpleComposite;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a page.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface Content extends Resource, SimpleComposite<Content>
  {
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
        public static interface CallBack
          {
            @Nonnull
            public Content build (@Nonnull Builder builder);
          }

        @Nonnull
        private final ModelFactory modelFactory;

        @Nonnull
        private final CallBack callBack;

        @Wither
        private ResourceFile folder;

        @Nonnull
        public Content build()
          {
            return callBack.build(this);
          }
      }

    public static final Class<Content> Content = Content.class;

    /*******************************************************************************************************************
     *
     * Returns the exposed URI mapped to this resource.
     *
     * @return  the exposed URI
     * @throws  NotFoundException  if the resource can't be found
     * @throws  IOException        if an I/O error occurs (??? FIXME)
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<ResourcePath> getExposedUri();

    /*******************************************************************************************************************
     *
     * Returns the exposed URI mapped to this resource.
     *
     * @deprecated  Use {@link #getExposedUri()} instead.
     * @return  the exposed URI
     * @throws  NotFoundException  if the resource can't be found
     * @throws  IOException        if an I/O error occurs (??? FIXME)
     *
     ******************************************************************************************************************/
    @Nonnull @Deprecated
    public ResourcePath getExposedUri2()
      throws NotFoundException, IOException;
  }
