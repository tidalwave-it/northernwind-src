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
package it.tidalwave.northernwind.frontend.ui.component.menu.vaadin;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.model.WebSiteNode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.WebSiteNode.*;

/***********************************************************************************************************************
 *
 * A Vaadin implementation of {@link MenuView}, using an horizontal layout.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class VaadinHorizontalMenuView extends HorizontalLayout implements MenuView
  {
    @Nonnull @Inject
    private WebSite webSite;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given name.
     * 
     * @param  name  the component name
     *
     ******************************************************************************************************************/
    public VaadinHorizontalMenuView (final @Nonnull String name) 
      {
        setMargin(false);
        setStyleName("component-" + name);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setLinks (final @Nonnull List<String> relativeUris) 
      {
        for (final String relativeUri : relativeUris)
          {  
            try
              {
                final WebSiteNode node = webSite.find(WebSiteNode).withRelativeUri(relativeUri).result();
                final String navigationTitle = node.getProperty(PROP_NAVIGATION_TITLE, "no nav. title");
                addComponent(new Link(navigationTitle, new ExternalResource(webSite.getContextPath() + relativeUri)));                
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
