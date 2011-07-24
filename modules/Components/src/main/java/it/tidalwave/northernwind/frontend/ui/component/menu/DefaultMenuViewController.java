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
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.SiteNode.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MenuViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Scope("session") @Slf4j
public class DefaultMenuViewController implements MenuViewController
  {    
    @Nonnull @Inject
    private Site site;
    
    @Nonnull
    protected final MenuView view;

    /*******************************************************************************************************************
     *
     * @param  view              the related view
     * @param  viewInstanceName  the name of the view instance
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultMenuViewController (final @Nonnull MenuView view, 
                                      final @Nonnull String viewInstanceName, 
                                      final @Nonnull SiteNode siteNode) 
    {
        this.view = view;
        
        try 
          {
            final Key<String> PROP_CONTENT = new Key<String>(viewInstanceName + ".content"); // FIXME: have a subproperty group with the name
            final String uris = siteNode.getProperty(PROP_CONTENT);
            setLinks(Arrays.asList(uris.split(",")));
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
    
    /*******************************************************************************************************************
     *
     * Sets the navigation links.
     * 
     * @param  relativeUris   the relative URIs of the targets
     *
     ******************************************************************************************************************/
    private void setLinks (final @Nonnull List<String> relativeUris) 
      {
        log.debug("setLinks({})", relativeUris);
        
        for (final String relativeUri : relativeUris)
          {  
            try
              {
                // FIXME: should be fixed in the Infoglue importer
                final String fixedUri = "/" + relativeUri.trim().replaceAll("Mobile", "");
                final SiteNode targetSiteNode = site.find(SiteNode).withRelativeUri(fixedUri).result();
                final String navigationTitle = targetSiteNode.getProperty(PROP_NAVIGATION_TITLE, "no nav. title");
                view.addLink(navigationTitle, fixedUri);                
              }
            catch (IOException e)
              {
                log.warn("", e);
              }
            catch (NotFoundException e)
              {
                log.warn("", e);
              }
          }
      }
  }