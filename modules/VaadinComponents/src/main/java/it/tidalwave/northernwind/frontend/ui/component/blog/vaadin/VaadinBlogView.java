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
package it.tidalwave.northernwind.frontend.ui.component.blog.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import com.vaadin.ui.VerticalLayout;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri = "http://northernwind.tidalwave.it/component/Blog/#v1.0",
              controlledBy = DefaultBlogViewController.class)
@Slf4j
public class VaadinBlogView extends VerticalLayout implements BlogView
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
    public VaadinBlogView (final @Nonnull Id id)
      {
        this.id = id;
        setStyleName(NW + id.stringValue());
      }

//    @Override FIXME
//    public void addPost (final @Nonnull BlogPost blogPost)
//      {
//        final Label label = new Label();
//        label.setContentMode(Label.CONTENT_XHTML);
//        final StringBuilder builder = new StringBuilder();
//        builder.append("<h3>").append(blogPost.getTitle()).append("</h3>");
//        builder.append(blogPost.getFullText());
//        label.setValue(builder.toString());
//        addComponent(label);
//      }
  }
