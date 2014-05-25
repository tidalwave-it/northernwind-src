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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilderSupport;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ResponseEntityHolder extends ResponseHolder<ResponseEntity<?>>
  {
    @NotThreadSafe
    public class ResponseBuilder extends ResponseBuilderSupport<ResponseEntity<?>>
      {
        private final HttpHeaders headers = new HttpHeaders();

        @Override @Nonnull
        public ResponseBuilder withHeader (final @Nonnull String header, final @Nonnull String value)
          {
            headers.add(header, value);
            return this;
          }
        
        @Override
        protected String getHeader (final @Nonnull String header)
          {
            final List<String> g = headers.get(header);
            return ((g == null) || g.isEmpty()) ? null : g.get(0);
          }

        @Override @Nonnull
        public ResponseBuilder withContentType (final @Nonnull String contentType)
          {
            headers.setContentType(MediaType.parseMediaType(contentType));
            return this;
          }

        @Override @Nonnull
        public ResponseBuilder withContentLength (final @Nonnegative long contentLenght)
          {
            headers.setContentLength(contentLenght);
            return this;
          }

        @Override @Nonnull
        protected ResponseEntity<?> doBuild()
          {
            return new ResponseEntity<>(body, headers, HttpStatus.valueOf(httpStatus));
          }
      }

    @Override @Nonnull
    public ResponseBuilder response()
      {
        return new ResponseBuilder();
      }
  }
