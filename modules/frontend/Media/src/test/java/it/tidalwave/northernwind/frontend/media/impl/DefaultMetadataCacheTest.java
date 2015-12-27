/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.DefaultMetadataCache.ExpirableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import it.tidalwave.northernwind.util.test.TestHelper;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataCacheTest
  {
    private final TestHelper helper = new TestHelper(this);

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

    private DateTime initialTime;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = helper.createSpringContext();
        underTest = context.getBean(DefaultMetadataCache.class);
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
        siteNodeProperties = mock(ResourceProperties.class);
        mediaId = new Id("mediaId");

        when(metadataLoader.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);

        // Don't use 'thenReturn(new DefaultMetadata(image))' as a new instance must be created each time
        when(metadataLoader.loadMetadata(same(mediaFile))).thenAnswer(new Answer<DefaultMetadata>()
          {
            @Override
            public DefaultMetadata answer (final @Nonnull InvocationOnMock invocation)
              {
                return new DefaultMetadata("media.jpg", image);
              }
          });

        assertThat(underTest.getMedatataExpirationTime(), is(DefaultMetadataCache.DEFAULT_METADATA_EXPIRATION_TIME));
        initialTime = new DateTime(1369080000000L);
        setTime(initialTime);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_correctly_load_medatada_when_not_in_cache()
      throws Exception
      {
        // when
        final Metadata metadata = underTest.findMetadataById(mediaId, siteNodeProperties);
        // then
        final DateTime expectedExpirationTime = initialTime.plusSeconds(underTest.getMedatataExpirationTime());
        // FIXME: the validity of loaded data must be moved to the loader test
        assertThat(metadata.getDirectory(TIFF.class), sameInstance(tiff));
        assertThat(metadata.getDirectory(EXIF.class), sameInstance(exif));
        assertThat(metadata.getDirectory(IPTC.class), sameInstance(iptc));
        assertThat(metadata.getDirectory(XMP.class),  sameInstance(xmp));

        final ExpirableMetadata expirableMetadata = underTest.metadataMapById.get(mediaId);
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
        final Metadata metadata = underTest.findMetadataById(mediaId, siteNodeProperties);
        final DateTime expectedExpirationTime = initialTime.plusSeconds(underTest.getMedatataExpirationTime());

        for (DateTime now = initialTime;
             now.isBefore(expectedExpirationTime);
             now = now.plusSeconds(underTest.getMedatataExpirationTime() / 100))
          {
            setTime(now);
            // when
            final Metadata metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(sameInstance(metadata)));

            final ExpirableMetadata expirableMetadata = underTest.metadataMapById.get(mediaId);
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
        DateTime nextExpectedExpirationTime = initialTime.plusSeconds(underTest.getMedatataExpirationTime());
        when(mediaFile.getLatestModificationTime()).thenReturn(initialTime.minusMillis(1));

        final Metadata metadata = underTest.findMetadataById(mediaId, siteNodeProperties);

        for (int count = 1; count <= 10; count++)
          {
            final DateTime now = nextExpectedExpirationTime.plusMillis(1);
            setTime(now);
            nextExpectedExpirationTime = now.plusSeconds(underTest.getMedatataExpirationTime());
            // when
            final Metadata metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(sameInstance(metadata)));

            final ExpirableMetadata expirableMetadata = underTest.metadataMapById.get(mediaId);
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
        DateTime nextExpectedExpirationTime = initialTime.plusSeconds(underTest.getMedatataExpirationTime());
        final Metadata metadata = underTest.findMetadataById(mediaId, siteNodeProperties);

        for (int count = 1; count < 10; count++)
          {
            final DateTime now = nextExpectedExpirationTime.plusMillis(1);
            setTime(now);
            when(mediaFile.getLatestModificationTime()).thenReturn(now.plusMillis(1));
            nextExpectedExpirationTime = now.plusSeconds(underTest.getMedatataExpirationTime());
            // when
            final Metadata metadata2 = underTest.findMetadataById(mediaId, siteNodeProperties);
            // then
            assertThat(metadata2, is(not(sameInstance(metadata))));

            final ExpirableMetadata expirableMetadata = underTest.metadataMapById.get(mediaId);
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
    private static void setTime (final @Nonnull DateTime dateTime)
      {
        DateTimeUtils.setCurrentMillisFixed(dateTime.getMillis());
        log.info("==== Time set to           {}", new DateTime());
      }
  }
