/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import java.util.Map;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ShootingDataInterpolator extends MetadataInterpolatorSupport
  {
    public ShootingDataInterpolator() 
      {
        super("$shootingData$");
      }
    
    @Override @Nonnull
    public String interpolate (final @Nonnull String string, final @Nonnull Context context) 
      {
        final EXIF exif = context.getMetadata().getDirectory(EXIF.class);
        final Map<String, String> xmpProperties = context.getMetadata().getDirectory(XMP.class).getXmpProperties();
        final Map<String, String> lensMap = context.getLensMap();
        
        final StringBuilder builder = new StringBuilder();
        builder.append(formatted(exif.getModel()));
        builder.append(" + ");
        builder.append(formatted(lensMap.get(xmpProperties.get("aux:LensID"))));
        builder.append(" @ ");
        builder.append(exif.getFocalLength().intValue()).append(" mm, ");
        // FIXME: eventually teleconverter
        builder.append(exif.getExposureTime().toString()).append(" sec @ f/");
        builder.append(String.format("%.1f", exif.getFNumber().floatValue()));

        final Rational exposureBiasValue = exif.getExposureBiasValue();

        if (exposureBiasValue.getNumerator() != 0)
          {
            builder.append(String.format(", %+.2f EV", exposureBiasValue.floatValue()));
          }

        builder.append(", ISO ").append(exif.getISOSpeedRatings().intValue());

        return string.replace(id, builder.toString());
      }
  }
