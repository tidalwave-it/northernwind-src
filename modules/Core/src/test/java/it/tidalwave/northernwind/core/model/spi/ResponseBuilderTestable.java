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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class ResponseBuilderTestable extends ResponseBuilderSupport<byte[]>
  {
    private final Map<String, String> headerMap = new TreeMap<>();

    @Override @Nonnull
    public ResponseBuilderSupport<byte[]> withHeader (@Nonnull final String name, @Nonnull final String value)
      {
        headerMap.put(name, value);
        return this;
      }

    @Override @Nonnull
    protected byte[] doBuild()
      {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final DataOutputStream dos = new DataOutputStream(baos))
          {
            dos.writeBytes("HTTP/1.1 " + httpStatus + "\n");

            for (final Entry<String, String> entry : headerMap.entrySet())
              {
                dos.writeBytes(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
              }

            dos.writeBytes("\n");
            baos.write((byte[])body);
            return baos.toByteArray();
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    @Override @Nonnull
    protected Optional<String> getHeader (@Nonnull final String header)
      {
        return Optional.ofNullable(headerMap.get(header));
      }
  }
