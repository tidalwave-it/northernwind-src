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
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Request;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.frontend.model.RequestProcessor.Status.*;

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
    private ResponseHolder<?> responseHolder;

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
        
        if (relativeUri.startsWith("/css"))
          {
            try
              {
                responseHolder.response().withContentType("text/css").withBody(loadCss(relativeUri)).put();  
                return BREAK;
              }
            catch (FileNotFoundException e)
              {
                log.info("Cannot find {}, continuing...", relativeUri);  
              }
          }
        
        return CONTINUE;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String loadCss (final @Nonnull String path)
      throws IOException
      {
        final Resource htmlResource = new ClassPathResource(path, getClass());  
        final @Cleanup Reader r = new InputStreamReader(htmlResource.getInputStream());
        log.info(">>>> serving contents of {} ...", path);            
        final CharBuffer charBuffer = CharBuffer.allocate((int)htmlResource.contentLength());
        final int length = r.read(charBuffer);
        r.close();
        return new String(charBuffer.array(), 0, length);
      }  
  }
