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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.imajine.image.EditableImage;
import org.imajine.image.Rational;
import org.imajine.image.metadata.XMP;
import org.imajine.image.metadata.Directory;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.op.ReadOp;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.TestHelper;
import it.tidalwave.northernwind.util.test.TestHelper.TestResource;
import java.time.format.FormatStyle;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataProviderTest
  {
    private final TestHelper helper = new TestHelper(this);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_read_metadata()
      throws Exception
      {
        // given
        final File file = helper.resourceFileFor("20100102-0001.jpg").toFile();
        final EditableImage image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        log.info("IMAGE: {}", image);
        // when
        final IPTC iptc = image.getMetadata(IPTC.class);
        final EXIF exif = image.getMetadata(EXIF.class);
        final XMP xmp = image.getMetadata(XMP.class);
        // then
        log.info("IPTC: {}", iptc);
        log.info("EXIF: {}", exif);
        log.info("XMP: {}", xmp);
        final String resourceName = String.format("MetadataDump-%s.txt", "20100102-0001");
        final TestResource tr = helper.testResourceFor(resourceName);
        final List<String> strings = new ArrayList<>();
        dumpTags(strings, "EXIF", exif);
        dumpTags(strings, "IPTC", iptc);
        dumpTags(strings, "XMP ", xmp);
        tr.writeToActualFile(strings);
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void dumpTags (final @Nonnull List<String> strings,
                           final @Nonnull String directoryName,
                           final @Nonnull Directory directory)
      {
        for (final int tag : directory.getTagCodes())
          {
            Object value = directory.getObject(tag);

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

            final String s = String.format("%s [%d] %s: %s", directoryName, tag, directory.getTagName(tag), value);
            log.info("{}", s);
            strings.add(s);
          }
      }
  }
