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

import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import java.util.List;

import javax.annotation.Nonnull;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import it.tidalwave.northernwind.frontend.model.Node;
import java.io.IOException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class VaadinHorizontalMenuView extends HorizontalLayout implements MenuView
  {
    public VaadinHorizontalMenuView (final @Nonnull String name) 
      {
        setMargin(false);
        setStyleName("component-" + name);
      }
    
    @Override
    public void setLinks (final @Nonnull List<Node> nodes) 
      throws IOException
      {
        for (final Node node : nodes)
          {  
            final String uri = node.getUri();
            final String navigationTitle = node.getProperties().getProperty("NavigationTitle");
            addComponent(new Link(navigationTitle, new ExternalResource("/nw" + uri)));                
          }
      }
  }
