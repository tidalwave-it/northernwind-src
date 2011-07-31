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
package it.tidalwave.northernwind.frontend.model.spi; 

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.Request;
import it.tidalwave.northernwind.frontend.model.RequestProcessor;
import it.tidalwave.northernwind.frontend.model.Site;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.Media.Media;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMediaRequestProcessor<ResponseType> implements RequestProcessor
  {
    @Inject @Nonnull
    private Site site;
    
    @Inject @Nonnull
    protected ResponseHolder<ResponseType> responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean process (final @Nonnull Request request) 
      throws NotFoundException, IOException
      {
        final String relativeUri = request.getRelativeUri();
        
        if (relativeUri.startsWith("/media"))
          {
            final Media media = site.find(Media).withRelativeUri(relativeUri.replaceAll("^/media", "")).result();
            final FileObject file = media.getFile();
            createResponse(file);
            return true;
          }
        
        return false;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected void createResponse (final @Nonnull FileObject file)
      throws IOException
      {
        log.info(">>>> serving contents of /{} ...", file.getPath());
        responseHolder.response().fromFile(file).put();
      }
  }
