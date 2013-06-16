/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.htmlfragment.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.HtmlFragmentView;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.DefaultHtmlFragmentViewController;
import com.vaadin.ui.Label;
import lombok.Getter;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link ArticleView}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri = "http://northernwind.tidalwave.it/component/HtmlFragment/#v1.0",
              controlledBy = DefaultHtmlFragmentViewController.class)
public class VaadinHtmlFragmentView extends Label implements HtmlFragmentView
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
    public VaadinHtmlFragmentView (final @Nonnull Id id)
      {
        this.id = id;
        setStyleName(NW + id.stringValue());
        setContentMode(Label.CONTENT_XHTML);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setContent (final @Nonnull String html)
      {
        setValue(html);
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
