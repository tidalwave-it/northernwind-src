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
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;
import java.io.IOException;
import org.joda.time.DateTime;
import it.tidalwave.northernwind.core.model.NwFileObject;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class ResponseHolder<ResponseType> implements RequestResettable
  { 
    protected static final String HEADER_CONTENT_LENGTH = "Content-Length";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";
    protected static final String HEADER_EXPIRES = "Expires";
    protected static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final ThreadLocal<Object> threadLocal = new ThreadLocal<>();
//    private final ThreadLocal<ResponseType> threadLocal = new ThreadLocal<ResponseType>();
    
    @NotThreadSafe
    public abstract class ResponseBuilderSupport<ResponseType>
      {
        protected Object body = "";
        
        protected int httpStatus = 200;
        
        @Nonnull
        public abstract ResponseBuilderSupport<ResponseType> withHeader (@Nonnull String header, @Nonnull String value);
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withHeaders (@Nonnull Map<String, String> headers)
          {
            ResponseBuilderSupport<ResponseType> result = this;
            
            for (final Entry<String, String> entry : headers.entrySet())
              {
                result = result.withHeader(entry.getKey(), entry.getValue());
              }
            
            return result;
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withContentType (final @Nonnull String contentType)
          {
            return withHeader(HEADER_CONTENT_TYPE, contentType);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withContentLength (final @Nonnull long contentLength)
          {
            return withHeader(HEADER_CONTENT_LENGTH, "" + contentLength);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withExpirationTime (final @Nonnull Duration duration)
          {
            final Date expirationTime = getTime().plus(duration).toDate();
            return withHeader(HEADER_EXPIRES, new SimpleDateFormat(PATTERN_RFC1123).format(expirationTime));
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withLastModified (final @Nonnull Date lastModified)
          {
            return withHeader(HEADER_LAST_MODIFIED, new SimpleDateFormat(PATTERN_RFC1123).format(lastModified))
                  .withHeader(HEADER_ETAG, String.format("\"%d\"", lastModified.getTime()));
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withBody (final @Nonnull Object body)
          {
            this.body = body;
            return this;
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> fromFile (final @Nonnull NwFileObject file)
          throws IOException
          {
            final byte[] bytes = file.asBytes(); // TODO: this always loads, in some cases would not be needed

            return withContentType(file.getMIMEType())
                  .withContentLength(bytes.length)
                  .withLastModified(file.lastModified())
                  .withBody(bytes);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withStatus (final @Nonnull int httpStatus)
          {
            this.httpStatus = httpStatus;
            return this;
          }

        @Nonnull
        public ResponseBuilderSupport<ResponseType> forException (final @Nonnull NotFoundException e) 
          {
            log.info("NOT FOUND: {}", e.toString());
            return forException(new HttpStatusException(404));
          }

        @Nonnull
        public ResponseBuilderSupport<ResponseType> forException (final @Nonnull IOException e) 
          {
            log.error("", e);
            return forException(new HttpStatusException(500));
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> forException (final @Nonnull HttpStatusException e) 
          {
            String message = String.format("<h1>HTTP Status: %d</h1>%n", e.getHttpStatus());
            
            switch (e.getHttpStatus()) // FIXME: get from a resource bundle
              {
                case 302:
                  break;
                    
                case 404:
                  message = "<h1>Not found</h1>";
                  break;
                    
                case 500:
                default: // FIXME: why?
                  message = "<h1>Internal error</h1>";
                  break;
              }
            
            return withContentType("text/html")
                  .withHeaders(e.getHeaders())
                  .withBody(message) 
                  .withStatus(e.getHttpStatus());
          }
        
        @Nonnull
        public abstract ResponseType build();
        
        public void put()
          {
            threadLocal.set(build()); 
          }
      }
    
    @Nonnull
    public abstract ResponseBuilderSupport<ResponseType> response();
    
    @Nonnull
    public ResponseType get()
      {  
        return (ResponseType)threadLocal.get();   
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void requestReset()
      {
        threadLocal.remove(); 
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    protected DateTime getTime()
      {
        return new DateTime();  
      }
  }
