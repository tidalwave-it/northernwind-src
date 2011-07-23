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
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuViewControllerSupport;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Link;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class VaadinMenuViewController extends MenuViewControllerSupport
  {
    @Nonnull @Inject
    private WebSite webSite;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public VaadinMenuViewController (final @Nonnull MenuView view) 
      {
        super(view);
      }
        
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addLink (final @Nonnull String navigationTitle, final @Nonnull String relativeUri)
      {
        ((AbstractOrderedLayout)view).addComponent(new Link(navigationTitle, new ExternalResource(webSite.getContextPath() + relativeUri)));                        
      }
  }
