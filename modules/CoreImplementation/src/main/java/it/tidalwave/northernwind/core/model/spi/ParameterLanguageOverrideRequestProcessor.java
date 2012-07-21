/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Locale;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.impl.model.DefaultRequestLocaleManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(2000) @Slf4j // order must come after HeaderLanguageOverrideRequestProcessor
public class ParameterLanguageOverrideRequestProcessor implements RequestProcessor, RequestResettable
  {
    @Getter @Setter @Nonnull
    private String parameterName = "l";
            
    @Inject @Nonnull
    private DefaultRequestLocaleManager requestLocaleManager;
    
    private final ThreadLocal<String> parameterValueHolder = new ThreadLocal<String>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (final @Nonnull Request request) 
      {
        try
          {
            final String parameterValue = request.getParameter(parameterName);
            parameterValueHolder.set(parameterValue);
            requestLocaleManager.setRequestLocale(new Locale(parameterValue));
          }
        catch (NotFoundException e) 
          {
            // ok, no override
          }

        return CONTINUE;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getParameterValue()
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(parameterValueHolder.get(), "parameterValue");  
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void requestReset() 
      {
        parameterValueHolder.remove();
      }
  }
