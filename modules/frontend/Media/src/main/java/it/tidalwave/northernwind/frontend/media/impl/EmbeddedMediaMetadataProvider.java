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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

/***********************************************************************************************************************
 *
 * An implementation of {@link MediaMetadataProvider} which retrieves metadata from embedded data inside media files.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class EmbeddedMediaMetadataProvider implements MediaMetadataProvider
  {
    public final static Key<List<String>> PROPERTY_LENS_IDS = new Key<>("lensIds");

    public final static Key<List<String>> PROPERTY_MEDIA_PATHS = new Key<>("mediaPaths");

    /* package */ final static Id PROPERTY_GROUP_ID = new Id("EmbeddedMediaMetadataProvider");

    /** Expiration time for metadata in seconds; after this time, medatata are reloaded. */
    @Getter @Setter @Nonnegative
    private int medatataExpirationTime = 10 * 60;
    
    @Inject @Nonnull
    private MediaLoader mediaLoader;

    /* package */ final Map<Id, MetadataBag> metadataMapById = new HashMap<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // FIXME: should use the Metadata API of blueMarine, but we have first to make it work with Spring and its DI.
    @Override @Nonnull
    public String getMetadataString (final @Nonnull Id id,
                                     final @Nonnull String format,
                                     final @Nonnull ResourceProperties siteNodeProperties)
      {
        try
          {
            log.info("getMetadataString({}, {})", id, format);

            final long time = System.currentTimeMillis();
            final MetadataBag metadataBag = findMetadataById(id, siteNodeProperties);
            final String string = interpolateMedatadaString(id, metadataBag, format, siteNodeProperties);
            log.info(">>>> metadata retrieved in {} msec", System.currentTimeMillis() - time);

            return string;
          }
        catch (NotFoundException e)
          {
            log.warn("Cannot find media " + id, e);
            return "";
          }
        catch (IOException e)
          {
            log.warn("Cannot get metadata for " + id, e);
            return "";
          }
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    /* package */ String interpolateMedatadaString (final @Nonnull Id id,
                                                    final @Nonnull MetadataBag metadata,
                                                    final @Nonnull String format,
                                                    final @Nonnull ResourceProperties siteNodeProperties)
      throws IOException
      {
        final XMP xmp = metadata.getXmp();
        final TIFF tiff = metadata.getTiff();
        final EXIF exif = metadata.getExif();
        final Map<String, String> xmpProperties = xmp.getXmpProperties();
        // FIXME: use format as an interpolated string to get properties both from EXIF and IPTC
//            final String string = formatted(iptc.getObject(517, String.class));

        final ResourceProperties properties = siteNodeProperties.getGroup(PROPERTY_GROUP_ID);
        final Map<String, String> lensMap = new HashMap<>();

        try
          {
            for (final String s : properties.getProperty(PROPERTY_LENS_IDS))
              {
                final String[] split = s.split(":");
                lensMap.put(split[0].trim(), split[1].trim());
              }
          }
        catch (NotFoundException e)
          {
            log.warn("", e);
          }

        if (log.isDebugEnabled())
          {
            metadata.log(id);
          }

        String string = format;

        if (format.contains("$XMP.dc.title$"))
          {
            string = string.replace("$XMP.dc.title$", formatted(xmpProperties.get("dc:title[1]")));
          }

        if (format.contains("$shootingData$"))
          {
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

            string = string.replace("$shootingData$", builder.toString());
            // Nikon D200 + AF-S 300 f/4D + TC 17E, 1/250 sec @ f/9, ISO 100
          }
        
        return string;
      }

    /*******************************************************************************************************************
     *
     * Finds metadata for the given id.
     *
     * @param  mediaId            the media id
     * @param  properties         the configuration properties
     * @return                    the {@code Media}
     * @throws NotFoundException  if no {@code Media} is found
     *
     ******************************************************************************************************************/
    // FIXME: shouldn't synchronize the whole method, only map manipulation
    @Nonnull
    /* package */ synchronized MetadataBag findMetadataById (final @Nonnull Id mediaId,
                                                             final @Nonnull ResourceProperties siteNodeProperties)
      throws NotFoundException, IOException
      {
        log.debug("findMetadataById({}, ...)", mediaId);
        MetadataBag metadataBag = metadataMapById.get(mediaId);

        if ((metadataBag != null) && metadataBag.getExpirationTime().isAfterNow())
          {
            return metadataBag;
          }
        
        final ResourceFile file = mediaLoader.findMediaResourceFile(siteNodeProperties, mediaId);
        
        if (metadataBag != null)
          {
            log.debug(">>>> checking for file modification...");
            
            if (file.getLatestModificationTime().isAfter(metadataBag.getCreationTime()))
              {
                log.debug(">>>> media file is more recent than metadata");
                metadataBag = null;  
              }
            else
              {
                metadataBag.postponeExpirationTime();
              }
          }
        
        if (metadataBag == null) 
          {
            metadataBag = new MetadataBag(file);
            metadataMapById.put(mediaId, metadataBag);
          }

        return metadataBag;
      }


    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String formatted (final @CheckForNull String string)
      {
        return (string != null) ? string : "";
      }
  }
