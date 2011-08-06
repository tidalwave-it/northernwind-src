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
package it.tidalwave.northernwind.frontend.ui.component.menu;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.SiteNode.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MenuViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope("session") @Slf4j
public class DefaultMenuViewController implements MenuViewController
  {    
    @Inject @Nonnull
    private Site site;
    
    @Nonnull
    protected final MenuView view;
    
    @Nonnull
    protected final SiteNode siteNode;

    /*******************************************************************************************************************
     *
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultMenuViewController (final @Nonnull MenuView view, final @Nonnull SiteNode siteNode) 
      {
        this.view = view;
        this.siteNode = siteNode;
      }
    
    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
   @PostConstruct
   /* package */ void initialize()
     {
        try 
          {
            for (final String relativePath : siteNode.getProperties(view.getId()).getProperty(PROPERTY_LINKS))
              {  
                try
                  {
                    final SiteNode targetSiteNode = site.find(SiteNode).withRelativePath(relativePath).result();
                    final String navigationTitle = targetSiteNode.getProperties().getProperty(PROPERTY_NAVIGATION_LABEL, "no nav. label");
                    view.addLink(navigationTitle, targetSiteNode.getRelativeUri());                
                  }
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
                catch (NotFoundException e)
                  {
                    log.warn("Ignoring link '{}' because of {}", relativePath, e.toString());
                  }
              }
          }
        catch (NotFoundException e)
          {
            log.error("", e);
          }
        catch (IOException e) 
          {
            log.error("", e);
          }
      }
  }