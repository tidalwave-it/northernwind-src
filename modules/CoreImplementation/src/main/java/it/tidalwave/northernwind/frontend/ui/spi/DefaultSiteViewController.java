/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.core.model.spi.RequestResettable;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link SiteViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value = "session") @Slf4j
public class DefaultSiteViewController implements SiteViewController
  {
    @Inject @Nonnull
    private List<RequestResettable> requestResettables;

    @Inject @Nonnull
    private List<RequestProcessor> requestProcessors;

    @Inject @Nonnull
    private RequestHolder requestHolder;

    @Inject @Nonnull
    private ResponseHolder<?> responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <ResponseType> ResponseType processRequest (final @Nonnull Request request)
      {
        try
          {
            log.info("processRequest({})", request);
            resetRequestResettables();
            requestHolder.set(request);

            for (final RequestProcessor requestProcessor : requestProcessors)
              {
                log.debug(">>>> trying {} ...", requestProcessor);

                if (requestProcessor.process(request) == BREAK)
                  {
                    break;
                  }
              }

            return (ResponseType)responseHolder.get();
          }
        catch (NotFoundException e)
          {
            log.warn("processing: {} - {}", request, e.toString());
            return (ResponseType)responseHolder.response().forException(e).build();
          }
        catch (IOException e)
          {
            log.warn("processing: " + request, e);
            return (ResponseType)responseHolder.response().forException(e).build();
          }
        catch (HttpStatusException e)
          {
            if (e.getHttpStatus() != 302) //
              {
                log.warn("processing: " + request, e);
              }

            return (ResponseType)responseHolder.response().forException(e).build();
          }
        finally
          {
            resetRequestResettables();
          }
      }

    /*******************************************************************************************************************
     *
     * Resets all {@link Resettable}s.
     *
     ******************************************************************************************************************/
    private void resetRequestResettables()
      {
        for (final RequestResettable requestResettable : requestResettables)
          {
            log.debug(">>>> resetting {} ...", requestResettable);
            requestResettable.requestReset();
          }
      }

    /*******************************************************************************************************************
     *
     * Logs the {@link RequestProcessor}s.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        Collections.sort(requestProcessors, new AnnotationAwareOrderComparator());
        log.info(">>>> requestProcessors:");

        for (final RequestProcessor requestProcessor : requestProcessors)
          {
            log.info(">>>>>>>> {}", requestProcessor);
          }
      }
  }
