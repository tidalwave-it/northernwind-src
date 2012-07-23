/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultRequestLocaleManager implements RequestLocaleManager, RequestResettable
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    private final ThreadLocal<Locale> localeHolder = new ThreadLocal<Locale>();
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Locale> getLocales() 
      {
        final Locale requestLocale = localeHolder.get();
        final List<Locale> locales = new ArrayList<Locale>(siteProvider.get().getSite().getConfiguredLocales());

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
     *
     ******************************************************************************************************************/
    public void setRequestLocale (final @Nonnull Locale locale)
      {
        log.debug("setRequestLocale({})", locale);
        localeHolder.set(locale);            
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
        return DateTimeFormat.fullDateTime().withLocale(getLocales().get(0));
      }
  }
