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
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.menu.DefaultMenuViewController;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import com.vaadin.ui.HorizontalLayout;
import lombok.Delegate;

/***********************************************************************************************************************
 *
 * A Vaadin implementation of {@link MenuView}, using an horizontal layout.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/HorizontalMenu", 
              controlledBy=DefaultMenuViewController.class)
public class VaadinHorizontalMenuView extends HorizontalLayout implements MenuView
  {
    @Delegate
    private final VaadinMenuViewHelper helper = new VaadinMenuViewHelper(this);
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public VaadinHorizontalMenuView (final @Nonnull String id) 
      {
        setMargin(false);
        setStyleName("component-" + id);
      }
  }
