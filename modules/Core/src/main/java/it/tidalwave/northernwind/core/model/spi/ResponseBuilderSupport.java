/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.io.IOException;
import java.io.InputStream;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourceFile;
import static javax.servlet.http.HttpServletResponse.*;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A partial implementation of {@link ResponseBuilder}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j // FIXME: move to Core Default Implementation?
public abstract class ResponseBuilderSupport<RESPONSE_TYPE> implements ResponseBuilder<RESPONSE_TYPE>
  {
    protected static final String HEADER_CONTENT_LENGTH = "Content-Length";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";
    protected static final String HEADER_EXPIRES = "Expires";
    protected static final String HEADER_LOCATION = "Location";
    protected static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    protected static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    protected static final String HEADER_CACHE_CONTROL = "Cache-Control";

    protected static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String[] DATE_FORMATS = new String[] 
      {
        PATTERN_RFC1123,
        "EEE, dd-MMM-yy HH:mm:ss zzz",
        "EEE MMM dd HH:mm:ss yyyy"
      };

    /** The body of the response. */
    @Nonnull
    protected Object body = new byte[0];

    /** The HTTP status of the response. */
    protected int httpStatus = SC_OK;

    /** The If-None-Match header specified in the request we're responding to. */
    @Nullable
    protected String requestIfNoneMatch;

    /** The If-Modified-Since header specified in the request we're responding to. */
    @Nullable
    protected DateTime requestIfModifiedSince;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public abstract ResponseBuilder<RESPONSE_TYPE> withHeader (@Nonnull String header, @Nonnull String value);

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withHeaders (final @Nonnull Map<String, String> headers)
      {
        ResponseBuilder<RESPONSE_TYPE> result = this;

        for (final Map.Entry<String, String> entry : headers.entrySet())
          {
            result = result.withHeader(entry.getKey(), entry.getValue());
          }

        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentType (final @Nonnull String contentType)
      {
        return withHeader(HEADER_CONTENT_TYPE, contentType);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentLength (final @Nonnull long contentLength)
      {
        return withHeader(HEADER_CONTENT_LENGTH, "" + contentLength);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentDisposition (final @Nonnull String contentDisposition)
      {
        return withHeader(HEADER_CONTENT_DISPOSITION, contentDisposition);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withExpirationTime (final @Nonnull Duration duration)
      {
        final DateTime expirationTime = getCurrentTime().plus(duration);
        return withHeader(HEADER_EXPIRES, createFormatter(PATTERN_RFC1123).format(expirationTime.toDate()))
              .withHeader(HEADER_CACHE_CONTROL, String.format("max-age=%d", duration.toStandardSeconds().getSeconds()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withLatestModifiedTime (final @Nonnull DateTime time)
      {
        return withHeader(HEADER_LAST_MODIFIED, createFormatter(PATTERN_RFC1123).format(time.toDate()))
              .withHeader(HEADER_ETAG, String.format("\"%d\"", time.getMillis()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withBody (final @Nonnull Object body)
      {
        this.body = (body instanceof byte[]) ? body : 
                    (body instanceof InputStream) ? body :
                     body.toString().getBytes();
        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> fromFile (final @Nonnull ResourceFile file)
      throws IOException
      {
        final byte[] bytes = file.asBytes(); // TODO: this always loads, in some cases would not be needed

        return withContentType(file.getMimeType())
              .withContentLength(bytes.length)
              .withLatestModifiedTime(file.getLatestModificationTime())
              .withBody(bytes);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forRequest (final @Nonnull Request request) 
      {    
        try // FIXME: this would be definitely better with Optional
          {
            this.requestIfNoneMatch = request.getHeader(HEADER_IF_NONE_MATCH);
          }
        catch (NotFoundException e)
          {
            // never mind  
          }

        try // FIXME: this would be definitely better with Optional
          {
            this.requestIfModifiedSince = parseDate(request.getHeader(HEADER_IF_MODIFIED_SINCE));
          }
        catch (NotFoundException e)
          {
            // never mind  
          }

        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (final @Nonnull NotFoundException e)
      {
        log.info("NOT FOUND: {}", e.toString());
        return forException(new HttpStatusException(SC_NOT_FOUND));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (final @Nonnull Throwable e)
      {
        log.error("", e);
        return forException(new HttpStatusException(SC_INTERNAL_SERVER_ERROR));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (final @Nonnull HttpStatusException e)
      {
        String message = String.format("<h1>HTTP Status: %d</h1>%n", e.getHttpStatus());

        switch (e.getHttpStatus()) // FIXME: get from a resource bundle
          {
            case SC_MOVED_TEMPORARILY:
              break;

            case SC_NOT_FOUND:
              message = "<h1>Not found</h1>";
              break;

            case SC_INTERNAL_SERVER_ERROR:
            default: // FIXME: why?
              message = "<h1>Internal error</h1>";
              break;
          }

        return withContentType("text/html")
              .withHeaders(e.getHeaders())
              .withBody(message)
              .withStatus(e.getHttpStatus());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withStatus (final @Nonnull int httpStatus)
      {
        this.httpStatus = httpStatus;
        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> permanentRedirect (final @Nonnull String url)
      {
        return withHeader(HEADER_LOCATION, url)
              .withStatus(SC_MOVED_PERMANENTLY);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final RESPONSE_TYPE build()
      {
        return ((ResponseBuilderSupport<RESPONSE_TYPE>)cacheSupport()).doBuild();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override 
    public void put()
      {
        ResponseHolder.THREAD_LOCAL.set(build());
      }

    /*******************************************************************************************************************
     *
     * This method actually builds the response and must be provided by concrete subclasses.
     * 
     * @return  the response
     *
     ******************************************************************************************************************/
    @Nonnull
    protected abstract RESPONSE_TYPE doBuild();

    /*******************************************************************************************************************
     *
     * Returns a header response previously added.
     * 
     * @param   header  the header name
     * @return          the header value
     *
     ******************************************************************************************************************/
    @Nullable
    protected abstract String getHeader (@Nonnull String header);

    /*******************************************************************************************************************
     *
     * Returns a header response previously added.
     * 
     * @param   header  the header name
     * @return          the header value
     *
     ******************************************************************************************************************/
    @Nullable
    protected final DateTime getDateTimeHeader (final @Nonnull String header)
      {
        final String value = getHeader(header);
        return (value == null) ? null : parseDate(value);
      }

    /*******************************************************************************************************************
     *
     * Takes care of the caching feature. If the response refers to an entity whose value has been cached by the
     * client and it's still fresh, a "Not modified" response will be returned.
     * 
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResponseBuilder<RESPONSE_TYPE> cacheSupport()
      {
        final String eTag = getHeader(HEADER_ETAG);
        final DateTime lastModified = getDateTimeHeader(HEADER_LAST_MODIFIED);

        log.debug(">>>> eTag: {} - requestIfNoneMatch: {}", eTag, requestIfNoneMatch);
        log.debug(">>>> lastModified: {} - requestIfNotModifiedSince: {}", lastModified, requestIfModifiedSince);

        if ( ((eTag != null) && eTag.equals(requestIfNoneMatch)) ||
             ((requestIfModifiedSince != null) && (lastModified != null) && 
              (lastModified.isBefore(requestIfModifiedSince) || lastModified.isEqual(requestIfModifiedSince))) )
          {
            return notModified();
          }

        return this;
      }

    /*******************************************************************************************************************
     *
     * Returns the current time. This can be overridden for mocking time in tests.
     * 
     * @return  the current time
     *
     ******************************************************************************************************************/
    @Nonnull
    protected DateTime getCurrentTime()
      {
        return new DateTime();
      }

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResponseBuilder<RESPONSE_TYPE> notModified() 
      {
        return withBody(new byte[0])
              .withContentLength(0)
              .withStatus(SC_NOT_MODIFIED);
      }

    /*******************************************************************************************************************
     *
     * Parse a date with one of the valid formats for HTTP headers.
     * 
     * FIXME: we should try to avoid depending on this stuff...
     *
     ******************************************************************************************************************/
    @Nonnull
    private DateTime parseDate (final @Nonnull String string)
      {
        for (final String dateFormat : DATE_FORMATS) 
          {
            try
              {
                log.debug("Parsing {} with {}...", string, dateFormat);
                return new DateTime(createFormatter(dateFormat).parse(string));
              }
            catch (ParseException e) 
              {
                log.debug("{}", e.getMessage());
              }
          }

        throw new IllegalArgumentException("Cannot parse date " + string);
      }

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    /* package */ static SimpleDateFormat createFormatter (final @Nonnull String template) 
      {
        final SimpleDateFormat formatter = new SimpleDateFormat(template, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
      }
  }

