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
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceProperties;

/***********************************************************************************************************************
 *
 * Implementations of this interface provide metadata for media rendering.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface MediaMetadataProvider
  {
    /*******************************************************************************************************************
     *
     * A default implementation that always returns an empty string.
     *
     ******************************************************************************************************************/
    public static final MediaMetadataProvider VOID = new MediaMetadataProvider()
      {
        @Override
        public String getMetadataString (final @Nonnull Id id,
                                         final @Nonnull String format,
                                         final @Nonnull ResourceProperties properties)
          {
            return format;
          }
      };

    /*******************************************************************************************************************
     *
     * Retrieves metadata items and formats them.
     *
     * @param  mediaId     the id of the media item to retrieve metadata from
     * @param  format      the format string for metadata
     * @param  properties  some configuration properties
     * @return             the formatted metadata
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getMetadataString (@Nonnull Id mediaId,
                                     @Nonnull String format,
                                     @Nonnull ResourceProperties properties);
  }
