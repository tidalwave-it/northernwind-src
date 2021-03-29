/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import static javax.servlet.http.HttpServletResponse.*;

/***********************************************************************************************************************
 *
 * An exceptional response representing a situation that should be reported to the client with a specific HTTP status
 * code. Note that this class doesn't necessarily represent an error, but it could be e.g. a redirect and such. This
 * class can also carry headers to be included as part of the response
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ToString
public class HttpStatusException extends Exception
  {
    /** Status codes that don't imply an error. */
    private static final List<Integer> GOOD_CODES = List.of(SC_FOUND, SC_MOVED_PERMANENTLY, SC_MOVED_TEMPORARILY);

    @Getter
    private final int httpStatus;

    @Getter
    private final Map<String, String> headers;

    /*******************************************************************************************************************
     *
     * Creates an exception representing a temporary redirect.
     *
     * @param  site         the {@link Site}
     * @param  target       the target to redirect to
     * @return              the exception
     *
     ******************************************************************************************************************/
    @Nonnull
    public static HttpStatusException temporaryRedirect (@Nonnull final Site site, @Nonnull final String target)
      {
        // FIXME: inject Site
        return new HttpStatusException(SC_MOVED_TEMPORARILY).withHeader("Location", createUrl(site, target));
      }

    /*******************************************************************************************************************
     *
     * Creates an exception representing a permanent redirect.
     *
     * @param  site         the {@link Site}
     * @param  target       the target to redirect to
     * @return              the exception
     *
     ******************************************************************************************************************/
    @Nonnull
    public static HttpStatusException permanentRedirect (@Nonnull final Site site, @Nonnull final String target)
      {
        // FIXME: inject Site
        return new HttpStatusException(SC_MOVED_PERMANENTLY).withHeader("Location", createUrl(site, target));
      }

    /*******************************************************************************************************************
     *
     * Creates an instance with the given HTTP status.
     *
     * @param  httpStatus  the status
     *
     ******************************************************************************************************************/
    public HttpStatusException (final int httpStatus)
      {
        this(httpStatus, Collections.emptyMap());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private HttpStatusException (final int httpStatus, @Nonnull final Map<String, String> headers)
      {
        super(String.format("httpStatus=%d, headers=%s", httpStatus, headers));
        this.httpStatus = httpStatus;
        this.headers = headers;
      }

    /*******************************************************************************************************************
     *
     * Creates a clone with the given header.
     *
     * @param  name   the header name
     * @param  value  the header value
     * @return        the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public HttpStatusException withHeader (@Nonnull final String name, @Nonnull final String value)
      {
        final Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(name, value);
        return new HttpStatusException(httpStatus, newHeaders);
      }

    /*******************************************************************************************************************
     *
     * Return {@code true} whether this exception represents an error.
     *
     * @return  {@code true} in case of error
     *
     ******************************************************************************************************************/
    public boolean isError()
      {
        return !GOOD_CODES.contains(httpStatus);
      }

    /*******************************************************************************************************************
     *
     * @param site
     * @param target
     * @return
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String createUrl (@Nonnull final Site site, @Nonnull final String target)
      {
        return target.startsWith("http://") || target.startsWith("https://")
                ? target
                : site.createLink(ResourcePath.of(target));
      }
  }
