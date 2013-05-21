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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor @ToString
class MetadataBuilder 
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
    public MetadataBag build()
      throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
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
        exif.setModel(exifModel);
        exif.setFocalLength(exifFocalLength);
        exif.setExposureTime(exifExposureTime);
        exif.setFNumber(exifFNumber);
        exif.setExposureBiasValue(exifExposureBiasValue);
        exif.setISOSpeedRatings(exifIsoSpeedRatings);
        
        return new MetadataBag(tiff, exif, iptc, xmp, 0);
      }
  }