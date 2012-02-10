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
import java.io.IOException;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * This {@link RequestProcessor} returns an HTTP 503 status code, with a proper readable message, when the site is not
 * available.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class AvailabilityEnforcerRequestProcessor implements RequestProcessor
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    @Getter @Setter // TODO: required for test, try to drop
    @Inject @Nonnull
    private ResponseHolder<?> responseHolder;

    @Override @Nonnull
    public Status process (final @Nonnull Request request)
      throws NotFoundException, IOException, HttpStatusException 
      {
        if (siteProvider.get().isSiteAvailable())
          {
            return CONTINUE;
          }
        
        log.warn("Site unavailable, sending maintenance page");
        // TODO: use a resource
        final String page = String.format("<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<meta http-equiv=\"Refresh\" content=\"15; url=%s%s\"/>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div style=\"padding: 5%% 0pt;\">\n"
                + "<div style=\"padding: 10%% 0pt; text-align: center\">"
                + "<p style=\"font-family: sans-serif; font-size: 24px\">Site under maintenance, please retry in a short time.<br/>"
                + "This page will reload automatically.</p>"
                + "</div>\n"
                + "</div>\n"
                + "</body>\n"
                + "</html>", request.getBaseUrl(), request.getOriginalRelativeUri());
        responseHolder.response().withContentType("text/html")
                                 .withStatus(503)
                                 .withExpirationTime(new Duration(0))
                                 .withBody(page)
                                 .put();   
        return BREAK;
      }
  }