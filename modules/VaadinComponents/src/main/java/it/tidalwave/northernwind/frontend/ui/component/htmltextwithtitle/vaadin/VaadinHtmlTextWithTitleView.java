/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.HtmlTextWithTitleView;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.DefaultHtmlTextWithTitleViewController;
import com.vaadin.ui.Label;
import lombok.Getter;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link HtmlTextWithTitleView}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri = "http://northernwind.tidalwave.it/component/HtmlTextWithTitle/#v1.0",
              controlledBy = DefaultHtmlTextWithTitleViewController.class)
public class VaadinHtmlTextWithTitleView extends Label implements HtmlTextWithTitleView
  {
    @Nonnull @Getter
    private final Id id;

    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     *
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public VaadinHtmlTextWithTitleView (final @Nonnull Id id)
      {
        this.id = id;
        setStyleName(NW + id.stringValue());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setText (final @Nonnull String text)
      {
        setContentMode(Label.CONTENT_XHTML);
        setValue(text);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setClassName (final @Nonnull String className)
      {
        setStyleName(className);
      }
  }
