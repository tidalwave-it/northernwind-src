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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * This class holds a response object to be served. It's an abstract class: concrete descendants are supposed to 
 * create concrete responses adapting to a specific technology (e.g. Spring MVC, Jersey, etc...).
 * 
 * @param  <ResponseType> the produced response
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j // FIXME: turn into an interface, move implementaton to Core Default Implementation
public abstract class ResponseHolder<ResponseType> implements RequestResettable
  {
    protected static final int STATUS_PERMANENT_REDIRECT = 301;

    protected static final String HEADER_CONTENT_LENGTH = "Content-Length";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";
    protected static final String HEADER_EXPIRES = "Expires";
    protected static final String HEADER_LOCATION = "Location";
    protected static final String HEADER_IF_NOT_MODIFIED_SINCE = "If-Modified-Since";

    protected static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String[] DATE_FORMATS = new String[] 
      {
        PATTERN_RFC1123,
        "EEE, dd-MMM-yy HH:mm:ss zzz",
        "EEE MMM dd HH:mm:ss yyyy"
      };
    
    private final ThreadLocal<Object> threadLocal = new ThreadLocal<>();
//    private final ThreadLocal<ResponseType> threadLocal = new ThreadLocal<ResponseType>();

    /*******************************************************************************************************************
     *
     * A support for a builder of {@link ResponseHolder}.
     *
     * @param <RESPONSE_TYPE>  the produced response
     * 
     ******************************************************************************************************************/
    @NotThreadSafe
    public abstract class ResponseBuilderSupport<RESPONSE_TYPE>
      {
        protected Object body = new byte[0];

        protected int httpStatus = HttpServletResponse.SC_OK;

        @Nullable
        protected String requestIfNoneMatch;

        @Nullable
        protected DateTime requestIfModifiedSince;

        @Nonnull
        public abstract ResponseBuilderSupport<RESPONSE_TYPE> withHeader (@Nonnull String header, @Nonnull String value);

        /***************************************************************************************************************
         *
         * Specifies a set of headers.
         *
         * @param   headers             the headers
         * @return                      itself for fluent interface style
         * 
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withHeaders (@Nonnull Map<String, String> headers)
          {
            ResponseBuilderSupport<RESPONSE_TYPE> result = this;

            for (final Entry<String, String> entry : headers.entrySet())
              {
                result = result.withHeader(entry.getKey(), entry.getValue());
              }

            return result;
          }

        /***************************************************************************************************************
         *
         * Specifies the content type.
         * 
         * @param   contentType         the content type
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withContentType (final @Nonnull String contentType)
          {
            return withHeader(HEADER_CONTENT_TYPE, contentType);
          }

        /***************************************************************************************************************
         *
         * Specifies the content length.
         * 
         * @param  contentLength        the content length
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withContentLength (final @Nonnull long contentLength)
          {
            return withHeader(HEADER_CONTENT_LENGTH, "" + contentLength);
          }

        /***************************************************************************************************************
         *
         * Specifies the content disposition.
         * 
         * @param  contentDisposition   the content disposition
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withContentDisposition (final @Nonnull String contentDisposition)
          {
            return withHeader(HEADER_CONTENT_DISPOSITION, contentDisposition);
          }

        /***************************************************************************************************************
         *
         * Specifies the expiration time.
         * 
         * @param  duration             the duration
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withExpirationTime (final @Nonnull Duration duration)
          {
            final DateTime expirationTime = getTime().plus(duration);
            return withHeader(HEADER_EXPIRES, new SimpleDateFormat(PATTERN_RFC1123, Locale.US).format(expirationTime.toDate()));
          }

        /***************************************************************************************************************
         *
         * Specifies the latest modified time.
         * 
         * @param  time                 the time
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withLatestModifiedTime (final @Nonnull DateTime time)
          {
            return withHeader(HEADER_LAST_MODIFIED, new SimpleDateFormat(PATTERN_RFC1123, Locale.US).format(time.toDate()))
                  .withHeader(HEADER_ETAG, String.format("\"%d\"", time.getMillis()));
          }

        /***************************************************************************************************************
         *
         * Specifies the body of the response. Accepted objects are: {@code byte[]}, {@code String}, 
         * {@code InputStream}.
         * 
         * @param  body                 the body 
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withBody (final @Nonnull Object body)
          {
            this.body = (body instanceof byte[]) ? body : 
                        (body instanceof InputStream) ? body :
                         body.toString().getBytes();
            return this;
          }

        /***************************************************************************************************************
         *
         * Specifies the body of the response as a {@link ResourceFile}.
         * 
         * @param  file                 the file
         * @return                      itself for fluent interface style
         * @throws IOException          if an error occurs when reading the file
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> fromFile (final @Nonnull ResourceFile file)
          throws IOException
          {
            final byte[] bytes = file.asBytes(); // TODO: this always loads, in some cases would not be needed

            return withContentType(file.getMimeType())
                  .withContentLength(bytes.length)
                  .withLatestModifiedTime(file.getLatestModificationTime())
                  .withBody(bytes);
          }

        /***************************************************************************************************************
         *
         * Specifies an exception to create the response from.
         * 
         * @param  e                    the exception
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> forException (final @Nonnull NotFoundException e)
          {
            log.info("NOT FOUND: {}", e.toString());
            return forException(new HttpStatusException(HttpServletResponse.SC_NOT_FOUND));
          }

        /***************************************************************************************************************
         *
         * Specifies an exception to create the response from.
         * 
         * @param  e                    the exception
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> forException (final @Nonnull IOException e)
          {
            log.error("", e);
            return forException(new HttpStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
          }

        /***************************************************************************************************************
         *
         * Specifies an exception to create the response from.
         * 
         * @param  e                    the exception
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> forException (final @Nonnull HttpStatusException e)
          {
            String message = String.format("<h1>HTTP Status: %d</h1>%n", e.getHttpStatus());

            switch (e.getHttpStatus()) // FIXME: get from a resource bundle
              {
                case HttpServletResponse.SC_MOVED_TEMPORARILY:
                  break;

                case HttpServletResponse.SC_NOT_FOUND:
                  message = "<h1>Not found</h1>";
                  break;

                case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                default: // FIXME: why?
                  message = "<h1>Internal error</h1>";
                  break;
              }

            return withContentType("text/html")
                  .withHeaders(e.getHeaders())
                  .withBody(message)
                  .withStatus(e.getHttpStatus());
          }
        
        /***************************************************************************************************************
         *
         * Specifies the HTTP status.
         * 
         * @param  httpStatus           the status
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withStatus (final @Nonnull int httpStatus)
          {
            this.httpStatus = httpStatus;
            return this;
          }

        /***************************************************************************************************************
         *
         * Specifies the If-None-Match header taken from the request. It's used by the caching logics.
         * 
         * @param  eTag                 the header value
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withRequestIfNoneMatch (final @Nullable String eTag) 
          {
            this.requestIfNoneMatch = eTag;
            return this;
          }

        /***************************************************************************************************************
         *
         * Specifies the If-Modified header taken from the request. It's used by the caching logics.
         * 
         * @param  dateTime             the header value
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> withRequestIfModifiedSince (final @Nullable String dateTime) 
          {
            this.requestIfModifiedSince = (dateTime == null) ? null : parseDate(dateTime);
            return this;
          }
        
        /***************************************************************************************************************
         *
         * Creates a builder for a permanent redirect.
         * 
         * @param  url                  the URL of the redirect       
         * @return                      itself for fluent interface style
         *
         **************************************************************************************************************/
        @Nonnull
        public ResponseBuilderSupport<RESPONSE_TYPE> permanentRedirect (final @Nonnull String url)
          {
            return withHeader(HEADER_LOCATION, url)
                  .withStatus(STATUS_PERMANENT_REDIRECT);
          }

        /***************************************************************************************************************
         *
         * Builds the response.
         * 
         * @return                              the response
         *
         **************************************************************************************************************/
        @Nonnull
        public final RESPONSE_TYPE build()
          {
            return cacheSupport().doBuild();
          }
        
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        public void put()
          {
            threadLocal.set(build());
          }
        
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nonnull
        protected abstract RESPONSE_TYPE doBuild();
        
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nullable
        protected abstract String getHeader (@Nonnull String header);
          
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nullable
        protected final DateTime getDateTimeHeader (final @Nonnull String header)
          {
            final String value = getHeader(header);
            return (value == null) ? null : parseDate(value);
          }
          
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nonnull
        protected ResponseBuilderSupport<RESPONSE_TYPE> cacheSupport()
          {
            final String eTag = getHeader(HEADER_ETAG);
            final DateTime lastModified = getDateTimeHeader(HEADER_LAST_MODIFIED);
            
            log.trace(">>>> eTag: {} - requestIfNoneMatch: {}", eTag, requestIfNoneMatch);
            log.trace(">>>> ifNotModifiedSince: {} - lastModified: {}", requestIfModifiedSince, lastModified);
            
            if ( ((eTag != null) && eTag.equals(requestIfNoneMatch)) ||
                 ((requestIfModifiedSince != null) && (lastModified != null) && requestIfModifiedSince.isAfter(lastModified)))
              {
                return notModified();
              }
            
            return this;
          }

        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nonnull
        private ResponseBuilderSupport<RESPONSE_TYPE> notModified() 
          {
            return withBody(new byte[0])
                  .withContentLength(0)
                  .withStatus(HttpServletResponse.SC_NOT_MODIFIED);
          }
        
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nonnull
        private DateTime parseDate (final @Nonnull String string)
          {
            for (final String dateFormat : DATE_FORMATS) 
              {
                try
                  {
                    log.debug("Parsing {} with {}...", string, dateFormat);
                    return new DateTime(new SimpleDateFormat(dateFormat).parse(string));
                  }
                catch (ParseException e) 
                  {
                    log.debug("{}", e.getMessage());
                  }
              }
            
            throw new IllegalArgumentException("Cannot parse date " + string);
          }
      }

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    public abstract ResponseBuilderSupport<ResponseType> response();

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
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
