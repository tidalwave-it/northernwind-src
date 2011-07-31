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

import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.openide.filesystems.FileObject;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class ResponseHolder<ResponseType>
  { 
    protected static final String HEADER_CONTENT_LENGTH = "Content-Length";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";
    protected static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final ThreadLocal<Object> threadLocal = new ThreadLocal<Object>();
//    private final ThreadLocal<ResponseType> threadLocal = new ThreadLocal<ResponseType>();
    
    @NotThreadSafe
    public abstract class ResponseBuilderSupport<ResponseType>
      {
        protected Object body = "";
        
        protected int httpStatus = 200;
        
        @Nonnull
        public abstract ResponseBuilderSupport<ResponseType> withHeader (@Nonnull String header, @Nonnull String value);
        
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
        public ResponseBuilderSupport<ResponseType> withBody (final @Nonnull Object body)
          {
            this.body = body;
            return this;
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> fromFile (final @Nonnull FileObject file)
          throws IOException
          {
            final byte[] bytes = file.asBytes(); // TODO: this always loads, in some cases would not be needed

            return withContentType(file.getMIMEType())
                  .withContentLength(bytes.length)
                  .withHeader(HEADER_LAST_MODIFIED, new SimpleDateFormat(PATTERN_RFC1123).format(file.lastModified()))
                  .withHeader(HEADER_ETAG, String.format("\"%d\"", file.lastModified().getTime()))
                  .withBody(bytes);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withStatus (final @Nonnull int httpStatus)
          {
            this.httpStatus = httpStatus;
            return this;
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
    
    public void clear()
      {
        threadLocal.set(null);  
      }
  }
