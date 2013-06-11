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
package it.tidalwave.northernwind.core.impl.util;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import lombok.NoArgsConstructor;
import static lombok.AccessLevel.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE)
public final class UriUtilities
  {
    /*******************************************************************************************************************
     *
     * Decodes an URL-encoded URI
     *
     * @param   uri   the URL-encoded URI
     * @return        the plain text URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String urlDecodedPath (final @Nonnull String uri)
      throws UnsupportedEncodingException
      {
        final StringBuilder builder = new StringBuilder();

        for (final String part : uri.split("/"))
          {
            builder.append("/").append(URLDecoder.decode(part, "UTF-8"));
          }

        String s = builder.toString();

        if (!s.startsWith("/"))
          {
            s = "/" + s;
          }

        return "".equals(s) ? "/" : s.replace("//", "/").replace("///", "/");
      }

    /*******************************************************************************************************************
     *
     * Decodes an URL-encoded name
     *
     * @param   name  the URL-encoded name
     * @return        the plain text name
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String urlDecodedName (final @Nonnull String name)
      throws UnsupportedEncodingException
      {
        return URLDecoder.decode(name, "UTF-8");
      }

    /*******************************************************************************************************************
     *
     * Encodes an URL-encoded URI
     *
     * @param   uri   the plain text URI
     * @return        the URL-encoded URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String urlEncodedPath (final @Nonnull String uri)
      throws UnsupportedEncodingException
      {
        final StringBuilder builder = new StringBuilder();

        for (final String part : uri.split("/"))
          {
            builder.append("/").append(URLEncoder.encode(part, "UTF-8"));
          }

        return builder.toString();
      }
  }
