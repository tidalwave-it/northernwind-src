/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.HtmlTextWithTitleView;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.DefaultHtmlTextWithTitleViewController;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * The HtmlTemplateimplementation of {@link HtmlTextWithTitleView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/HtmlTextWithTitle/#v1.0", 
              controlledBy=DefaultHtmlTextWithTitleViewController.class)
public class HtmlTemplateHtmlTextWithTitleView extends HtmlHolder implements HtmlTextWithTitleView
  {
    @Getter @Nonnull
    private final Id id;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public HtmlTemplateHtmlTextWithTitleView (final @Nonnull Id id) 
      {
        super(id);
        this.id = id;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setText (final @Nonnull String text)
      {
        setContent(text);
      }
  }
