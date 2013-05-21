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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.imajine.image.EditableImage;
import org.imajine.image.Rational;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.TIFF;
import org.imajine.image.metadata.XMP;
import org.imajine.image.metadata.Directory;
import org.imajine.image.op.CreateOp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.PROPERTY_GROUP_ID;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.PROPERTY_LENS_IDS;
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
    
    private MetadataCache metadataCache;
    
    private MockedImage mockedImage;
    
    private Id mediaId;
    
    private ResourceProperties siteNodeProperties;
    
    private ResourceFile mediaFile;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("EmbeddedMediaMetadataProviderTestBeans.xml");
        fixture = context.getBean(EmbeddedMediaMetadataProvider.class);
        metadataCache = context.getBean(MetadataCache.class);
        mediaFile = mock(ResourceFile.class);
        mockedImage = new MockedImage();
        siteNodeProperties = mock(ResourceProperties.class);
        mediaId = new Id("mediaId");

//        when(metadataCache.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);
//        when(metadataCache.loadImage(same(mediaFile))).thenReturn(mockedImage.image);
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "metadataProvider")
    public void must_properly_interpolate_metadata_string (final @Nonnull MetadataTestBuilder metadataBuilder,
                                                           final @Nonnull String format,
                                                           final @Nonnull String expectedResult)
      throws IOException, NotFoundException, 
             NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
      {
        final ResourceProperties resourceProperties = mock(ResourceProperties.class);
        when(resourceProperties.getProperty(PROPERTY_LENS_IDS)).thenReturn(Arrays.asList("1:Lens1", "2:Lens2"));
        when(siteNodeProperties.getGroup(PROPERTY_GROUP_ID)).thenReturn(resourceProperties);

        final Metadata metadata = metadataBuilder.build();
        
        final String result = fixture.interpolateMedatadaString(mediaId, metadata, format, siteNodeProperties);
        
        assertThat(result, is(expectedResult));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "metadataProvider") @Nonnull
    private Object[][] dataProvider()
      {
        return new Object[][]
          {
              {
                new MetadataTestBuilder().withXmpDcTitle("The title 1")
                                     .withExifModel("Model 1")
                                     .withExifFocalLength(new Rational(70))
                                     .withExifExposureTime(new Rational(1, 640))
                                     .withExifFNumber(new Rational(11))
                                     .withExifExposureBiasValue(new Rational(-2, 3))
                                     .withExifIsoSpeedRatings(100)
                                     .withXmpAuxLensId("1"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ baz bar foo",
                "Foo bar Model 1 + Lens1 @ 70 mm, 1/640 sec @ f/11.0, -0.67 EV, ISO 100 foo bar The title 1 baz bar foo"
              },
              {
                new MetadataTestBuilder().withXmpDcTitle("The title 2")
                                     .withExifModel("Model 2")
                                     .withExifFocalLength(new Rational(20))
                                     .withExifExposureTime(new Rational(1, 20))
                                     .withExifFNumber(new Rational(8))
                                     .withExifExposureBiasValue(new Rational(+1, 3))
                                     .withExifIsoSpeedRatings(200)
                                     .withXmpAuxLensId("2"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ baz bar foo",
                "Foo bar Model 2 + Lens2 @ 20 mm, 1/20 sec @ f/8.0, +0.33 EV, ISO 200 foo bar The title 2 baz bar foo"
              }   
          };
      }    
  }
