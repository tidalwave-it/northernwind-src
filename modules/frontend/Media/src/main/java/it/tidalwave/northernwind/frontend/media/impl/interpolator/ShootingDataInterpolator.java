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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import java.text.DecimalFormat;
import java.util.Arrays;
import javax.annotation.Nonnull;
import java.util.Map;
import java.text.DecimalFormat;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;

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
        final TIFF tiff = context.getMetadata().getDirectory(TIFF.class);
        final EXIF exif = context.getMetadata().getDirectory(EXIF.class);
        final XMP xmp = context.getMetadata().getDirectory(XMP.class);
        final Map<String, String> xmpProperties = xmp.getXmpProperties();
        final Map<String, String> lensMap = context.getLensMap();

        final StringBuilder builder = new StringBuilder();
        String cameraModel = formatted(exif.getModel());

        builder.append("//").append(xmpProperties).append("//");

        if ("".equals(cameraModel))
          {
            cameraModel = formatted(tiff.getModel());
          }

        if ("".equals(cameraModel))
          {
            cameraModel = formatted(xmpProperties.get("tiff:Model"));
          }

        builder.append(cameraModel);
        builder.append(" + ");

        String lens = formatted(lensMap.get(xmpProperties.get("aux:LensID")));

        if ("".equals(lens))
          {
            lens = formatted(lookup(lensMap, xmpProperties.get("aux:Lens")));
          }

        builder.append(lens);
        builder.append(" @ ");
        builder.append(exif.getFocalLength().intValue()).append(" mm, ");
        // FIXME: eventually teleconverter
        builder.append(exif.getExposureTime()).append(" sec @ \u0192/");
        builder.append(new DecimalFormat("0.#").format(exif.getFNumber().floatValue()));

        final Rational exposureBiasValue = exif.getExposureBiasValue();

        if (exposureBiasValue.getNumerator() != 0)
          {
            builder.append(String.format(", %+.2f EV", exposureBiasValue.floatValue()));
          }

        builder.append(", ISO ").append(exif.getISOSpeedRatings().intValue());

        return template.replace("$" + macro + "$", builder.toString());
      }
  }
