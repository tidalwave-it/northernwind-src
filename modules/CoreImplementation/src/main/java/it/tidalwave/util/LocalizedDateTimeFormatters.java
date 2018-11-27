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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.util;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/***********************************************************************************************************************
 *
 * A factory class for localized {@link DateTimeFormatter}s in various flavours, specified by the {@link FormatStyle}.
 * This class is especially useful for migration to JDK 9+, where the default behaviour of
 * {@code DateTimeFormatter.ofLocalizedDate/DateTime(...)} has changed.
 *
 * At the moment only locales for English and Italian are supported.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalizedDateTimeFormatters
  {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class BundleUtilities // FIXME: push to TFT
    {
      /*******************************************************************************************************************
       *
       *
       *
       ******************************************************************************************************************/
      @Nonnull
      public static String getMessage (final @Nonnull Class<?> ownerClass,
                                       final @Nonnull String resourceName,
                                       final @Nonnull Object ... params)
      {
        return getMessage(ownerClass, Locale.getDefault(), resourceName, params);
      }
      /*******************************************************************************************************************
       *
       *
       *
       ******************************************************************************************************************/
      @Nonnull
      public static String getMessage (final @Nonnull Class<?> ownerClass,
                                       final @Nonnull Locale locale,
                                       final @Nonnull String resourceName,
                                       final @Nonnull Object ... params)
      {
        final String packageName = ownerClass.getPackage().getName();
        final ResourceBundle bundle = ResourceBundle.getBundle(packageName + ".Bundle", locale);
        final String string = bundle.getString(resourceName);

        return (params.length == 0) ? string : String.format(string, params);
      }
    }

    /*******************************************************************************************************************
     *
     * Returns a formatter.
     *
     * @param   style   the style
     * @param   locale  the locale
     * @return          the formatter
     *
     ******************************************************************************************************************/
    @Nonnull
    public static DateTimeFormatter getDateTimeFormatterFor (final @Nonnull FormatStyle style,
                                                             final @Nonnull Locale locale)
      {
        final String pattern = BundleUtilities.getMessage(LocalizedDateTimeFormatters.class,
                                                          locale,
                                            "dateTimeFormatterPattern." + style.name());
        return DateTimeFormatter.ofPattern(pattern).withLocale(locale);
      }
  }
