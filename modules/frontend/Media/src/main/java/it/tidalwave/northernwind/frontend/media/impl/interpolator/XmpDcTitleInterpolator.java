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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import javax.annotation.Nonnull;
import java.util.Map;
import org.imajine.image.metadata.XMP;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class XmpDcTitleInterpolator extends MetadataInterpolatorSupport
  {
    public XmpDcTitleInterpolator() 
      {
        super("XMP.dc.title");
      }
    
    @Override @Nonnull
    public String interpolate (final @Nonnull String template, final @Nonnull Context context)
      {
        final Map<String, String> xmpProperties = context.getMetadata().getDirectory(XMP.class).getXmpProperties();
        
        return template.replace("$" + macro + "$", formatted(xmpProperties.get("dc:title[1]")));
      }
  }