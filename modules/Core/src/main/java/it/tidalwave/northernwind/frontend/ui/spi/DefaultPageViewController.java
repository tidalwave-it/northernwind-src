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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.model.WebSiteNode;
import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.ui.PageViewController;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link PageViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultPageViewController implements PageViewController
  {
    @Inject @Nonnull
    private WebSite webSite;
    
    @Inject @Nonnull
    private PageView pageView;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource handleUri (final @Nonnull URL context, final @Nonnull String relativeUri) 
      throws NotFoundException, IOException, DoNothingException
      {
        log.info("handleUri({}. {})", context, relativeUri);

        // FIXME: pass thru JavaScript calls

        // FIXME: move to a filter
        if (relativeUri.startsWith("media"))
          {
            return webSite.findMediaByUri(relativeUri.replaceAll("^media", "")).getResource();      
          }

        // FIXME: move this to a filter too
        final WebSiteNode node = webSite.findNodeByUri("/" + relativeUri);            
//            pageView.setCaption(structure.getProperties().getProperty("Title")); TODO
        pageView.setContents(node.createContents());
        
        throw new DoNothingException(); 
      }
  } 
