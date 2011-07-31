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
import java.util.List;
import java.io.IOException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
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
    @Getter @Setter @Nonnull
    private List<RequestProcessor> requestProcessors;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void processRequest (final @Nonnull URL context, final @Nonnull String relativeUri) 
      throws HttpErrorException
      {
        try
          {
            log.info("processRequest({}, {})", context, relativeUri);
            
            for (final RequestProcessor requestProcessor : requestProcessors)
              {
                log.debug(">>>> trying {} ...", requestProcessor);
                
                if (requestProcessor.process(context, relativeUri))
                  {
                    break;  
                  }
              }
          }
        catch (NotFoundException e) 
          {
            throw new HttpErrorException(404, e); 
          }
        catch (IOException e) 
          {
            throw new HttpErrorException(500, e); 
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
