/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 * 
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.menu.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.menu.DefaultMenuViewController;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import lombok.Getter;
import lombok.experimental.Delegate;

/***********************************************************************************************************************
 *
 * An HtmlTemplate implementation of {@link MenuView}, using an horizontal layout.
 * 
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/HorizontalMenu/#v1.0", 
              controlledBy=DefaultMenuViewController.class)
public class HtmlTemplateHorizontalMenuView extends HtmlHolder implements MenuView
  {
    @Delegate
    private final HtmlTemplateMenuViewHelper helper = new HtmlTemplateMenuViewHelper(this);
    
    @Getter @Nonnull
    private final Id id;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public HtmlTemplateHorizontalMenuView (@Nonnull final Id id)
      {
        super(id);
        this.id = id;
      }
  }
