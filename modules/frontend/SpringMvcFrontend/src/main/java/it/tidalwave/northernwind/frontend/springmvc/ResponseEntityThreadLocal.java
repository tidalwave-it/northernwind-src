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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResponseEntityThreadLocal extends ThreadLocal<ResponseEntity<?>> 
  {
    @NotThreadSafe
    public class ResponseBuilder
      {
        private final HttpHeaders headers = new HttpHeaders();
        
        private Object body = "";
        
        private HttpStatus httpStatus = HttpStatus.OK;
        
        @Nonnull
        public ResponseBuilder withHeader (final @Nonnull String header, final @Nonnull String value)
          {
            headers.add(header, value);        
            return this;
          }
        
        @Nonnull
        public ResponseBuilder withContentType (final @Nonnull String contentType)
          {
            headers.setContentType(MediaType.parseMediaType(contentType));
            return this;
          }
        
        @Nonnull
        public ResponseBuilder withContentLenght (final @Nonnull long contentLenght)
          {
            headers.setContentLength(contentLenght);
            return this;
          }
        
        @Nonnull
        public ResponseBuilder withBody (final @Nonnull Object body)
          {
            this.body = body;
            return this;
          }
        
        @Nonnull
        public ResponseBuilder withStatus (final @Nonnull HttpStatus httpStatus)
          {
            this.httpStatus = httpStatus;
            return this;
          }
        
        @Nonnull
        public ResponseEntity<?> build()
          {
            return new ResponseEntity<Object>(body, headers, httpStatus);
          }
        
        public void put()
          {
            set(build());
          }
      }
    
    @Nonnull
    public ResponseBuilder response()
      {
        return new ResponseBuilder();  
      }
  }
