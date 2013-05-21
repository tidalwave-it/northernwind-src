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
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataCache implements MetadataCache
  {
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
    // FIXME: shouldn't synchronize the whole method, only map manipulation
    @Override @Nonnull
    public synchronized MetadataBag findMetadataById (final @Nonnull Id mediaId,
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
            metadataBag = new MetadataBag(file, medatataExpirationTime);
            metadataMapById.put(mediaId, metadataBag);
          }

        return metadataBag;
      }
  }
