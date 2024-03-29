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
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.aspect.DebugProfiling;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * An implementation of {@link MediaMetadataProvider} which retrieves metadata from embedded data inside media files.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class EmbeddedMediaMetadataProvider implements MediaMetadataProvider
  {
    public static final Key<List<String>> P_CAMERA_IDS = new Key<>("cameraIds") {};

    public static final Key<List<String>> P_LENS_IDS = new Key<>("lensIds") {};

    public static final Key<List<String>> P_MEDIA_PATHS = new Key<>("mediaPaths") {};

    public static final Id P_GROUP_ID = new Id("EmbeddedMediaMetadataProvider");

    @Inject
    private MetadataCache metadataCache;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // FIXME: should use the Metadata API of blueMarine, but we have first to make it work with Spring and its DI.
    @Override @Nonnull @DebugProfiling(message = "metadata retrieved")
    public String getMetadataString (@Nonnull final Id mediaId,
                                     @Nonnull final String template,
                                     @Nonnull final ResourceProperties properties)
      {
        try
          {
            final var metadata = metadataCache.findMetadataById(mediaId, properties);
            return metadata.interpolateString(template, properties);
          }
        catch (NotFoundException e)
          {
            log.warn("Cannot find media for id {}: {}", mediaId, e.toString());
            return "";
          }
        catch (IOException e)
          {
            log.warn("Unexpected I/O error for id {}: {}", mediaId, e.toString());
            return "";
          }
      }
  }
