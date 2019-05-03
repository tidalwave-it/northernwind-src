/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilder;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilderSupport;
import static java.util.Collections.emptyList;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe
public class SpringMvcResponseBuilder extends ResponseBuilderSupport<ResponseEntity<?>>
  {
    private final HttpHeaders headers = new HttpHeaders();

    @Override @Nonnull
    public ResponseBuilder withHeader (final @Nonnull String header, final @Nonnull String value)
      {
        headers.add(header, value);
        return this;
      }

    @Override @Nonnull
    protected Optional<String> getHeader (final @Nonnull String header)
      {
        return headers.getOrDefault(header, emptyList()).stream().findFirst();
      }

    @Override @Nonnull
    public ResponseBuilder withContentType (final @Nonnull String contentType)
      {
        headers.setContentType(MediaType.parseMediaType(contentType));
        return this;
      }

    @Override @Nonnull
    public ResponseBuilder withContentLength (final @Nonnegative long contentLenght)
      {
        headers.setContentLength(contentLenght);
        return this;
      }

    @Override @Nonnull
    protected ResponseEntity<?> doBuild()
      {
        return new ResponseEntity<>(body, headers, HttpStatus.valueOf(httpStatus));
      }
  }

