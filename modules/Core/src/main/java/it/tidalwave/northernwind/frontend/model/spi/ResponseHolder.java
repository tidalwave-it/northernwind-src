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
import javax.annotation.concurrent.NotThreadSafe;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class ResponseHolder<ResponseType>
  { 
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
            return withHeader("Content-Type", contentType);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withContentLenght (final @Nonnull long contentLenght)
          {
            return withHeader("Content-Length", "" + contentLenght);
          }
        
        @Nonnull
        public ResponseBuilderSupport<ResponseType> withBody (final @Nonnull Object body)
          {
            this.body = body;
            return this;
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
            threadLocal.set(build()); // FIXME
//            threadLocal.set(build());
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
