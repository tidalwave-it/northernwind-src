/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * An exceptional response representing a situation that should be reported to the client with a specific HTTP status
 * code. Note that this class doesn't necessarily represent an error, but it could be e.g. a redirect and such. This
 * class can also carry headers to be included as part of the response
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString
public class HttpStatusException extends Exception
  {
    @Getter
    private final int httpStatus;

    @Getter
    private final Map<String, String> headers;

    /*******************************************************************************************************************
     *
     * Creates an instance with the given HTTP status.
     *
     * @param  httpStatus  the status
     *
     ******************************************************************************************************************/
    public HttpStatusException (final int httpStatus)
      {
        this(httpStatus, Collections.<String, String>emptyMap());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private HttpStatusException (final int httpStatus, final @Nonnull Map<String, String> headers)
      {
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
    public HttpStatusException withHeader (final @Nonnull String name, final @Nonnull String value)
      {
        final Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(name, value);
        return new HttpStatusException(httpStatus, newHeaders);
      }
  }
