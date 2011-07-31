/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Request;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
import it.tidalwave.northernwind.frontend.model.spi.RequestResettable;
import it.tidalwave.northernwind.frontend.model.spi.ResponseHolder;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link SiteViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class DefaultSiteViewController implements SiteViewController
  {
    @Inject @Nonnull
    private List<RequestResettable> requestResettables;
    
    @Getter @Setter @Nonnull
    private List<RequestProcessor> requestProcessors;
    
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
            
            for (final RequestProcessor requestProcessor : requestProcessors)
              {
                log.debug(">>>> trying {} ...", requestProcessor);
                
                if (requestProcessor.process(request))
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
        finally
          {
            resetRequestResettables();
          }
      }
    
    /*******************************************************************************************************************
     *
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
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void logConfiguration()
      {
        log.info(">>>> uriHandlers: {}", requestProcessors);  
      }
  } 
