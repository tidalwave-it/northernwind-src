/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

/***********************************************************************************************************************
 *
 * A {@link MIMEResolver} which resolves MIME types against the {@link ServletContext}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @ServiceProvider(service=MIMEResolver.class) @Slf4j
public class ServletContextMimeResolver extends MIMEResolver
  {
    @Inject 
    private ApplicationContext applicationContext;
    
    @Override @Nullable
    public String findMIMEType (final @Nonnull FileObject fileObject) 
      {
        final String fileName = fileObject.getNameExt();
        final String mimeType = applicationContext.getBean(ServletContext.class).getMimeType(fileName);
        log.trace(">>>> MIME type for {} is {}", fileObject, mimeType);
        return mimeType;
      }
  }
