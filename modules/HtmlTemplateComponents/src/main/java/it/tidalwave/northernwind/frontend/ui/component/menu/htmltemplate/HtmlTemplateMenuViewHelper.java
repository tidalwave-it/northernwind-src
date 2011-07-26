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
package it.tidalwave.northernwind.frontend.ui.component.menu.htmltemplate;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.menu.MenuView;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
 /* package */ class HtmlTemplateMenuViewHelper 
  {
    @Nonnull
    private final MenuView view;
    
    @Nonnull @Inject
    private Site site;
    
    public HtmlTemplateMenuViewHelper (final @Nonnull MenuView view)
      {
        this.view = view;
      }
        
    public void addLink (final @Nonnull String navigationTitle, final @Nonnull String relativeUri)
      {
        ((HtmlHolder)view).addComponent(new HtmlHolder(
                String.format("<li><a href='%s'>%s</a></li>", site.getContextPath() + relativeUri, navigationTitle)));                        
      }
  }
