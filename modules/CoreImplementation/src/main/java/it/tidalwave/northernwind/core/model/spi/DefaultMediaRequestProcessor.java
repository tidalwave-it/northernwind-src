/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
import org.openide.filesystems.NwFileObject;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.Media.Media;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @Order(HIGHEST_PRECEDENCE)
public class DefaultMediaRequestProcessor<ResponseType> implements RequestProcessor
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    @Inject @Nonnull
    protected ResponseHolder<ResponseType> responseHolder;

    @Getter @Setter
    private Duration duration = Duration.standardDays(7);

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (final @Nonnull Request request) 
      throws NotFoundException, IOException
      {
        final String relativeUri = request.getRelativeUri();
        
        if (relativeUri.startsWith("/media"))
          {
            final Media media = siteProvider.get().getSite().find(Media).withRelativePath(relativeUri.replaceAll("^/media", "")).result();
            final NwFileObject file = media.getFile();
            createResponse(file);
            return BREAK;
          }
        
        return CONTINUE;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected void createResponse (final @Nonnull NwFileObject file)
      throws IOException
      {
        log.info(">>>> serving contents of /{} ...", file.getPath());
        responseHolder.response().fromFile(file)
                                 .withExpirationTime(duration)
                                 .put();
      }
  }
