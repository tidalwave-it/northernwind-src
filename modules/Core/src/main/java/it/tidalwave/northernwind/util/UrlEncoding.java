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
package it.tidalwave.northernwind.util;

import javax.annotation.Nonnull;
import java.net.URLDecoder;
import java.net.URLEncoder;
import lombok.NoArgsConstructor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * A wrapper around {@link URLDecoder} and {@link URLEncoder} so functional descriptors can be used.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE)
public final class UrlEncoding
  {
    /*******************************************************************************************************************
     *
     * Decodes a string in UTF-8.
     *
     * @param   string      the string
     * @return              the decoded string
     *
     ******************************************************************************************************************/
    @Nonnull @SuppressWarnings("squid:S00112")
    public static String decodedUtf8 (@Nonnull final String string)
      {
        return URLDecoder.decode(string, UTF_8);
      }

    /*******************************************************************************************************************
     *
     * Encodes a string in UTF-8.
     *
     * @param   string      the string
     * @return              the encoded string
     *
     *
     ******************************************************************************************************************/
    @Nonnull @SuppressWarnings("squid:S00112")
    public static String encodedUtf8 (@Nonnull final String string)
      {
        return URLEncoder.encode(string, UTF_8);
      }
  }
