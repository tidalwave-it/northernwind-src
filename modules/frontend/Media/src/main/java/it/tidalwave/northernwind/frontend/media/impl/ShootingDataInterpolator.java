/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import java.util.Map;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ShootingDataInterpolator extends MetadataInterpolatorSupport
  {
    @Override @Nonnull
    public String interpolate (final @Nonnull String string, final @Nonnull Context context) 
      {
        final EXIF exif = context.getMetadata().getExif();
        final Map<String, String> xmpProperties = context.getMetadata().getXmp().getXmpProperties();
        final Map<String, String> lensMap = context.getLensMap();
        
        final StringBuilder builder = new StringBuilder();
        builder.append(formatted(exif.getModel()));
        builder.append(" + ");
        builder.append(formatted(lensMap.get(xmpProperties.get("aux:LensID"))));
        builder.append(" @ ");
        builder.append(exif.getFocalLength().intValue());
        // FIXME: eventually teleconverter
        builder.append(" mm, ");
        builder.append(exif.getExposureTime().toString());
        builder.append(" sec @ f/");
        builder.append(String.format("%.1f", exif.getFNumber().floatValue()));

        final Rational exposureBiasValue = exif.getExposureBiasValue();

        if (exposureBiasValue.getNumerator() != 0)
          {
            builder.append(String.format(", %+.2f EV", exposureBiasValue.floatValue()));
          }

        builder.append(", ISO ");
        builder.append(exif.getISOSpeedRatings().intValue());

        return string.replace("$shootingData$", builder.toString());
      }
  }
