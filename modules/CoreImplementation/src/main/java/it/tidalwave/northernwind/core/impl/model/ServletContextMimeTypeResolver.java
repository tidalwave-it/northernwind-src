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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import it.tidalwave.northernwind.core.model.MimeTypeResolver;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * An implementation of {@link MimeTypeResolver} that delegates to a {@link ServletContext}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ServletContextMimeTypeResolver implements MimeTypeResolver
  {
    @Inject @Nonnull
    private Provider<ServletContext> servletContext;

    @Override @Nonnull
    public String getMimeType (final @Nonnull String fileName)
      {
        String mimeType = servletContext.get().getMimeType(fileName);
        mimeType = (mimeType != null) ? mimeType : "content/unknown";
        log.trace(">>>> MIME type for {} is {}", fileName, mimeType);
        return mimeType;
      }
  }
