/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.model.htmltemplate.urihandler;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.northernwind.frontend.model.spi.MediaUriHandlerSupport;
import it.tidalwave.northernwind.frontend.htmltemplate.ResponseThreadLocal;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") 
public class HtmlTemplateMediaUriHandler extends MediaUriHandlerSupport<Response, ResponseThreadLocal>
  {
    @Override @Nonnull
    protected Response createResponse (final @Nonnull FileObject file) 
      throws FileNotFoundException
      {
        return Response.ok(file.getInputStream(), file.getMIMEType()).build();
      }
  }
