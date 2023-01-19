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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import it.tidalwave.image.EditableImage;
import it.tidalwave.image.metadata.Directory;
import it.tidalwave.image.metadata.EXIF;
import it.tidalwave.image.metadata.IPTC;
import it.tidalwave.image.metadata.TIFF;
import it.tidalwave.image.metadata.XMP;
import it.tidalwave.image.op.CreateOp;
import lombok.With;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor
class ImageTestBuilder 
  {
    @With
    private TIFF tiff;
    
    @With
    private EXIF exif;
    
    @With
    private IPTC iptc;
    
    @With
    private XMP xmp;

    @Nonnull
    public EditableImage build()
      throws Exception 
      {
        // TODO: EditableImage getMetadata() can't be mocked :-( because it's final - use PowerMock?
        final EditableImage image = EditableImage.create(new CreateOp(10, 10, EditableImage.DataType.BYTE)); // mock(EditableImage.class);
        final Field metadataMapByClassField = image.getClass().getDeclaredField("metadataMapByClass");
        metadataMapByClassField.setAccessible(true);
        final Map<Class<? extends Directory>, List<? extends Directory>> metadataMapByClass = (Map<Class<? extends Directory>, List<? extends Directory>>) metadataMapByClassField.get(image);
        metadataMapByClass.put(TIFF.class, Collections.singletonList(tiff));
        metadataMapByClass.put(EXIF.class, Collections.singletonList(exif));
        metadataMapByClass.put(IPTC.class, Collections.singletonList(iptc));
        metadataMapByClass.put(XMP.class, Collections.singletonList(xmp));
        //        when(image.getMetadata(eq(TIFF.class))).thenReturn(tiff);
        //        when(image.getMetadata(eq(EXIF.class))).thenReturn(exif);
        //        when(image.getMetadata(eq(IPTC.class))).thenReturn(iptc);
        //        when(image.getMetadata(eq(XMP.class))).thenReturn(xmp);
        
        return image;
      }
  }
