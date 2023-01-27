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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.Optional;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class ShootingDataInterpolator extends MetadataInterpolatorSupport
  {
    public ShootingDataInterpolator()
      {
        super("shootingData");
      }

    @Override @Nonnull
    public String interpolate (@Nonnull final String template, @Nonnull final Context context)
      {
        final var tiff = context.getMetadata().getDirectory(TIFF.class);
        final var exif = context.getMetadata().getDirectory(EXIF.class);
        final var xmp = context.getMetadata().getDirectory(XMP.class);
        final var xmpProperties = xmp.getXmpProperties();
        final var modelMap = context.getModelMap();
        final var lensMap = context.getLensMap();

        final var builder = new StringBuilder();
        final var cameraMake = tiff.getMake()
                                   .or(exif::getMake)
                                   .or(() -> Optional.ofNullable(xmpProperties.get("tiff:Make")))
                                   .orElse("");
        final var cameraModel = tiff.getModel()
                                    .or(exif::getModel)
                                    .or(() -> Optional.ofNullable(xmpProperties.get("tiff:Model")))
                                    .orElse("");

        var camera = (cameraMake + ((!cameraModel.isBlank() && !cameraMake.isBlank()) ? " " : "") + cameraModel).trim();
        camera = modelMap.getOrDefault(camera, camera);
        builder.append(camera);
        builder.append(" + ");

        final var lensMake = exif.getLensMake()
                                 .or(() -> Optional.ofNullable(xmpProperties.get("exif:LensMake")))
                                 .orElse("");
        final var lensModel = exif.getLensModel()
                                  .or(() -> Optional.ofNullable(xmpProperties.get("aux:Lens")))
                                  .or(() -> Optional.ofNullable(xmpProperties.get("aux:LensID")))
                                  .orElse("");

        var lens = (lensMake + ((!lensModel.isBlank() && !lensMake.isBlank()) ? " " : "") + lensModel).trim();
        lens = lensMap.getOrDefault(lens, lens);

        builder.append(lens);
        builder.append(" @ ");
        exif.getFocalLength().ifPresent(fl -> builder.append(fl.intValue()).append(" mm, "));
        // FIXME: eventually teleconverter
        exif.getExposureTime().ifPresent(t -> builder.append(t).append(" sec @ ƒ/"));
        exif.getFNumber().map(f -> new DecimalFormat("0.#").format(f.floatValue())).ifPresent(builder::append);

        exif.getExposureBiasValue().ifPresent(exposureBiasValue ->
          {
            if (exposureBiasValue.getNumerator() != 0)
              {
                builder.append(String.format(", %+.2f EV", exposureBiasValue.floatValue()));
              }
          });

        exif.getISOSpeedRatings().ifPresent(iso -> builder.append(", ISO ").append(iso.intValue()));

        return template.replace("$" + macro + "$", builder.toString());
      }
  }
