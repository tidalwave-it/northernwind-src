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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.imajine.image.Rational;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * A test facility for programmatically creating items of {@link Metadata} with a fluent interface.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor @ToString
class MetadataTestBuilder 
  {
    @Wither
    private String xmpDcTitle;
    
    @Wither
    private String exifModel;
    
    @Wither
    private Rational exifFocalLength;
    
    @Wither
    private Rational exifExposureTime;
    
    @Wither
    private Rational exifFNumber;
    
    @Wither
    private Rational exifExposureBiasValue;
    
    @Wither
    private int exifIsoSpeedRatings;
    
    @Wither
    private String xmpAuxLensId;

    @Nonnull
    public Metadata build()
      throws Exception 
      {
        final ImageTestBuilder imageBuilder = new ImageTestBuilder();
//        final TIFF tiff = new TIFF();
//        final EXIF exif = new EXIF();
//        final IPTC iptc = new IPTC();
//        final XMP xmp = new XMP();
        final Map<String, String> xmpProperties = new HashMap<>();
        xmpProperties.put("dc:title[1]", xmpDcTitle);
        xmpProperties.put("aux:LensID", xmpAuxLensId);
        final Method method = imageBuilder.xmp.getClass().getDeclaredMethod("_setProperties", Map.class);
        method.setAccessible(true);
        method.invoke(imageBuilder.xmp, xmpProperties);
        imageBuilder.exif.setModel(exifModel);
        imageBuilder.exif.setFocalLength(exifFocalLength);
        imageBuilder.exif.setExposureTime(exifExposureTime);
        imageBuilder.exif.setFNumber(exifFNumber);
        imageBuilder.exif.setExposureBiasValue(exifExposureBiasValue);
        imageBuilder.exif.setISOSpeedRatings(exifIsoSpeedRatings);
        
        return new DefaultMetadata(imageBuilder.image, 0);
      }
  }
