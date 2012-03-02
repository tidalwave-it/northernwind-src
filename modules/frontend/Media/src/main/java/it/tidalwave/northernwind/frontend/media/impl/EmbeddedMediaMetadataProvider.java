/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.XMP;
import org.imajine.image.op.ReadOp;
import org.openide.filesystems.FileUtil;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;
import static org.imajine.image.op.ReadOp.Type.*;
import static it.tidalwave.northernwind.core.model.Media.Media;

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
    private final static Key<List<String>> PROPERTY_MEDIA_PATHS = new Key<List<String>>("mediaPaths");
    private final static Id PROPERTY_GROUP_ID = new Id("EmbeddedMediaMetadataProvider");
    
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getMetadataString (final @Nonnull Id id, 
                                     final @Nonnull String format,
                                     final @Nonnull ResourceProperties siteNodeProperties) 
      {
        try
          {
            log.info("getMetadataString({}, {})", id, format);
            
            final long time = System.currentTimeMillis();
            final ResourceProperties properties = siteNodeProperties.getGroup(PROPERTY_GROUP_ID);
            final Media media = findMedia(id, properties);
            final File file = FileUtil.toFile(media.getFile());
            final EditableImage image = EditableImage.create(new ReadOp(file, METADATA));
            final EXIF exif = image.getMetadata(EXIF.class);
            final IPTC iptc = image.getMetadata(IPTC.class);
            final XMP xmp = image.getMetadata(XMP.class);
            // FIXME: use format as an interpolated string to get properties both from EXIF and IPTC
//            final String string = formatted(iptc.getObject(517, String.class));
            final String string = formatted(xmp.getXmpProperties().get("dc:title[1]"));
           
            log.info(">>>> metadata retrieved in {} msec", System.currentTimeMillis() - time);
                    
            return string;
          }
        catch (NotFoundException e)
          {
            log.warn("Cannot find media", e);
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
