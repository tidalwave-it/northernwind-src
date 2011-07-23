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
package it.tidalwave.northernwind.frontend.model.vaadin.urihandler;

import it.tidalwave.northernwind.frontend.model.Media;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.UriHandler;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.vaadin.DownloadStreamThreadLocal;
import com.vaadin.terminal.DownloadStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import static it.tidalwave.northernwind.frontend.model.Media.Media;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class VaadinMediaUriHandler implements UriHandler
  {
    @Inject @Nonnull
    private WebSite webSite;
    
    @Inject @Nonnull
    private DownloadStreamThreadLocal downloadStreamHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean handleUri (final @Nonnull URL context, final @Nonnull String relativeUri) 
      throws NotFoundException, IOException
      {
        if (relativeUri.startsWith("media"))
          {
            final Media media = webSite.find(Media).withRelativeUri(relativeUri.replaceAll("^media", "")).result();
            final Resource resource = media.getResource();     
            final FileObject file = resource.getFile();
            log.info(">>>> serving contents of {} ...", file.getPath());
            downloadStreamHolder.set(new DownloadStream(file.getInputStream(), file.getNameExt(), null));
            // FIXE: mimeType triggers a NB Platform exception
//            downloadStreamHolder.set(new DownloadStream(file.getInputStream(), file.getNameExt(), file.getMIMEType()));
            // TODO: I suppose DownloadStream closes the stream
            return true;
          }
        
        return false;
      }
  }
