/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.util;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.SiteProvider;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class NorthernWindResponseHeaderFilter extends FilterSupport
  {
    private static final String HEADER_NORTHERNWIND_VERSION = "X-NorthernWind-Version";

    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void doFilter (final @Nonnull ServletRequest request,
                          final @Nonnull ServletResponse response,
                          final @Nonnull FilterChain chain)
      throws IOException, ServletException
      {
        addNorthernWindHeader(request, response);
        chain.doFilter(request, response);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void addNorthernWindHeader (final @Nonnull ServletRequest request, final @Nonnull ServletResponse response)
      throws IOException, ServletException
      {
        if (bootThrowable == null)
          {
            ((HttpServletResponse)response).addHeader(HEADER_NORTHERNWIND_VERSION,
                                                      siteProvider.get().getVersionString());
          }
      }
  }
