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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.impl.model.MacroSetExpander;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class DefaultCssRequestProcessor implements RequestProcessor
  {
    @Inject @Nonnull
    private Provider<Site> site;
    
    @Inject @Nonnull
    private Provider<MacroSetExpander> macroExpander;
    
    @Inject @Nonnull
    private ResponseHolder<?> responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (final @Nonnull Request request) 
      {
        final String relativeUri = request.getRelativeUri();
        
        if (relativeUri.startsWith("/css"))
          {
            try
              {
                final Resource cssResource = site.get().find(Resource.class).withRelativePath(relativeUri).result();
                final FileObject file = cssResource.getFile();
                final String cssText = macroExpander.get().filter(file.asText());
                responseHolder.response().withContentType(file.getMIMEType()).withBody(cssText).put();  
                return BREAK;
              }
            catch (IOException e)
              {
                log.info("Cannot find {}, continuing...", relativeUri);  
              }
            catch (NotFoundException e)
              {
                log.info("Cannot find {}, continuing...", relativeUri);  
              }
          }
        
        return CONTINUE;
      }
  }
