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
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Locale;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Request;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
import it.tidalwave.northernwind.frontend.impl.model.DefaultRequestLocaleManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class ParameterLanguageOverrideRequestProcessor implements RequestProcessor
  {
    @Getter @Setter @Nonnull
    private String parameterName = "l";
            
    @Inject @Nonnull
    private DefaultRequestLocaleManager requestLocaleManager;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean process (final @Nonnull Request request) 
      {
        try
          {
            requestLocaleManager.setRequestLocale(new Locale(request.getParameter(parameterName)));
          }
        catch (NotFoundException ex) 
          {
            // ok, no override
          }

        return false;
      }
  }
