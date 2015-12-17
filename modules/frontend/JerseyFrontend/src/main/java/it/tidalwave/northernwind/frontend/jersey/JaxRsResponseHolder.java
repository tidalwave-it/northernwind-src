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
package it.tidalwave.northernwind.frontend.jersey;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilder;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilderSupport;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder;

/***********************************************************************************************************************
 *
 * An implementation of {@link ResponseHolder} for JAX-RS.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class JaxRsResponseHolder extends ResponseHolder<Response> 
  {
    @NotThreadSafe
    public class JaxRsResponseBuilder extends ResponseBuilderSupport<Response>
      {
        private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        @Override @Nonnull
        public ResponseBuilder withHeader (final @Nonnull String header, final @Nonnull String value)
          {
            headers.add(header, value);
            return this;
          }

        @Override @Nonnull
        protected Response doBuild()
          {
            Response.ResponseBuilder builder = Response.status(httpStatus).entity(body);

            for (final Entry<String, List<String>> entry : headers.entrySet())
              {
                for (final String value : entry.getValue())
                  {
                    builder = builder.header(entry.getKey(), value);
                  }
              }

            return builder.build();
          }

        @Override
        protected String getHeader (final @Nonnull String header) 
          {
            final List<String> list = headers.get(header);
            return list.isEmpty() ? null : list.get(0);
          }
      }

    @Override @Nonnull
    public ResponseBuilder response()
      {
        return new JaxRsResponseBuilder();
      }
  }
