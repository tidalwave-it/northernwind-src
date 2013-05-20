/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor
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
        
        return new MetadataBag(tiff, exif, iptc, xmp);
      }
  }
