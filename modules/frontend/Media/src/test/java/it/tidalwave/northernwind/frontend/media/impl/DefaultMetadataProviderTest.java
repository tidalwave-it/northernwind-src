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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.Rational;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;
import it.tidalwave.image.metadata.Directory;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.IPTC;
import it.tidalwave.image.op.ReadOp;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import it.tidalwave.util.test.SpringTestHelper;
import static it.tidalwave.image.metadata.Directory.Tag;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataProviderTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_read_metadata()
      throws Exception
      {
        // given
        final var file = helper.resourceFileFor("20100102-0001.jpg").toFile();
        final var image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        log.info("IMAGE: {}", image);
        // when
        final var tiff = image.getMetadata(TIFF.class).orElseGet(TIFF::new);
        final var exif = image.getMetadata(EXIF.class).orElseGet(EXIF::new);
        final var iptc = image.getMetadata(IPTC.class).orElseGet(IPTC::new);
        final var xmp = image.getMetadata(XMP.class).orElseGet(XMP::new);
        // then
        log.info("TIFF: {}", tiff);
        log.info("EXIF: {}", exif);
        log.info("IPTC: {}", iptc);
        log.info("XMP: {}", xmp);
        final var resourceName = String.format("MetadataDump-%s.txt", "20100102-0001");
        final var tr = helper.testResourceFor(resourceName);
        final List<String> strings = new ArrayList<>();
        dumpTags(strings, "TIFF", tiff);
        dumpTags(strings, "EXIF", exif);
        dumpTags(strings, "IPTC", iptc);
        dumpTags(strings, "XMP ", xmp);
        tr.writeToActualFile(strings);
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void dumpTags (@Nonnull final List<? super String> strings,
                                  @Nonnull final String directoryName,
                                  @Nonnull final Directory directory)
      {
        for (final var tag : directory.getTagCodes())
          {
            var value = directory.getRaw(tag);

            if (value instanceof byte[])
              {
                value = Arrays.toString((byte[])value);
              }
            else if (value instanceof Rational[])
              {
                value = Arrays.toString((Rational[])value);
              }
            else if (value instanceof Object[])
              {
                value = Arrays.toString((Object[])value);
              }
            else if (value instanceof Date)
              {
                value = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                     .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
              }

            final var s = String.format("%s [%d] %s: %s",
                                        directoryName, tag, directory.getTagInfo(tag).map(Tag::getName).orElse(""),
                                        value);
            log.info("{}", s);
            strings.add(s);
          }
      }
  }
