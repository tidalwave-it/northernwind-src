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
import it.tidalwave.image.metadata.Directory;
import it.tidalwave.northernwind.core.model.ResourceProperties;

/***********************************************************************************************************************
 *
 * A container of metadata for a given media item.
 * 
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface Metadata
  {
    /*******************************************************************************************************************
     *
     * Interpolates the given template expanding macros with values from metadata items.
     * 
     * @param template    the template
     * @param properties  some properties for customization 
     *
     ******************************************************************************************************************/
    @Nonnull
    public String interpolateString (@Nonnull String template, @Nonnull ResourceProperties properties);

    /*******************************************************************************************************************
     *
     * Returns a metadata directory (e.g. TIFF, EXIF, etc...)
     * 
     * @param   directoryClass  the directory type
     * @return  the directory
     * 
     ******************************************************************************************************************/
    @Nonnull
    public <T extends Directory> T getDirectory (@Nonnull Class<T> directoryClass);
  }
