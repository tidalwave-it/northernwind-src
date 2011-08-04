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
package it.tidalwave.northernwind.core.model.spi; 

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Order(LOWEST_PRECEDENCE)
public class DefaultContentRequestProcessor implements RequestProcessor 
  {
    @Inject @Nonnull
    private Site site;
    
    @Inject @Nonnull
    private SiteView siteView;
        
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (final @Nonnull Request request)
      throws NotFoundException, IOException 
      {
        final SiteNode siteNode = site.find(SiteNode).withRelativeUri(request.getRelativeUri()).result();            
//            siteView.setCaption(structure.getProperties().getProperty("Title")); TODO
        siteView.renderSiteNode(siteNode);
        
        return BREAK;
      }
  }
