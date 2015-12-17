/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Locale;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * An object representing an incoming request.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable
public interface Request
  {
    /*******************************************************************************************************************
     *
     * Creates a clone with the given relative URI
     *
     * @param   relativeUri  the relative URI
     * @return               the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public Request withRelativeUri (@Nonnull String relativeUri);

    /*******************************************************************************************************************
     *
     * Returns the base URL of this request.
     *
     * @return  the base URL
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getBaseUrl();

    /*******************************************************************************************************************
     *
     * Returns the relative URI of this request.
     *
     * @return  the relative URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getRelativeUri();

    /*******************************************************************************************************************
     *
     * Returns the original relative URI of this request.
     *
     * @return  the original relative URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getOriginalRelativeUri();

    /*******************************************************************************************************************
     *
     * Returns the locales preferred by the client originating this request.
     *
     * @return  the preferred locales
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Locale> getPreferredLocales();

    /*******************************************************************************************************************
     *
     * Returns a header value.
     *
     * @param  headerName     the name of the header
     * @return                the value of the header
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getHeader (@Nonnull String headerName)
      throws NotFoundException;

    /*******************************************************************************************************************
     *
     * Returns a header value.
     *
     * @param  headerName     the name of the header
     * @return                the value of the header
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<String> getMultiValuedHeader (@Nonnull String headerName)
      throws NotFoundException;
    
    /*******************************************************************************************************************
     *
     * Returns a parameter value.
     *
     * @param  parameterName  the name of the parameter
     * @return                the value of the parameter
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getParameter (@Nonnull String parameterName)
      throws NotFoundException;

    /*******************************************************************************************************************
     *
     * Returns a parameter value in form of a list.
     *
     * @param  parameterName  the name of the parameter
     * @return                the value of the parameter
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<String> getMultiValuedParameter (@Nonnull String parameterName)
      throws NotFoundException;

    /*******************************************************************************************************************
     *
     * Returns the path params.
     *
     * @param  siteNode
     * @return           the path params
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getPathParams (@Nonnull SiteNode siteNode);
  }
