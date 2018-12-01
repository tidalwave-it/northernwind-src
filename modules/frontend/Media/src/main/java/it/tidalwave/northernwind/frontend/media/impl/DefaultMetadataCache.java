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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Map;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A default implementation of {@link MetadataCache}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataCache implements MetadataCache
  {
    /*******************************************************************************************************************
     *
     * A holder of {@code Metadata} together with expiration information.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @Getter @ToString
    class ExpirableMetadata
      {
        @Nonnull
        private final Metadata metadata;

        private final ZonedDateTime creationTime = ZonedDateTime.now(clock.get());

        @Nonnull
        private ZonedDateTime expirationTime = creationTime.plusSeconds(medatataExpirationTime);

        /***************************************************************************************************************
         *
         * Postpones the expiration time.
         *
         **************************************************************************************************************/
        public void postponeExpirationTime()
          {
            expirationTime = ZonedDateTime.now(clock.get()).plusSeconds(medatataExpirationTime);
          }
      }

    public static final int DEFAULT_METADATA_EXPIRATION_TIME = 10 * 60;

    @Getter @Setter @Nonnull
    private Supplier<Clock> clock = Clock::systemDefaultZone;

    /** Expiration time for metadata in seconds; after this time, medatata are reloaded. */
    @Getter @Setter @Nonnegative
    private int medatataExpirationTime = DEFAULT_METADATA_EXPIRATION_TIME;

    @Inject
    private MetadataLoader metadataLoader;

    /* package */ final Map<Id, ExpirableMetadata> metadataMapById = new HashMap<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    // FIXME: shouldn't synchronize the whole method, only map manipulation
    @Override @Nonnull
    public synchronized Metadata findMetadataById (final @Nonnull Id mediaId,
                                                   final @Nonnull ResourceProperties siteNodeProperties)
      throws NotFoundException, IOException
      {
        log.debug("findMetadataById({}, ...)", mediaId);
        ExpirableMetadata metadata = metadataMapById.get(mediaId);

        if ((metadata != null) && metadata.getExpirationTime().isAfter(ZonedDateTime.now(clock.get())))
          {
            log.debug(">>>> returning cached data which will expire at {}", metadata.getExpirationTime());
            return metadata.getMetadata();
          }

        final ResourceFile file = metadataLoader.findMediaResourceFile(siteNodeProperties, mediaId);

        if (metadata != null)
          {
            final ZonedDateTime fileLatestModificationTime = file.getLatestModificationTime();
            final ZonedDateTime metadataCreationTime = metadata.getCreationTime();

            if (fileLatestModificationTime.isAfter(metadataCreationTime))
              {
                log.debug(">>>>>>>> expiring metadata: file {} > metadata {}",
                          fileLatestModificationTime, metadataCreationTime);
                metadata = null;
              }
            else
              {
                log.debug(">>>>>>>> postponing metadata expiration: file {} < metadata {}",
                          fileLatestModificationTime, metadataCreationTime);
                metadata.postponeExpirationTime();
              }
          }

        if (metadata == null)
          {
            log.debug(">>>> loading medatata...");
            metadata = new ExpirableMetadata(metadataLoader.loadMetadata(file));
            metadataMapById.put(mediaId, metadata);
          }

        return metadata.getMetadata();
      }
  }
