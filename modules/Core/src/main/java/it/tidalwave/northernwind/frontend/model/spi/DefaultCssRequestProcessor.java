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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.net.URL;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class DefaultCssRequestProcessor implements RequestProcessor
  {
    @Inject @Nonnull
    private ResponseHolder<?> responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean handleUri (final @Nonnull URL context, final @Nonnull String relativeUri) 
      throws NotFoundException, IOException
      {
        if (relativeUri.startsWith("/css"))
          {
            final String path = relativeUri.replaceAll("^/css", "");
            responseHolder.response().withContentType("text/css").withBody(loadCss(path)).put();  
            return true;
          }
        
        return false;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String loadCss (final @Nonnull String path)
      throws IOException
      {
        final String resourcePath = "/css" + path;
        log.info(">>>> serving contents of {} ...", resourcePath);            
        final Resource htmlResource = new ClassPathResource(resourcePath, getClass());  
        final @Cleanup Reader r = new InputStreamReader(htmlResource.getInputStream());
        final CharBuffer charBuffer = CharBuffer.allocate((int)htmlResource.contentLength());
        final int length = r.read(charBuffer);
        r.close();
        return new String(charBuffer.array(), 0, length);
      }  
  }
