/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.media.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.imajine.image.EditableImage;
import org.imajine.image.metadata.EXIF;
import org.imajine.image.metadata.IPTC;
import org.imajine.image.metadata.XMP;
import org.imajine.image.op.ReadOp;
import org.testng.annotations.Test;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class EmbeddedMediaMetadataProviderTest
  {
    @Test
    public void test1()
      throws Exception
      {
        final File file = new File("/Users/fritz/Personal/WebSites/StoppingDown.net/ExternalMedia/stillimages/100/20100102-0001.jpg");
        final EditableImage image = EditableImage.create(new ReadOp(file, ReadOp.Type.METADATA));
        System.err.println("IMAGE: " + image);
        final IPTC iptc = image.getMetadata(IPTC.class);
        final EXIF exif = image.getMetadata(EXIF.class);
        final XMP xmp = image.getMetadata(XMP.class);
//          System.err.println("IPTC: " + iptc + " " + Arrays.toString(iptc.getTagCodes()) + " / " + iptc.getByline());
//          System.err.println("EXIF: " + exif);

        System.err.println("EXIF");
          
        for (int exifTag : exif.getTagCodes())
          {
            System.err.printf("[%d] %s: %s%n", exifTag, exif.getTagName(exifTag), exif.getObject(exifTag));  
          }
          
        System.err.println("IPTC");
          
        for (int iptcTag : iptc.getTagCodes())
          {
            System.err.printf("[%d] %s: %s%n", iptcTag, iptc.getTagName(iptcTag), iptc.getObject(iptcTag));  
          }
          
        System.err.println("XMP");
          
        for (int xmpTag : xmp.getTagCodes())
          {
            System.err.printf("[%d] %s: %s%n", xmpTag, xmp.getTagName(xmpTag), xmp.getObject(xmpTag));  
          }
        
        System.err.println("XMP 2");
        
        final Map<String, String> map = new TreeMap<String, String>(xmp.getXmpProperties());
        
        for (final Entry<String, String> e : map.entrySet())
          {
            System.err.printf("%s: %s%n", e.getKey(), e.getValue());  
          }
      }  
  }
