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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.model.DefaultRequestLocaleManager;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Order(1000) @Slf4j
public class HeaderLanguageOverrideRequestProcessor implements RequestProcessor
  {
    @Inject
    private DefaultRequestLocaleManager requestLocaleManager;

    @Inject
    private Provider<SiteProvider> siteProvider;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (@Nonnull final Request request)
      {
        final var site = siteProvider.get().getSite();

        for (final var locale : request.getPreferredLocales())
          {
            if (site.getConfiguredLocales().contains(locale))
              {
                requestLocaleManager.setLocale(locale);
                break;
              }
          }

        return CONTINUE;
      }
  }
