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
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Order(LOWEST_PRECEDENCE)
public class DefaultContentRequestProcessor implements RequestProcessor 
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    @Inject @Nonnull
    private SiteView siteView;
    
    @Inject @Nonnull
    private RequestHolder requestHolder;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (final @Nonnull Request request)
      throws NotFoundException, IOException, HttpStatusException 
      {
        final String relativeUri = request.getRelativeUri();
        final Site site = siteProvider.get().getSite();
        siteView.renderSiteNode(site.find(SiteNode).withRelativeUri(relativeUri).result());
        //
        // Check *after* finding the SiteNode, since a not found must be properly handled.
        //
        final String originalRelativeUri = requestHolder.get().getOriginalRelativeUri();
        
        if (!relativeUri.contains(".") && !originalRelativeUri.endsWith("/"))
          {
            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("Location", site.createLink(relativeUri + "/"));
            throw new HttpStatusException(302, headers);
          }
        
        return BREAK;
      }
  }
