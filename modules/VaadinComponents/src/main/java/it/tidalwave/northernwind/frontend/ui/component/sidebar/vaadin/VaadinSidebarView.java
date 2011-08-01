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
package it.tidalwave.northernwind.frontend.ui.component.sidebar.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.sidebar.DefaultSidebarViewController;
import it.tidalwave.northernwind.frontend.ui.component.sidebar.SidebarView;
import com.vaadin.ui.VerticalLayout;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Sidebar/#v1.0", 
              controlledBy=DefaultSidebarViewController.class)
public class VaadinSidebarView extends VerticalLayout implements SidebarView
  {
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public VaadinSidebarView (final @Nonnull Id id) 
      {
        setMargin(false);
        setStyleName(NW + id.stringValue());
      }
  }  
