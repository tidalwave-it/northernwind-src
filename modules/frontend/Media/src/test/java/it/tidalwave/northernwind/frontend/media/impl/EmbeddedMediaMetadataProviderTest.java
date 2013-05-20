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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.imajine.image.metadata.Directory;
import org.imajine.image.op.CreateOp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.MetadataBag;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import org.joda.time.format.DateTimeParser;
        
/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class EmbeddedMediaMetadataProviderTest
  {
    static class MockedImage
      {
//        final TIFF tiff = mock(TIFF.class);
//        final EXIF exif = mock(EXIF.class);
//        final IPTC iptc = mock(IPTC.class);
//        final XMP xmp = mock(XMP.class);
//        
        final TIFF tiff = new TIFF();
        final EXIF exif = new EXIF();
        final IPTC iptc = new IPTC();
        final XMP xmp = new XMP();
        
        final EditableImage image = EditableImage.create(new CreateOp(10, 10, EditableImage.DataType.BYTE)); // mock(EditableImage.class);
        
        public MockedImage()
          throws Exception
          {
            // TODO: EditableImage getMetadata() can't be mocked :-( because it's final - use PowerMock?
            final Field metadataMapByClassField = image.getClass().getDeclaredField("metadataMapByClass");
            metadataMapByClassField.setAccessible(true);
            final Map<Class<? extends Directory>, List<? extends Directory>> metadataMapByClass = 
                    (Map<Class<? extends Directory>, List<? extends Directory>>) metadataMapByClassField.get(image);
            metadataMapByClass.put(TIFF.class, Collections.singletonList(tiff));
            metadataMapByClass.put(EXIF.class, Collections.singletonList(exif));
            metadataMapByClass.put(IPTC.class, Collections.singletonList(iptc));
            metadataMapByClass.put(XMP.class, Collections.singletonList(xmp));

    //        when(image.getMetadata(eq(TIFF.class))).thenReturn(tiff);
    //        when(image.getMetadata(eq(EXIF.class))).thenReturn(exif);
    //        when(image.getMetadata(eq(IPTC.class))).thenReturn(iptc);
    //        when(image.getMetadata(eq(XMP.class))).thenReturn(xmp);
          }
      }
    
    private ApplicationContext context;

    private EmbeddedMediaMetadataProvider fixture;
    
    private MediaLoader mediaLoader;
    
    private MockedImage mockedImage;
    
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
        context = new ClassPathXmlApplicationContext("EmbeddedMediaMetadataProviderTestBeans.xml");
        fixture = context.getBean(EmbeddedMediaMetadataProvider.class);
        mediaLoader = context.getBean(MediaLoader.class);
        mediaFile = mock(ResourceFile.class);
        mockedImage = new MockedImage();
        siteNodeProperties = mock(ResourceProperties.class);
        mediaId = new Id("mediaId");
        
        when(mediaLoader.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);
        when(mediaLoader.loadImage(same(mediaFile))).thenReturn(mockedImage.image);
        
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
        final MetadataBag metadataBag = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        final DateTime expectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        assertThat(metadataBag.getTiff(), sameInstance(mockedImage.tiff));
        assertThat(metadataBag.getExif(), sameInstance(mockedImage.exif));
        assertThat(metadataBag.getIptc(), sameInstance(mockedImage.iptc));
        assertThat(metadataBag.getXmp(),  sameInstance(mockedImage.xmp));
        assertThat(metadataBag.getCreationTime(),   is(baseTime));
        assertThat(metadataBag.getExpirationTime(), is(expectedExpirationTime));
        
        assertThat(fixture.metadataMapById.get(mediaId), sameInstance(metadataBag));
        
        verify(mediaLoader, times(1)).loadImage(any(ResourceFile.class));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_keep_the_same_instance_in_cache_for_a_few_time_without_checking_file_modification()
      throws Exception
      {
        final MetadataBag metadataBag = fixture.findMetadataById(mediaId, siteNodeProperties);
        final DateTime expectedExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        
        for (DateTime time = baseTime; 
             time.isBefore(expectedExpirationTime);
             time = time.plusMillis((1000 * fixture.getMedatataExpirationTime()) / 100))
          {
            setTime(time);
            final MetadataBag metadataBag2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            assertThat(metadataBag2, is(sameInstance(metadataBag)));
            log.info(">>>> next expiration time: {}", metadataBag.getExpirationTime());
          }
        
        verify(mediaLoader, times(1)).loadImage(any(ResourceFile.class));
        verify(mediaFile,   times(0)).getLatestModificationTime();
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_check_file_modification_after_expiration_time_and_still_keep_in_cache_when_no_modifications()
      throws Exception
      {
        final DateTime firstExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        DateTime latestExpirationTime = firstExpirationTime;
        when(mediaFile.getLatestModificationTime()).thenReturn(baseTime.minusMillis(1));
        
        final MetadataBag metadataBag = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        for (int count = 1; count < 10; count++)
          {
            setTime(latestExpirationTime.plusMillis(1));
            final DateTime nextExpirationTime = new DateTime().plusSeconds(fixture.getMedatataExpirationTime());
            latestExpirationTime = nextExpirationTime;
            final MetadataBag metadataBag2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            log.info(">>>> next expiration time: {}", metadataBag.getExpirationTime());
            
            assertThat(metadataBag2, is(sameInstance(metadataBag)));
            assertThat(metadataBag2.getExpirationTime(), is(nextExpirationTime));

            verify(mediaLoader, times(1)).loadImage(any(ResourceFile.class));
            verify(mediaFile,   times(count)).getLatestModificationTime();
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_reload_after_expiration_time_when_file_has_been_changed()
      throws Exception
      {
        final DateTime firstExpirationTime = baseTime.plusSeconds(fixture.getMedatataExpirationTime());
        DateTime latestExpirationTime = firstExpirationTime;
        
        final MetadataBag metadataBag = fixture.findMetadataById(mediaId, siteNodeProperties);
        
        for (int count = 1; count < 10; count++)
          {
            setTime(latestExpirationTime.plusMillis(1));
            when(mediaFile.getLatestModificationTime()).thenReturn(new DateTime().plusMillis(1));
            final DateTime nextExpirationTime = new DateTime().plusSeconds(fixture.getMedatataExpirationTime());
            latestExpirationTime = nextExpirationTime;
            
            final MetadataBag metadataBag2 = fixture.findMetadataById(mediaId, siteNodeProperties);
            log.info(">>>> next expiration time: {}", metadataBag2.getExpirationTime());
            
            assertThat(metadataBag2, is(not(sameInstance(metadataBag))));
            assertThat(metadataBag2.getExpirationTime(), is(nextExpirationTime));

            verify(mediaLoader, times(count + 1)).loadImage(any(ResourceFile.class));
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
