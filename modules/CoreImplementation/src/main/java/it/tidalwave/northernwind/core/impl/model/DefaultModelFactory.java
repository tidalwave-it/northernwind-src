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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.ModelFactorySupport;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.ui.Layout;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ModelFactory}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ToString
public class DefaultModelFactory extends ModelFactorySupport
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource build (@Nonnull final Resource.Builder builder)
      {
        return new DefaultResource(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Content build (@Nonnull final Content.Builder builder)
      {
        return new DefaultContent(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Media build (@Nonnull final Media.Builder builder)
      {
        return new DefaultMedia(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNode createSiteNode (@Nonnull final Site site, @Nonnull final ResourceFile folder)
      {
        return new DefaultSiteNode(this, (DefaultSite)site, folder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout build (@Nonnull final Layout.Builder builder)
      {
        return new DefaultLayout(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultRequest createRequest()
      {
        return new DefaultRequest();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Request createRequestFrom (@Nonnull final HttpServletRequest httpServletRequest)
      {
        String relativeUri = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        relativeUri = "".equals(relativeUri) ? "/" : relativeUri;
        return createRequest().withBaseUrl(getBaseUrl(httpServletRequest))
                              .withRelativeUri(relativeUri)
                              .withParameterMap(httpServletRequest.getParameterMap())
                              .withHeaderMap(toMap(httpServletRequest))
                              .withPreferredLocales(Collections.list(httpServletRequest.getLocales()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties build (@Nonnull final ResourceProperties.Builder builder)
      {
        return new DefaultResourceProperties(builder);
//        return new DefaultResourceProperties(id, DefaultResourceProperties.PropertyResolver.DEFAULT);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Site build (@Nonnull final Site.Builder builder)
      {
        return new DefaultSite(builder);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String getBaseUrl (@Nonnull final HttpServletRequest httpServletRequest)
      {
        return httpServletRequest.getRequestURL().toString().replaceAll(":.*", "")
                + "://" + httpServletRequest.getHeader("Host");
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Map<String, List<String>> toMap (@Nonnull final HttpServletRequest httpServletRequest)
      {
        final Map<String, List<String>> headerMap = new HashMap<>();

        for (final Enumeration<String> e = httpServletRequest.getHeaderNames(); e.hasMoreElements(); )
          {
            final String headerName = e.nextElement();
            final String headerValue = httpServletRequest.getHeader(headerName); // FIXME: lacks support for multivalue
            headerMap.put(headerName, List.of(headerValue));
          }

        return headerMap;
      }
  }
