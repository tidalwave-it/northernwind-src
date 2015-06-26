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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import javax.annotation.Nonnull;
import java.util.Map;
import it.tidalwave.northernwind.frontend.media.impl.Metadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * An interface for classes capable to interpolate some metadata items in a template.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MetadataInterpolator 
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @Getter @ToString
    public class Context
      {
        private final Metadata metadata;
        
        private final Map<String, String> lensMap;
      }
    
    /*******************************************************************************************************************
     *
     * Returns the macro that this interpolator is capable to expand.
     * 
     * @return  the macro
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getMacro();
    
    /*******************************************************************************************************************
     *
     * Interpolates a template.
     * 
     * @param  template  the template
     * @param  context   a context
     * @return           the interpolated string
     *
     ******************************************************************************************************************/
    @Nonnull
    public String interpolate (@Nonnull String template, @Nonnull Context context);
  }
