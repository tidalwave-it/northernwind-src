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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
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
        if (log.isDebugEnabled())
          {
            metadata.log(id);
          }
        
        final MetadataInterpolator.Context context = 
                new MetadataInterpolator.Context(metadata, getLensMap(siteNodeProperties));
        final List<MetadataInterpolator> metadataInterpolators = new ArrayList<>();
        // FIXME: discover them with an annotation
        metadataInterpolators.add(new XmlDcTitleInterpolator());
        metadataInterpolators.add(new ShootingDataInterpolator());

        String string = format;
        
        for (final MetadataInterpolator metadataInterpolator : metadataInterpolators)
          {
            if (string.contains(metadataInterpolator.getId()))
              {
                string = metadataInterpolator.interpolate(string, context);
              }
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
    private Map<String, String> getLensMap (final @Nonnull ResourceProperties siteNodeProperties)
      throws IOException 
      {
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
        
        return lensMap;
      }
  }
