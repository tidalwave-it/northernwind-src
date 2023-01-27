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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.metadata.Directory;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.IPTC;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolator.Context;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyList;
import static it.tidalwave.util.FunctionalCheckedExceptionWrappers.*;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link Metadata}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @AllArgsConstructor @ToString @Slf4j
class DefaultMetadata implements Metadata
  {
    @Nonnull
    private final String mediaName;

    @Nonnull
    private final EditableImage image;

    @Inject
    private MetadataInterpolatorFactory interpolatorFactory;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultMetadata (@Nonnull final String mediaName, @Nonnull final EditableImage image)
      {
        this.mediaName = mediaName;
        this.image = image;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T extends Directory> T getDirectory (@Nonnull final Class<T> metadataClass)
      {
        return image.getMetadata(metadataClass).orElseGet(_s(() -> metadataClass.getConstructor().newInstance()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String interpolateString (@Nonnull final String template,
                                     @Nonnull final ResourceProperties properties)
      {
        if (log.isDebugEnabled())
          {
            log();
          }

        // FIXME: use format as an interpolated string to get properties both from EXIF and IPTC
        //            final String string = formatted(iptc.getObject(517, String.class));
        final var context = new Context(this, getMap(properties, P_CAMERA_IDS), getMap(properties, P_LENS_IDS));

        var result = template;

        for (final var interpolator : interpolatorFactory.getInterpolators())
          {
            if (result.contains("$" + interpolator.getMacro() + "$"))
              {
                result = interpolator.interpolate(result, context);
              }
          }

        return result;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void log()
      {
        final var tiff = image.getMetadata(TIFF.class).orElseGet(TIFF::new);
        final var exif = image.getMetadata(EXIF.class).orElseGet(EXIF::new);
        final var iptc = image.getMetadata(IPTC.class).orElseGet(IPTC::new);
        final var xmp = image.getMetadata(XMP.class).orElseGet(XMP::new);
        final Map<String, String> xmpProperties = new TreeMap<>(xmp.getXmpProperties());

        tiff.forEachTag(t -> log.debug("{}: TIFF[{}]: {}", mediaName, t.getName(), tiff.getRaw(t.getCode())));
        exif.forEachTag(t -> log.debug("{}: EXIF[{}]: {}", mediaName, t.getName(), exif.getRaw(t.getCode())));
        iptc.forEachTag(t -> log.debug("{}: IPTC[{}]: {}", mediaName, t.getName(), iptc.getRaw(t.getCode())));
        xmp.forEachTag(t -> log.debug("{}: XMP[{}]: {}", mediaName, t.getName(), xmp.getRaw(t.getCode())));
        xmpProperties.forEach((k, v) -> log.debug("XMPprop({}).{}: {}", mediaName, k, v));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Map<String, String> getMap (@Nonnull final ResourceProperties siteNodeProperties,
                                               @Nonnull final Key<List<String>> key)
      {
        final var properties = siteNodeProperties.getGroup(P_GROUP_ID);
        final Map<String, String> lensMap = new HashMap<>();

        for (final var s : properties.getProperty(key).orElse(emptyList()))
          {
            final var split = s.split(":");
            lensMap.put(split[0].trim(), split[1].trim());
          }

        return lensMap;
      }
  }
