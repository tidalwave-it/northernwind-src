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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import it.tidalwave.image.Rational;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.IPTC;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * A test facility for programmatically creating items of {@link Metadata} with a fluent interface.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor @ToString
class MetadataTestBuilder
  {
    @With
    private String xmpDcTitle;

    @With
    private String exifMake;

    @With
    private String exifModel;

    @With
    private Rational exifFocalLength;

    @With
    private Rational exifExposureTime;

    @With
    private Rational exifFNumber;

    @With
    private Rational exifExposureBiasValue;

    @With
    private int exifIsoSpeedRatings;

    @With
    private String xmpAuxLensId;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public Metadata build()
      throws Exception
      {
        final TIFF tiff = new TIFF();
        final EXIF exif = new EXIF();
        final IPTC iptc = new IPTC();
        final XMP xmp = new XMP();
        final Map<String, String> xmpProperties = new HashMap<>();
        xmpProperties.put("dc:title[1]", xmpDcTitle);
        xmpProperties.put("aux:LensID", xmpAuxLensId);
        final Method method = xmp.getClass().getDeclaredMethod("_setProperties", Map.class);
        method.setAccessible(true);
        method.invoke(xmp, xmpProperties);
        tiff.setMake(exifMake);
        tiff.setModel(exifModel);
        exif.setFocalLength(exifFocalLength);
        exif.setExposureTime(exifExposureTime);
        exif.setFNumber(exifFNumber);
        exif.setExposureBiasValue(exifExposureBiasValue);
        exif.setISOSpeedRatings(exifIsoSpeedRatings);
        final EditableImage image = new ImageTestBuilder().withTiff(tiff)
                                                          .withExif(exif)
                                                          .withIptc(iptc)
                                                          .withXmp(xmp)
                                                          .build();
//        final DefaultMetadataInterpolatorFactory interpolatorFactory = new DefaultMetadataInterpolatorFactory();
//        interpolatorFactory.initialize();
        return new DefaultMetadata("test", image);
      }
  }
