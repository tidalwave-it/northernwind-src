/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.spi.RequestResettable;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link RequestLocaleManager}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultRequestLocaleManager implements RequestLocaleManager, RequestResettable
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    private final ThreadLocal<Locale> localeHolder = new ThreadLocal<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Locale> getLocales()
      {
        final Locale requestLocale = localeHolder.get();
        final List<Locale> locales = new ArrayList<>(siteProvider.get().getSite().getConfiguredLocales());

        if (requestLocale != null)
          {
            locales.remove(requestLocale);
            locales.add(0, requestLocale);
          }

        log.debug(">>>> locales: {}", locales);

        return locales;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getLocaleSuffixes()
      {
        final List<String> suffixes = new ArrayList<>();
        suffixes.add("");

        for (final Locale locale : getLocales())
          {
            suffixes.add("_" + locale.getLanguage());
          }

        return suffixes;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean setLocale (final @Nonnull Locale locale)
      {
        if (siteProvider.get().getSite().getConfiguredLocales().contains(locale))
            {
              log.debug("setting locale to {} ...", locale);
              localeHolder.set(locale);
              return true;
            }
          else
            {
              log.warn("Can't set locale to {}, not in the configured ones", locale);
              return false;
            }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void requestReset()
      {
        localeHolder.remove();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DateTimeFormatter getDateTimeFormatter()
      {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                .withLocale(getLocales().get(0))
                                .withZone(ZoneId.systemDefault());
      }
  }
