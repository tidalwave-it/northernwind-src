/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Request}
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @AllArgsConstructor @Getter @ToString
/* package */ class DefaultRequest implements Request
  {
    @Nonnull
    private final String baseUrl;

    @Nonnull
    private final String relativeUri;

    @Nonnull
    private final String originalRelativeUri;

    @Nonnull
    private final Map<String, List<String>> parametersMap;

    @Nonnull
    private final Map<String, List<String>> headersMap;

    @Nonnull
    private final List<Locale> preferredLocales;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getHeader (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return getMultiValuedHeader(parameterName).get(0);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getMultiValuedHeader (final @Nonnull String headerName)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(headersMap.get(headerName), headerName);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getParameter (final @Nonnull String headerName)
      throws NotFoundException
      {
        return getMultiValuedParameter(headerName).get(0);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getMultiValuedParameter (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(parametersMap.get(parameterName), parameterName);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultRequest withRelativeUri (final @Nonnull String relativeUri)
      {
        return new DefaultRequest(baseUrl,
                                  new ResourcePath(relativeUri).urlDecoded().asString(),
                                  relativeUri,
                                  parametersMap,
                                  headersMap,
                                  preferredLocales);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withBaseUrl (final @Nonnull String baseUrl)
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap, headersMap, preferredLocales);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getPathParams (final @Nonnull SiteNode siteNode)
      {
        final String siteNodeRelativeUri = siteNode.getRelativeUri().asString();
        return (relativeUri.length() <= siteNodeRelativeUri.length())
                ? ""
                : relativeUri.substring(siteNodeRelativeUri.length());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withParameterMap (final @Nonnull Map<String, String[]> httpParameterMap)
      {
        final Map<String, List<String>> parameterMap = new HashMap<>();

        for (final Entry<String, String[]> entry : httpParameterMap.entrySet())
          {
            parameterMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
          }

        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parameterMap,  headersMap, preferredLocales);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withHeaderMap (final @Nonnull Map<String, List<String>>  headersMap) 
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap,  headersMap, preferredLocales);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withPreferredLocales (final @Nonnull List<Locale> preferredLocales)
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap,  headersMap, preferredLocales);
      }
  }
