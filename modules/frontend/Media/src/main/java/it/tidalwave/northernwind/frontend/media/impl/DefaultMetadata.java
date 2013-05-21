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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.joda.time.DateTime;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.PROPERTY_GROUP_ID;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @ToString @Slf4j
class DefaultMetadata implements Metadata
  {
    @Nonnull
    private final EditableImage image;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> T getDirectory (final @Nonnull Class<T> metadataClass)
      {
        return image.getMetadata(metadataClass);
      }
    
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String interpolateMetadataString (final @Nonnull Id mediaId,
                                             final @Nonnull ResourceProperties siteNodeProperties,
                                             final @Nonnull String format)
      throws IOException
      {
        if (log.isDebugEnabled())
          {
            log(mediaId);
          }
        
        // FIXME: use format as an interpolated string to get properties both from EXIF and IPTC
        //            final String string = formatted(iptc.getObject(517, String.class));
        final MetadataInterpolator.Context context = 
                new MetadataInterpolator.Context(this, getLensMap(siteNodeProperties));
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
     ******************************************************************************************************************/
    private void log (final @Nonnull Id id) 
      {
        final TIFF tiff = image.getMetadata(TIFF.class);
        final EXIF exif = image.getMetadata(EXIF.class);
        final IPTC iptc = image.getMetadata(IPTC.class);
        final XMP xmp = image.getMetadata(XMP.class);
        final Map<String, String> xmpProperties = xmp.getXmpProperties();
        log.debug("XMP({}): {}", id, xmpProperties);
        
        for (final int tagCode : exif.getTagCodes()) 
          {
            log.debug("EXIF({}).{}: {}", id, exif.getTagName(tagCode), exif.getObject(tagCode));
          }
        
        for (final int tagCode : tiff.getTagCodes()) 
          {
            log.debug("TIFF({}).{}: {}", id, tiff.getTagName(tagCode), tiff.getObject(tagCode));
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Map<String, String> getLensMap (final @Nonnull ResourceProperties siteNodeProperties)
      throws IOException 
      {
        final ResourceProperties properties = siteNodeProperties.getGroup(PROPERTY_GROUP_ID);
        final Map<String, String> lensMap = new HashMap<>();
        
        try
          {
            for (final String s : properties.getProperty(EmbeddedMediaMetadataProvider.PROPERTY_LENS_IDS))
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
