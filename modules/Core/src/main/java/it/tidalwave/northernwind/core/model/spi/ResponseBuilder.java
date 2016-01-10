/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Map;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourceFile;

/***********************************************************************************************************************
 *
 * A builder of a response.
 *
 * @param <RESPONSE_TYPE>  the produced response (may change in function of the technology used for serving the
 *                         results)
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ResponseBuilder<RESPONSE_TYPE>
  {
    /*******************************************************************************************************************
     *
     * Sets a header.
     *
     * @param   header              the header name
     * @param   value               the header value
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withHeader (@Nonnull String header, @Nonnull String value);

    /*******************************************************************************************************************
     *
     * Sets multiple headers at the same time.
     *
     * @param   headers             the headers
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withHeaders (@Nonnull Map<String, String> headers);

    /*******************************************************************************************************************
     *
     * Specifies the content type.
     *
     * @param   contentType         the content type
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentType (@Nonnull String contentType);

    /*******************************************************************************************************************
     *
     * Specifies the content length.
     *
     * @param  contentLength        the content length
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentLength (@Nonnull long contentLength);

    /*******************************************************************************************************************
     *
     * Specifies the content disposition.
     *
     * @param  contentDisposition   the content disposition
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withContentDisposition (@Nonnull String contentDisposition);

    /*******************************************************************************************************************
     *
     * Specifies the expiration time.
     *
     * @param  duration             the duration
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withExpirationTime (@Nonnull Duration duration);

    /*******************************************************************************************************************
     *
     * Specifies the latest modified time.
     *
     * @param  time                 the time
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withLatestModifiedTime (@Nonnull ZonedDateTime time);

    /*******************************************************************************************************************
     *
     * Specifies the body of the response. Accepted objects are: {@code byte[]}, {@code String},
     * {@code InputStream}.
     *
     * @param  body                 the body
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withBody (@Nonnull Object body);

    /*******************************************************************************************************************
     *
     * Specifies the body of the response as a {@link ResourceFile}.
     *
     * @param  file                 the file
     * @return                      itself for fluent interface style
     * @throws IOException          if an error occurs when reading the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> fromFile (@Nonnull ResourceFile file)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Specifies the {@link Request} we're serving - this makes it possible to read some headers and other
     * configurations needed e.g. for cache control.
     *
     * @param  request              the request
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forRequest (@Nonnull Request request);

    /*******************************************************************************************************************
     *
     * Specifies an exception to create the response from.
     *
     * @param  e                    the exception
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (@Nonnull NotFoundException e);

    /*******************************************************************************************************************
     *
     * Specifies an exception to create the response from.
     *
     * @param  e                    the exception
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (@Nonnull HttpStatusException e);

    /*******************************************************************************************************************
     *
     * Specifies an exception to create the response from.
     *
     * @param  e                    the exception
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> forException (@Nonnull Throwable e);

    /*******************************************************************************************************************
     *
     * Specifies the HTTP status.
     *
     * @param  httpStatus           the status
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> withStatus (@Nonnull int httpStatus);

    /*******************************************************************************************************************
     *
     * Creates a builder for a permanent redirect.
     *
     * @param  url                  the URL of the redirect
     * @return                      itself for fluent interface style
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResponseBuilder<RESPONSE_TYPE> permanentRedirect (@Nonnull String url);

    /*******************************************************************************************************************
     *
     * Builds the response.
     *
     * @return                              the response
     *
     ******************************************************************************************************************/
    @Nonnull
    public RESPONSE_TYPE build();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void put();
  }

