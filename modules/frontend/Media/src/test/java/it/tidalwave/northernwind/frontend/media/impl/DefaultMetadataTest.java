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
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.Rational;
import it.tidalwave.image.metadata.MetadataTestUtils;
import it.tidalwave.image.op.ReadOp;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;

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
     * Initialize the MetadataInterpolatorFactory only once since it takes a few tenths of seconds because of classpath
     * scanning.
     *
     ******************************************************************************************************************/
    @BeforeClass
    public void setupCommon()
      {
        context = helper.createSpringContext();
        context.getBean(MetadataInterpolatorFactory.class); // initialize it
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        properties = createMockProperties();
        final var resourceProperties = createMockProperties();
        when(resourceProperties.getProperty(P_CAMERA_IDS)).thenReturn(Optional.of(List.of(
              "SONY ILCE-6000:Sony α6000",
              "SONY ILCE-6300:Sony α6300",
              "SONY ILCE-6600:Sony α6600",
              "SONY NEX-6:Sony NEX-6",
              "NIKON CORPORATION NIKON D70:Nikon D70",
              "NIKON CORPORATION NIKON D100:Nikon D100",
              "NIKON CORPORATION NIKON D200:Nikon D200",
              "NIKON CORPORATION NIKON D5000:Nikon D5000",
              "NIKON CORPORATION NIKON D5100:Nikon D5100",
              "NIKON CORPORATION NIKON D7000:Nikon D7000",
              "OLYMPUS IMAGING CORP. SP590UZ:Olympus SP-590 UZ")));
        when(resourceProperties.getProperty(P_LENS_IDS)).thenReturn(Optional.of(List.of(
              "0.0 mm f/0.0: Samyang 8mm ƒ/3.5 fish-eye",
              "105mm F2.8 DG DN MACRO | Art 020: Sigma 105mm F2.8 DG DN Macro Art",
              "106: Nikkor 300mm ƒ/4D ED-IF AF-S",
              "11: Nikkor 180mm ƒ/2.8N ED-IF AF",
              "12-24 mm f/4: 12.0-24.0 mm f/4.0: Nikkor 12-24mm ƒ/4G DX AF-S",
              "12.0-24.0 mm f/4.0: Nikkor 12-24mm ƒ/4G DX AF-S",
              "122: Nikkor 12-24mm ƒ/4G DX AF-S",
              "127: Nikkor 18-70mm ƒ/3.5-4.5G ED DX AF-S",
              "150-600mm F5-6.3 DG OS HSM | Contemporary 015: Sigma 150-600mm ƒ/5-6.3 DG OS HSM C",
              "18.0-70.0 mm f/3.5-4.5: Nikkor 18-70mm ƒ/3.5-4.5G ED DX AF-S",
              "180.0 mm f/2.8: Nikkor 180mm ƒ/2.8N ED-IF AF",
              "24.0 mm f/2.8: Nikkor 35mm ƒ/2.8D AF",
              "300.0 mm f/4.0: Nikkor 300mm ƒ/4D ED-IF AF-S",
              "35.0 mm f/1.8: Nikkor 35mm ƒ/1.8G AF-S",
              "35.0 mm f/2.0: Nikkor 35mm ƒ/2D AF",
              "50.0 mm f/1.8: Nikkor 50mm ƒ/1.8D AF",
              "70-200mm F4 G OSS: Sony FE 70-200mm F4 G OSS",
              "74: Samyang 8mm ƒ/3.5 II fish-eye",
              "8.0 mm f/3.5: Samyang 8mm ƒ/3.5 fish-eye",
              "85.0 mm f/1.8: Nikkor 85mm ƒ/1.8D AF",
              "E 10-18mm F4 OSS: Sony E 10-18mm F4 OSS",
              "E 150-600mm F5-6.3: Sigma 150-600mm ƒ/5-6.3 DG OS HSM C",
              "E 16-70mm F4 ZA OSS: Sony Zeiss Vario-Tessar T* E 16-70mm F4 ZA OSS",
              "E 20mm F2.8 F050: Tamron 20mm F/2.8 Di III RXD 1:2",
              "E 20mm F2.8: Tamron 20mm F/2.8 Di III RXD 1:2",
              "E 30mm F2.8: Sigma 30mm F2.8 DN | A",
              "E 35mm F2.8: Samyang 35mm AF F2.8",
              "FE 200-600mm F5.6-6.3 G OSS: Sony FE 200-600mm F5.6-6.3 G OSS",
              "FE 70-200mm F4 G OSS: Sony FE 70-200mm F4 G OSS",
              "Nikkor AF-D 50.0 mm f/1.8: Nikkor 50mm ƒ/1.8D AF",
              "Nikon AF Nikkor 85mm f/1.8D: Nikkor 85mm ƒ/1.8D AF",
              "SAMYANG AF 35mm F2.8: Samyang 35mm AF F2.8",
              "Samyang 8mm ƒ/3.5 II: Samyang 8mm ƒ/3.5 II fish-eye",
              "Samyang 8mm ƒ/3.5: Samyang 8mm ƒ/3.5 fish-eye",
              "Sigma 150-600mm ƒ/5-6.3 DG OS HSM Contemporary: Sigma 150-600mm ƒ/5-6.3 DG OS HSM C",
              "Sony E 10-18mm F4 OSS (SEL1018): Sony E 10-18mm F4 OSS",
              "Sony E 20mm F2.8 (SEL20F28): Tamron 20mm F/2.8 Di III RXD 1:2",
              "Sony FE 70-200mm F4 G OSS (SEL70200G): Sony FE 70-200mm F4 G OSS (SEL70200G)",
              "Zeiss Vario-Tessar T* E 16-70 mm F4 ZA OSS (SEL1670Z): Zeiss Vario-Tessar T* E 16-70 mm F4 ZA OSS",
              "Zenit Helios 44-2 58mm ƒ/2.0: Zenit Helios 44-2 58mm ƒ/2")));
        when(properties.getGroup(P_GROUP_ID)).thenReturn(resourceProperties);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "metadataProvider")
    public void must_properly_interpolate_metadata_string (@Nonnull final MetadataTestBuilder metadataBuilder,
                                                           @Nonnull final String template,
                                                           @Nonnull final String expectedResult)
      throws Exception
      {
        // given
        final var underTest = metadataBuilder.build();
        // when
        final var result = underTest.interpolateString(template, properties);
        // then
        assertThat(result, is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_interpolate_metadata_from_JPEG()
            throws IOException
      {
        // given
        final var file = helper.resourceFileFor("20230112-0076.jpg");
        final var image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        final Metadata underTest = new DefaultMetadata("test", image);
        final var template = "SHOOTING DATA $shootingData$ - TITLE: $IPTC.IIM.title$";
        // when
        final var result = underTest.interpolateString(template, properties);
        // then
        assertThat(result,
        is("SHOOTING DATA Sony α6600 + Sony FE 70-200mm F4 G OSS @ 168 mm, 1/160 sec @ ƒ/8, -0.30 EV, ISO 125 " +
        "- TITLE: Sant'Eusebio a Perti."));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "testSet_StoppingDown_100_20230116")
    public void must_properly_interpolate_metadata_from_StoppingDown_test_set (@Nonnull final Path file)
            throws IOException
      {
        // given
        final var image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        final Metadata underTest = new DefaultMetadata("test", image);
        final var template = "SHOOTING DATA: $shootingData$\nTITLE: $IPTC.IIM.title$\n";
        // when
        final var result = underTest.interpolateString(template, properties);
        // then
        log.info(result);
        final var resourceName = file.getFileName().toString().replaceAll("\\.jpg$", ".txt");
        final var actualResults = Path.of("target/test-results/stoppingdown_100_20230116");
        final var expectedResults = Path.of("src/test/resources/expected-results/stoppingdown_100_20230116");
        final var actualDump = actualResults.resolve(resourceName);
        final var expectedDump = expectedResults.resolve(resourceName);
        Files.createDirectories(actualDump.getParent());
        Files.createDirectories(expectedDump.getParent());
        Files.writeString(actualDump, result, UTF_8);
        assertSameContents(expectedDump, actualDump);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    protected static Object[][] testSet_StoppingDown_100_20230116()
            throws IOException
      {
        return MetadataTestUtils.testSet_StoppingDown_100_20230116().stream()
                                .map(p -> new Object[]{ p})
                                .toArray(Object[][]::new);
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
                                         .withExifMake("NIKON CORPORATION")
                                         .withExifModel("NIKON D100")
                                         .withExifFocalLength(Rational.of(180))
                                         .withExifExposureTime(Rational.of(1, 640))
                                         .withExifFNumber(Rational.of(11))
                                         .withExifExposureBiasValue(Rational.of(-2, 3))
                                         .withExifIsoSpeedRatings(100)
                                         .withXmpAuxLensId("11"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ bar foo",
                "Foo bar Nikon D100 + Nikkor 180mm ƒ/2.8N ED-IF AF @ 180 mm, 1/640 sec @ ƒ/11, -0.67 EV, ISO" +
                " 100 foo bar The title 1 bar foo"
              },
              {
                new MetadataTestBuilder().withXmpDcTitle("The title 2")
                                         .withExifMake("NIKON CORPORATION")
                                         .withExifModel("NIKON D200")
                                         .withExifFocalLength(Rational.of(8))
                                         .withExifExposureTime(Rational.of(1, 20))
                                         .withExifFNumber(Rational.of(8))
                                         .withExifExposureBiasValue(Rational.of(+1, 3))
                                         .withExifIsoSpeedRatings(200)
                                         .withXmpAuxLensId("74"),
                "Foo bar $shootingData$ foo bar $XMP.dc.title$ bar foo",
                "Foo bar Nikon D200 + Samyang 8mm ƒ/3.5 II fish-eye @ 8 mm, 1/20 sec @ ƒ/8, +0.33 EV, ISO 200 " +
                "foo bar The title 2 bar foo"
              }
          };
      }
  }
