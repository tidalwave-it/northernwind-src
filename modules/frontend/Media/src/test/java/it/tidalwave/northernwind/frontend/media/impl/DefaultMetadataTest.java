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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import org.imajine.image.Rational;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.P_GROUP_ID;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.P_LENS_IDS;
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
public class DefaultMetadataTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ApplicationContext context;

    private ResourceProperties properties;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = helper.createSpringContext();
        context.getBean(MetadataInterpolatorFactory.class); // initialize it
        properties = createMockProperties();
        final ResourceProperties resourceProperties = createMockProperties();
        when(resourceProperties.getProperty(P_LENS_IDS)).thenReturn(Optional.of(Arrays.asList("1:Lens1", "2:Lens2")));
        when(properties.getGroup(P_GROUP_ID)).thenReturn(resourceProperties);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "metadataProvider")
    public void must_properly_interpolate_metadata_string (final @Nonnull MetadataTestBuilder metadataBuilder,
                                                           final @Nonnull String template,
                                                           final @Nonnull String expectedResult)
      throws Exception
      {
        // given
        final Metadata underTest = metadataBuilder.build();
        // when
        final String result = underTest.interpolateString(template, properties);
        // then
        assertThat(result, is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] metadataProvider()
      {
        return new Object[][]
          {
              {
                new MetadataTestBuilder().withXmpDcTitle("The title 1")
                                         .withExifModel("Model1")
                                         .withExifFocalLength(new Rational(70))
                                         .withExifExposureTime(new Rational(1, 640))
                                         .withExifFNumber(new Rational(11))
                                         .withExifExposureBiasValue(new Rational(-2, 3))
                                         .withExifIsoSpeedRatings(100)
                                         .withXmpAuxLensId("1"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ baz bar foo",
                "Foo bar Model1 + Lens1 @ 70 mm, 1/640 sec @ \u0192/11, -0.67 EV, ISO 100 foo bar The title 1 baz bar foo"
              },
              {
                new MetadataTestBuilder().withXmpDcTitle("The title 2")
                                         .withExifModel("Model2")
                                         .withExifFocalLength(new Rational(20))
                                         .withExifExposureTime(new Rational(1, 20))
                                         .withExifFNumber(new Rational(8))
                                         .withExifExposureBiasValue(new Rational(+1, 3))
                                         .withExifIsoSpeedRatings(200)
                                         .withXmpAuxLensId("2"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ baz bar foo",
                "Foo bar Model2 + Lens2 @ 20 mm, 1/20 sec @ \u0192/8, +0.33 EV, ISO 200 foo bar The title 2 baz bar foo"
              }
          };
      }
  }
