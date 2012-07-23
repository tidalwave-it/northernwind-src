/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.menu.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import it.tidalwave.northernwind.frontend.ui.component.menu.DefaultMenuViewController;
import com.vaadin.ui.VerticalLayout;
import lombok.Delegate;
import lombok.Getter;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * A Vaadin implementation of {@link MenuView}, using an horizontal layout.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/VerticalMenu/#v1.0", 
              controlledBy=DefaultMenuViewController.class)
public class VaadinVerticalMenuView extends VerticalLayout implements MenuView
  {
    @Delegate
    private final VaadinMenuViewHelper helper = new VaadinMenuViewHelper(this);
    
    @Getter @Nonnull
    private final Id id;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public VaadinVerticalMenuView (final @Nonnull Id id) 
      {
        this.id = id;
        setMargin(false);
        setStyleName(NW + id.stringValue());
      }
  }
