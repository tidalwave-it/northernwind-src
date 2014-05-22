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
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MockResponseHolder extends ResponseHolder<String>
  {
    class MockResponseBuilder extends ResponseBuilderSupport<String>
      {
        private final Map<String, String> headerMap = new TreeMap<String, String>();

        @Override
        public ResponseBuilderSupport<String> withHeader (String name, String value)
          {
            headerMap.put(name, value);
            return this;
          }

        @Override
        protected String doBuild()
          {
            final StringBuilder builder = new StringBuilder();

            builder.append("HTTP/1.1 ").append(httpStatus).append("\n");

            for (final Entry<String, String> entry : headerMap.entrySet())
              {
                builder.append(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
              }

            builder.append("\n").append(body);
            return builder.toString();
          }

        @Override
        protected String getHeader (final @Nonnull String header) 
          {
            return headerMap.get(header);
          }
      }

    @Override
    public ResponseBuilderSupport response()
      {
        return new MockResponseBuilder();
      }

    @Override @Nonnull
    protected DateTime getTime()
      {
        return new DateTime(1341242353456L);
      }
  }
