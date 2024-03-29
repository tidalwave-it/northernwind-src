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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/***********************************************************************************************************************
 *
 * Manages the {@link Locale}-related stuff for elaborating the current request.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface RequestLocaleManager
  {
    /*******************************************************************************************************************
     *
     * Explictly sets a the {@link Locale} for the current request. If the given {@link Locale} is not among those
     * configured for this site, it will be ignored.
     *
     * @return   {@code true} if the {@link Locale} has been set.
     *
     ******************************************************************************************************************/
    public boolean setLocale (@Nonnull Locale locale);

    /*******************************************************************************************************************
     *
     * Returns the {@link Locale}s for the current request. They are ordered by preference, descending.
     *
     * @return   the {@code Locale}s
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Locale> getLocales();

    /*******************************************************************************************************************
     *
     * Returns the {@link Locale} suffixes for the current request. They are ordered by preference, descending.
     *
     * @return   the {@code Locale} suffixes
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<String> getLocaleSuffixes();

    /*******************************************************************************************************************
     *
     * Returns a properly localized formatter for date and time.
     *
     * @return   the localized formatter
     *
     ******************************************************************************************************************/
    @Nonnull
    public DateTimeFormatter getDateTimeFormatter();
  }
