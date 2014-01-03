/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.vaadin;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder.ResponseBuilderSupport;
import com.vaadin.terminal.DownloadStream;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DownloadStreamHolder extends ResponseHolder<DownloadStream>
  {
    @NotThreadSafe
    public class ResponseBuilder extends ResponseBuilderSupport<DownloadStream>
      {
        private MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

        @Override @Nonnull
        public ResponseBuilder withHeader (final @Nonnull String header, final @Nonnull String value)
          {
            headers.add(header, value);
            return this;
          }

        @Override @Nonnull
        public ResponseBuilder forException (final @Nonnull NotFoundException e)
          {
            return (ResponseBuilder)withContentType("text/plain")
                  .withBody(e.getMessage())
                  .withStatus(404);
          }

        @Override @Nonnull
        public ResponseBuilder forException (final @Nonnull IOException e)
          {
            return (ResponseBuilder)withContentType("text/plain")
                  .withBody(e.getMessage())
                  .withStatus(500);
          }

        @Override @Nonnull
        public DownloadStream build()
          {
            return new DownloadStream((InputStream)body, null, headers.get("Content-Type").get(0)); // FIXME: set name?
          }
      }

    @Override @Nonnull
    public ResponseBuilder response()
      {
        return new ResponseBuilder();
      }
  }
