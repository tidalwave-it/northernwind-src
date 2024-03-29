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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Request}
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Immutable @AllArgsConstructor(access = PRIVATE) @Getter @ToString
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
     *
     ******************************************************************************************************************/
    public DefaultRequest()
      {
        this("", "", "", new HashMap<>(), new HashMap<>(), new ArrayList<>());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Optional<String> getHeader (@Nonnull final String parameterName)
      {
        return getMultiValuedHeader(parameterName).stream().findFirst();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getMultiValuedHeader (@Nonnull final String headerName)
      {
        return headersMap.getOrDefault(headerName, emptyList());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Optional<String> getParameter (@Nonnull final String headerName)
      {
        return getMultiValuedParameter(headerName).stream().findFirst();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getMultiValuedParameter (@Nonnull final String parameterName)
      {
        return parametersMap.getOrDefault(parameterName, emptyList());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultRequest withRelativeUri (@Nonnull final String relativeUri)
      {
        return new DefaultRequest(baseUrl,
                                  ResourcePath.of(relativeUri).urlDecoded().asString(),
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
    public DefaultRequest withBaseUrl (@Nonnull final String baseUrl)
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap, headersMap, preferredLocales);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourcePath getPathParams (@Nonnull final SiteNode siteNode)
      {
        final var siteNodeRelativeUri = siteNode.getRelativeUri().asString();
        return (relativeUri.length() <= siteNodeRelativeUri.length())
                ? ResourcePath.EMPTY
                : ResourcePath.of(relativeUri.substring(siteNodeRelativeUri.length()));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withParameterMap (@Nonnull final Map<String, String[]> httpParameterMap)
      {
        final Map<String, List<String>> parameterMap = new HashMap<>();

        for (final var entry : httpParameterMap.entrySet())
          {
            parameterMap.put(entry.getKey(), List.of(entry.getValue()));
          }

        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parameterMap,  headersMap, preferredLocales);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withHeaderMap (@Nonnull final Map<String, List<String>>  headersMap)
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap,  headersMap, preferredLocales);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withPreferredLocales (@Nonnull final List<Locale> preferredLocales)
      {
        return new DefaultRequest(baseUrl, relativeUri, originalRelativeUri, parametersMap,  headersMap, preferredLocales);
      }
  }
