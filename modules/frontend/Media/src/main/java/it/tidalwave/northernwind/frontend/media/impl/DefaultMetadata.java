/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolator;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolator.Context;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.PROPERTY_GROUP_ID;
import lombok.AllArgsConstructor;

/***********************************************************************************************************************
 *
 * A default implementation of {@link Metadata}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @AllArgsConstructor @ToString @Slf4j
class DefaultMetadata implements Metadata
  {
    @Nonnull
    private final String mediaName;

    @Nonnull
    private final EditableImage image;
    
    @Inject
    private MetadataInterpolatorFactory interpolatorFactory;

    /*******************************************************************************************************************
     *
     *
     * 
     ******************************************************************************************************************/
    public DefaultMetadata (final @Nonnull String mediaName, final @Nonnull EditableImage image)
      {
        this.mediaName = mediaName;
        this.image = image;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> T getDirectory (final @Nonnull Class<T> metadataClass)
      {
        return image.getMetadata(metadataClass);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String interpolateString (final @Nonnull String template, 
                                     final @Nonnull ResourceProperties properties)
      throws IOException
      {
        if (log.isDebugEnabled())
          {
            log();
          }
        
        // FIXME: use format as an interpolated string to get properties both from EXIF and IPTC
        //            final String string = formatted(iptc.getObject(517, String.class));
        final Context context = new Context(this, getLensMap(properties));

        String result = template;
        
        for (final MetadataInterpolator interpolator : interpolatorFactory.getInterpolators())
          {
            if (result.contains("$" + interpolator.getMacro() + "$"))
              {
                result = interpolator.interpolate(result, context);
              }
          }
        
        return result;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void log() 
      {
        final TIFF tiff = image.getMetadata(TIFF.class);
        final EXIF exif = image.getMetadata(EXIF.class);
        final IPTC iptc = image.getMetadata(IPTC.class);
        final XMP xmp = image.getMetadata(XMP.class);
        final Map<String, String> xmpProperties = new TreeMap<>(xmp.getXmpProperties());
        
        for (final int tagCode : tiff.getTagCodes()) 
          {
            log.debug("TIFF({}).{}: {}", mediaName, tiff.getTagName(tagCode), tiff.getObject(tagCode));
          }
        
        for (final int tagCode : exif.getTagCodes()) 
          {
            log.debug("EXIF({}).{}: {}", mediaName, exif.getTagName(tagCode), exif.getObject(tagCode));
          }
        
        for (final int tagCode : iptc.getTagCodes()) 
          {
            log.debug("IPTC({}).{}: {}", mediaName, iptc.getTagName(tagCode), iptc.getObject(tagCode));
          }
        
        for (final int tagCode : xmp.getTagCodes()) 
          {
            log.debug("XMP({}).{}: {}", mediaName, xmp.getTagName(tagCode), xmp.getObject(tagCode));
          }
        
        for (final Map.Entry<String, String> e : xmpProperties.entrySet())
          {
            log.debug("XMPprop({}).{}: {}", mediaName, e.getKey(), e.getValue());
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
            for (final String s : properties.getProperty2(EmbeddedMediaMetadataProvider.PROPERTY_LENS_IDS))
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
