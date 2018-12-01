/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor
 /* package */ class HtmlTemplateMenuViewHelper 
  {
    @Nonnull
    private final MenuView view;
    
    @Nonnull
    private String title = "";
    
    public void setTitle (final @Nonnull String title)
      {
        ((HtmlHolder)view).addAttribute("title", String.format("<h2>%s</h2>", title));
      }
        
    public void addLink (final @Nonnull String navigationTitle, final @Nonnull String link)
      {
        ((HtmlHolder)view).addComponent(new HtmlHolder(String.format("<li><a href='%s'>%s</a></li>", 
                                                       link, navigationTitle)));                        
      }
  }
