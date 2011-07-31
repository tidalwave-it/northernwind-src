/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.impl.model;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import it.tidalwave.northernwind.frontend.model.RequestLocaleManager;
import it.tidalwave.northernwind.frontend.model.Site;
import java.util.ArrayList;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;

/***********************************************************************************************************************
 *
 * The default implementation of {@link RequestLocaleManager}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultRequestLocaleManager implements RequestLocaleManager
  {
    @Inject @Nonnull
    private Site site;
    
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
        final List<Locale> locales = new ArrayList<Locale>(site.getConfiguredLocales());
        
        
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
     *
     ******************************************************************************************************************/
    public void reset()
      {
        localeHolder.remove();  
      }
  }
