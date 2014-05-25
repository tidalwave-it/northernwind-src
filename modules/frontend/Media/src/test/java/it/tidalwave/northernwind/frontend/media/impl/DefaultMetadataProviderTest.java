/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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

import java.io.File;
import java.util.Arrays;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.op.ReadOp;
import org.testng.annotations.Test;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultMetadataProviderTest
  {
    @Test(enabled=false)
    public void test1()
      throws Exception
      {
        final File file = new File("/Users/fritz/Personal/WebSites/StoppingDown.net/ExternalMedia/stillimages/200/20100102-0001.jpg");
        final EditableImage image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        System.err.println("IMAGE: " + image);
        final IPTC iptc = image.getMetadata(IPTC.class);
        final EXIF exif = image.getMetadata(EXIF.class);
          System.err.println("IPTC: " + iptc + " " + Arrays.toString(iptc.getTagCodes()) + " / " + iptc.getByline());
          System.err.println("EXIF: " + exif);

        for (int iptcTag : iptc.getTagCodes())
          {
            System.err.printf("[%d] %s: %s\n", iptcTag, iptc.getTagName(iptcTag), iptc.getObject(iptcTag));
          }
      }
  }
