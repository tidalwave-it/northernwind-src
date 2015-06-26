/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
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
import java.io.PrintWriter;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.Directory;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.op.ReadOp;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.util.test.FileComparisonUtils.*;
import java.util.Arrays;
import org.imajine.image.Rational;
import org.imajine.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataProviderTest
  {
    @Test
    public void must_properly_read_metadata()
      throws Exception
      {
        final File file = new File("src/test/resources/images/20100102-0001.jpg");
        final EditableImage image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        log.info("IMAGE: {}", image);
        final IPTC iptc = image.getMetadata(IPTC.class);
        final EXIF exif = image.getMetadata(EXIF.class);
        final XMP xmp = image.getMetadata(XMP.class);
        log.info("IPTC: {}", iptc);
        log.info("EXIF: {}", exif);
        log.info("XMP: {}", xmp);

        final File expectedFile = new File(String.format("src/test/resources/expected-results/MetadataDump-%s.txt", "20100102-0001"));
        final File actualFile = new File(String.format("target/test-artifacts/MetadataDump-%s.txt", "20100102-0001"));
        actualFile.getParentFile().mkdirs();

        final PrintWriter pw = new PrintWriter(actualFile);
        dumpTags(pw, "EXIF", exif);
        dumpTags(pw, "IPTC", iptc);
        dumpTags(pw, "XMP ", xmp);
        pw.close();

        assertSameContents(expectedFile, actualFile);
      }

    private void dumpTags (final PrintWriter pw, 
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

            final String s = String.format("%s [%d] %s: %s", directoryName, tag, directory.getTagName(tag), value);
            log.info("{}", s);
            pw.println(s);
          }
      }
  }
