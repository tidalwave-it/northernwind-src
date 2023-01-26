/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnull;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.IPTC;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.stubbing.Answer;
import it.tidalwave.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataCacheTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ApplicationContext context;

    private DefaultMetadataCache underTest;

    private MetadataLoader metadataLoader;

    private EditableImage image;

    private TIFF tiff;

    private EXIF exif;

    private IPTC iptc;

    private XMP xmp;

    private Id mediaId;

    private ResourceProperties siteNodeProperties;

    private ResourceFile mediaFile;

    private ZonedDateTime initialTime;

    private Clock mockClock;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = helper.createSpringContext();
        underTest = context.getBean(DefaultMetadataCache.class);
        underTest.setClock(() -> mockClock);
        metadataLoader = context.getBean(MetadataLoader.class);
        mediaFile = mock(ResourceFile.class);
        tiff = new TIFF();
        exif = new EXIF();
        iptc = new IPTC();
        xmp = new XMP();
        image = new ImageTestBuilder().withTiff(tiff)
                                      .withExif(exif)
                                      .withIptc(iptc)
                                      .withXmp(xmp)
                                      .build();
        siteNodeProperties = createMockProperties();
        mediaId = new Id("mediaId");

        when(metadataLoader.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);

        // Don't use 'thenReturn(new DefaultMetadata(image))' as a new instance must be created each time
        when(metadataLoader.loadMetadata(same(mediaFile))).thenAnswer(
                (Answer<DefaultMetadata>)invocation -> new DefaultMetadata("media.jpg", image));

        assertThat(underTest.getMetadataExpirationTime(), is(DefaultMetadataCache.DEFAULT_METADATA_EXPIRATION_TIME));
        initialTime = Instant.ofEpochMilli(1369080000000L).atZone(ZoneId.of("GMT"));
        setTime(initialTime);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_correctly_load_metadata_when_not_in_cache()
      throws Exception
      {
        // when
        final var metadata = underTest.findMetadataById(mediaId, siteNodeProperties);
        // then
        final var expectedExpirationTime = initialTime.plusSeconds(underTest.getMetadataExpirationTime());
        // FIXME: the validity of loaded data must be moved to the loader test
        assertThat(metadata.getDirectory(TIFF.class), sameInstance(tiff));
        assertThat(metadata.getDirectory(EXIF.class), sameInstance(exif));
        assertThat(metadata.getDirectory(IPTC.class), sameInstance(iptc));
        assertThat(metadata.getDirectory(XMP.class),  sameInstance(xmp));

        final var expirableMetadata = underTest.metadataMapById.get(mediaId);
        assertThat(expirableMetadata, is(notNullValue()));
        assertThat(expirableMetadata.getMetadata(),       sameInstance(metadata));
        assertThat(expirableMetadata.getCreationTime(),   is(initialTime));
        assertThat(expirableMetadata.getExpirationTime(), is(expectedExpirationTime));

        verify(metadataLoader, times(1)).loadMetadata(eq(mediaFile));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_cache_the_same_instance_within_expiration_time_without_checking_for_file_modification()
      throws Exception
      {
        // given
        final var metadata = underTest.findMetadataById(mediaId, siteNodeProperties);
        final var expectedExpirationTime = initialTime.plusSeconds(underTest.getMetadataExpirationTime());

        for (var now = initialTime;
             now.isBefore(expectedExpirationTime);
             now = now.plusSeconds(underTest.getMetadataExpirationTime() / 100))
          {
            setTime(now);
            // when
            final var metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(sameInstance(metadata)));

            final var expirableMetadata = underTest.metadataMapById.get(mediaId);
            assertThat(expirableMetadata,                     is(notNullValue()));
            assertThat(expirableMetadata.getMetadata(),       sameInstance(metadata2));
            assertThat(expirableMetadata.getCreationTime(),   is(initialTime));
            assertThat(expirableMetadata.getExpirationTime(), is(expectedExpirationTime));
            log.info(">>>> next expiration time: {}", expirableMetadata.getExpirationTime());
          }

        verify(metadataLoader, times(1)).loadMetadata(eq(mediaFile));
        verify(mediaFile,      times(0)).getLatestModificationTime();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_check_file_modification_after_expiration_time_and_still_keep_in_cache_when_no_modifications()
      throws Exception
      {
        // given
        var nextExpectedExpirationTime = initialTime.plusSeconds(underTest.getMetadataExpirationTime());
        when(mediaFile.getLatestModificationTime()).thenReturn(initialTime.minusNanos(1));

        final var metadata = underTest.findMetadataById(mediaId, siteNodeProperties);

        for (var count = 1; count <= 10; count++)
          {
            final var now = nextExpectedExpirationTime.plusNanos(1);
            setTime(now);
            nextExpectedExpirationTime = now.plusSeconds(underTest.getMetadataExpirationTime());
            // when
            final var metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(sameInstance(metadata)));

            final var expirableMetadata = underTest.metadataMapById.get(mediaId);
            assertThat(expirableMetadata,                     is(notNullValue()));
            assertThat(expirableMetadata.getMetadata(),       sameInstance(metadata2));
            assertThat(expirableMetadata.getCreationTime(),   is(initialTime));
            assertThat(expirableMetadata.getExpirationTime(), is(nextExpectedExpirationTime));
            log.info(">>>> next expiration time: {}", expirableMetadata.getExpirationTime());

            verify(metadataLoader, times(1)).loadMetadata(eq(mediaFile));
            verify(mediaFile,      times(count)).getLatestModificationTime();
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_reload_metadata_after_expiration_time_when_file_has_been_changed()
      throws Exception
      {
        // given
        var nextExpectedExpirationTime = initialTime.plusSeconds(underTest.getMetadataExpirationTime());
        final var metadata = underTest.findMetadataById(mediaId, siteNodeProperties);

        for (var count = 1; count < 10; count++)
          {
            final var now = nextExpectedExpirationTime.plusNanos(1);
            setTime(now);
            when(mediaFile.getLatestModificationTime()).thenReturn(now.plusNanos(1));
            nextExpectedExpirationTime = now.plusSeconds(underTest.getMetadataExpirationTime());
            // when
            final var metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(not(sameInstance(metadata))));

            final var expirableMetadata = underTest.metadataMapById.get(mediaId);
            assertThat(expirableMetadata,                     is(notNullValue()));
            assertThat(expirableMetadata.getMetadata(),       sameInstance(metadata2));
            assertThat(expirableMetadata.getCreationTime(),   is(now));
            assertThat(expirableMetadata.getExpirationTime(), is(nextExpectedExpirationTime));
            log.info(">>>> next expiration time: {}", expirableMetadata.getExpirationTime());

            verify(metadataLoader, times(count + 1)).loadMetadata(eq(mediaFile));
            verify(mediaFile,      times(count)).getLatestModificationTime();
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void setTime (@Nonnull final ZonedDateTime dateTime)
      {
        mockClock = Clock.fixed(dateTime.toInstant(), dateTime.getZone());
        log.info("==== Time set to           {}", ZonedDateTime.now(mockClock));
      }
  }
