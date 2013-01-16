/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.XMP;
import org.imajine.image.op.ReadOp;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import static org.imajine.image.op.ReadOp.Type.*;
import static it.tidalwave.northernwind.core.model.Media.Media;
import org.imajine.image.Rational;
import org.imajine.image.metadata.TIFF;

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
    @RequiredArgsConstructor @Getter @ToString
    static class MetadataBag
      {
        @Nonnull
        private final TIFF tiff;

        @Nonnull
        private final EXIF exif;

        @Nonnull
        private final IPTC iptc;

        @Nonnull
        private final XMP xmp;
      }

    private final static Key<List<String>> PROPERTY_MEDIA_PATHS = new Key<>("mediaPaths");

    private final static Key<List<String>> PROPERTY_LENS_IDS = new Key<>("lensIds");

    private final static Id PROPERTY_GROUP_ID = new Id("EmbeddedMediaMetadataProvider");

    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;

    private final Map<Id, MetadataBag> metadataMapById = new HashMap<>();

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
            final XMP xmp = metadataBag.getXmp();
            final TIFF tiff = metadataBag.getTiff();
            final EXIF exif = metadataBag.getExif();
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
                log.debug("XMP({}): {}", id, xmpProperties);

                for (final int tagCode : exif.getTagCodes())
                  {
                    log.debug("EXIF({}).{}: {}", new Object[] { id, exif.getTagName(tagCode), exif.getObject(tagCode) });
                  }

                for (final int tagCode : tiff.getTagCodes())
                  {
                    log.debug("TIFF({}).{}: {}", new Object[] { id, tiff.getTagName(tagCode), tiff.getObject(tagCode) });
                  }
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
    private synchronized MetadataBag findMetadataById (final @Nonnull Id mediaId,
                                                       final @Nonnull ResourceProperties siteNodeProperties)
      throws NotFoundException, IOException
      {
        MetadataBag metadataBag = metadataMapById.get(mediaId);

        if (metadataBag == null)
          {
            final ResourceProperties properties = siteNodeProperties.getGroup(PROPERTY_GROUP_ID);
            final Media media = findMedia(mediaId, properties);
            final File file = media.getFile().toFile();
            final EditableImage image = EditableImage.create(new ReadOp(file, METADATA));
            final TIFF tiff = image.getMetadata(TIFF.class);
            final EXIF exif = image.getMetadata(EXIF.class);
            final IPTC iptc = image.getMetadata(IPTC.class);
            final XMP xmp = image.getMetadata(XMP.class);
            metadataBag = new MetadataBag(tiff, exif, iptc, xmp);
            metadataMapById.put(mediaId, metadataBag);
          }

        return metadataBag;
      }

    /*******************************************************************************************************************
     *
     * Finds a {@link Media} item for the given id.
     *
     * @param  mediaId            the media id
     * @param  properties         the configuration properties
     * @return                    the {@code Media}
     * @throws NotFoundException  if no {@code Media} is found
     *
     ******************************************************************************************************************/
    @Nonnull
    private Media findMedia (final @Nonnull Id mediaId, final @Nonnull ResourceProperties properties)
      throws NotFoundException, IOException
      {
        final Site site = siteProvider.get().getSite();

        for (final Iterator<String> i = properties.getProperty(PROPERTY_MEDIA_PATHS).iterator(); i.hasNext(); )
          {
            final String mediaPath = i.next();
            final String resourceRelativePath = String.format(mediaPath, mediaId.stringValue());

            try
              {
                return site.find(Media).withRelativePath(resourceRelativePath).result();
              }
            catch (NotFoundException e)
              {
                if (!i.hasNext())
                  {
                    throw e;
                  }
              }
          }

        throw new RuntimeException("Shouldn't get here");
      }

    @Nonnull
    private String formatted (final @Nonnull String string)
      {
        return (string != null) ? string : "";
      }
  }
