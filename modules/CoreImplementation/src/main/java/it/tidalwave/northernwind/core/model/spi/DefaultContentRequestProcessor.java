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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;
import static it.tidalwave.northernwind.core.model.SiteNode._SiteNode_;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Scope("session") @Order(LOWEST_PRECEDENCE)
public class DefaultContentRequestProcessor implements RequestProcessor
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    @Inject
    private SiteView siteView;

    @Inject
    private RequestHolder requestHolder;

    @Inject
    private RequestContext requestContext;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (@Nonnull final Request request)
      throws NotFoundException, HttpStatusException
      {
        try
          {
            final var relativeUri = request.getRelativeUri();
            final var site = siteProvider.get().getSite();
            final var node = site.find(_SiteNode_).withRelativeUri(relativeUri).result();
            requestContext.setNode(node);
            siteView.renderSiteNode(request, node);
            //
            // Check *after* finding the SiteNode, since a "not found" must have been already handled here.
            //
            enforceTrailingSlash(relativeUri, site);
            return BREAK;
          }
        finally
          {
            requestContext.clearNode();
          }
      }

    /*******************************************************************************************************************
     *
     * If relativeUri doesn't end with a trailing slash, send a redirect to the proper Uri.
     * FIXME: could be dropped and replaced with a configurable redirect?
     *
     ******************************************************************************************************************/
    private void enforceTrailingSlash (@Nonnull final String relativeUri, @Nonnull final Site site)
      throws HttpStatusException
      {
        final var originalRelativeUri = requestHolder.get().getOriginalRelativeUri();

        if (!relativeUri.contains(".") && !originalRelativeUri.endsWith("/"))
          {
            throw HttpStatusException.temporaryRedirect(site, relativeUri); // TODO: temporary or permanent?
          }
      }
  }
