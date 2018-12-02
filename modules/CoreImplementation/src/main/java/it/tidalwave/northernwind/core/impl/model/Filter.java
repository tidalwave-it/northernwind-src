/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;

/***********************************************************************************************************************
 *
 * A text manipulator that performs some kind of transformation of an input string.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface Filter
  {
    public static final Class<Filter> Filter = Filter.class;

    /*******************************************************************************************************************
     *
     * Filters an input text.
     *
     * @param   text        the input text
     * @param   mimeType    the MIME type of the input text
     * @return              the filtered text
     *
     ******************************************************************************************************************/
    @Nonnull
    public String filter (@Nonnull String text, @Nonnull String mimeType);
  }
