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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
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
 *
 **********************************************************************************************************************/
@Scope(value = "session") @Slf4j
public class DefaultSiteViewController implements SiteViewController
  {
    @Inject
    private List<RequestResettable> requestResettables;

    @Inject
    private List<RequestProcessor> requestProcessors;

    @Inject
    private RequestHolder requestHolder;

    @Inject
    private ResponseHolder<?> responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public <RESPONSE_TYPE> RESPONSE_TYPE processRequest (final @Nonnull Request request)
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

            return (RESPONSE_TYPE)responseHolder.get();
          }
        catch (NotFoundException e)
          {
            log.warn("processing: {} - {}", request, e.toString());
            return (RESPONSE_TYPE)responseHolder.response().forException(e).build();
          }
        catch (HttpStatusException e)
          {
            if (e.isError())
              {
                log.warn("processing: " + request, e);
              }

            return (RESPONSE_TYPE)responseHolder.response().forException(e).build();
          }
        catch (Exception e)
          {
            log.error("processing: " + request, e);
            return (RESPONSE_TYPE)responseHolder.response().forException(e).build();
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
        requestProcessors.sort(new AnnotationAwareOrderComparator());
        log.info(">>>> requestProcessors:");
        requestProcessors.forEach(p -> log.info(">>>>>>>> {}", p));
      }
  }
