/*
 * #%L
 * %%
 * %%
 * #L%
 */
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Map;
import java.io.IOException;
import org.joda.time.DateTime;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true)
@Getter @ToString @Slf4j @RequiredArgsConstructor
class MetadataBag 
  {
    @Inject
    private EmbeddedMediaMetadataProvider embeddedMediaMetadataProvider;
    
    @Inject
    private MediaLoader mediaLoader;
    
    @Getter
    private final DateTime creationTime = new DateTime();
    
    @Getter
    private DateTime expirationTime = 
                         creationTime.plusSeconds(embeddedMediaMetadataProvider.getMedatataExpirationTime());
    
    @Nonnull
    private final TIFF tiff;
    
    @Nonnull
    private final EXIF exif;
    
    @Nonnull
    private final IPTC iptc;
    
    @Nonnull
    private final XMP xmp;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public MetadataBag (final @Nonnull ResourceFile file) 
      throws IOException 
      {
        log.debug(">>>> loading medatata...");
        final EditableImage image = mediaLoader.loadImage(file);
        tiff = image.getMetadata(TIFF.class);
        exif = image.getMetadata(EXIF.class);
        iptc = image.getMetadata(IPTC.class);
        xmp = image.getMetadata(XMP.class);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    void postponeExpirationTime() 
      {
        expirationTime = new DateTime().plusSeconds(embeddedMediaMetadataProvider.getMedatataExpirationTime());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    void log (final @Nonnull Id id) 
      {
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
  }
