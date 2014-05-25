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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MockResponseHolder extends ResponseHolder<byte[]>
  {
    @Getter @Setter
    private static DateTime currentTime = new DateTime();
    
    class MockResponseBuilder extends ResponseBuilderSupport<byte[]>
      {
        private final Map<String, String> headerMap = new TreeMap<>();

        @Override
        public ResponseBuilderSupport<byte[]> withHeader (String name, String value)
          {
            headerMap.put(name, value);
            return this;
          }

        @Override
        protected byte[] doBuild()
          {
            try
              {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final DataOutputStream dos = new DataOutputStream(baos);

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

        @Override
        protected String getHeader (final @Nonnull String header) 
          {
            return headerMap.get(header);
          }

        @Override @Nonnull
        protected DateTime getCurrentTime()
          {
            return currentTime;
          }
      }

    @Override
    public ResponseBuilderSupport response()
      {
        return new MockResponseBuilder();
      }
  }
