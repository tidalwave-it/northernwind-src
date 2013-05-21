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
import java.util.Map;
import java.io.IOException;
import org.joda.time.DateTime;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import it.tidalwave.util.Id;
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
@Getter @ToString @Slf4j @RequiredArgsConstructor
class Metadata 
  {
    @Nonnull
    private final TIFF tiff;
    
    @Nonnull
    private final EXIF exif;
    
    @Nonnull
    private final IPTC iptc;
    
    @Nonnull
    private final XMP xmp;

    @Nonnegative
    private final int expirationPeriod;
    
    @Getter
    private final DateTime creationTime = new DateTime();
    
    @Getter
    private DateTime expirationTime;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public Metadata (final @Nonnull EditableImage image,
                     final @Nonnegative int expirationPeriod) 
      throws IOException 
      {
        tiff = image.getMetadata(TIFF.class);
        exif = image.getMetadata(EXIF.class);
        iptc = image.getMetadata(IPTC.class);
        xmp = image.getMetadata(XMP.class);
        this.expirationPeriod = expirationPeriod;
        expirationTime = creationTime.plusSeconds(expirationPeriod);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    void postponeExpirationTime() 
      {
        expirationTime = new DateTime().plusSeconds(expirationPeriod);
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
