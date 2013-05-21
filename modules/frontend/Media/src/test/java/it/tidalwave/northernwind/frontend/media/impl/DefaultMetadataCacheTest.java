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
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
    private ApplicationContext context;

    private DefaultMetadataCache fixture;
    
    private MediaLoader mediaLoader;
    
    private EditableImage image;
    
    private TIFF tiff;
    
    private EXIF exif;
    
    private IPTC iptc;
    
    private XMP xmp;
    
    private Id mediaId;
    
    private ResourceProperties siteNodeProperties;
    
    private ResourceFile mediaFile;
    
    private DateTime baseTime;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("DefaultMetadataCacheTestBeans.xml");
        fixture = context.getBean(DefaultMetadataCache.class);
        mediaLoader = context.getBean(MediaLoader.class);
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
        
        when(mediaLoader.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);
        when(mediaLoader.loadImage(same(mediaFile))).thenReturn(image);
        
        assertThat(fixture.getMedatataExpirationTime(), is(600));
        baseTime = new DateTime(1369080000000L);
        setTime(baseTime);
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_correctly_load_medatada_when_not_in_cache()
      throws Exception
      {
        final Metadata metadata = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        final DateTime expectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        assertThat(metadata.getDirectory(TIFF.class), sameInstance(tiff));
        assertThat(metadata.getDirectory(EXIF.class), sameInstance(exif));
        assertThat(metadata.getDirectory(IPTC.class), sameInstance(iptc));
        assertThat(metadata.getDirectory(XMP.class),  sameInstance(xmp));
        assertThat(metadata.getCreationTime(),   is(baseTime));
        assertThat(metadata.getExpirationTime(), is(expectedExpirationTime));
        
        assertThat(fixture.metadataMapById.get(mediaId), sameInstance(metadata));
        
        verify(mediaLoader, times(1)).loadImage(eq(mediaFile));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_cache_the_same_instance_within_expiration_time_without_checking_for_file_modification()
      throws Exception
      {
        final Metadata metadata = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        final DateTime expectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        
        for (DateTime time = baseTime; 
             time.isBefore(expectedExpirationTime);
             time = time.plusSeconds(fixture.getMedatataExpirationTime() / 100))
          {
            setTime(time);
            
            final Metadata metadata2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            
            assertThat(metadata2, is(sameInstance(metadata)));
            assertThat(metadata2.getExpirationTime(), is(expectedExpirationTime));
            log.info(">>>> next expiration time: {}", metadata.getExpirationTime());
          }
        
        verify(mediaLoader, times(1)).loadImage(eq(mediaFile));
        verify(mediaFile,   times(0)).getLatestModificationTime();
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_check_file_modification_after_expiration_time_and_still_keep_in_cache_when_no_modifications()
      throws Exception
      {
        DateTime nextExpectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        when(mediaFile.getLatestModificationTime()).thenReturn(baseTime.minusMillis(1));
        
        final Metadata metadata = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        for (int count = 1; count <= 10; count++)
          {
            setTime(nextExpectedExpirationTime.plusMillis(1));
            nextExpectedExpirationTime = new DateTime().plusSeconds(fixture.getMedatataExpirationTime());
            
            final Metadata metadata2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            
            assertThat(metadata2, is(sameInstance(metadata)));
            assertThat(metadata2.getExpirationTime(), is(nextExpectedExpirationTime));
            log.info(">>>> next expiration time: {}", metadata2.getExpirationTime());

            verify(mediaLoader, times(1)).loadImage(eq(mediaFile));
            verify(mediaFile,   times(count)).getLatestModificationTime();
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_reload_metadata_after_expiration_time_when_file_has_been_changed()
      throws Exception
      {
        DateTime nextExpectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());        
        final Metadata metadata = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        for (int count = 1; count < 10; count++)
          {
            setTime(nextExpectedExpirationTime.plusMillis(1));
            when(mediaFile.getLatestModificationTime()).thenReturn(new DateTime().plusMillis(1));
            nextExpectedExpirationTime = new DateTime().plusSeconds(fixture.getMedatataExpirationTime());
            
            final Metadata metadata2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            
            assertThat(metadata2, is(not(sameInstance(metadata))));
            assertThat(metadata2.getExpirationTime(), is(nextExpectedExpirationTime));
            log.info(">>>> next expiration time: {}", metadata2.getExpirationTime());

            verify(mediaLoader, times(count + 1)).loadImage(eq(mediaFile));
            verify(mediaFile,   times(count)).getLatestModificationTime();
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
