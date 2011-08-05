/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.io.IOException;
import org.joda.time.DateTime;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: $
 *
 **********************************************************************************************************************/
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    private final StringBuilder htmlBuilder = new StringBuilder();

    public HtmlTemplateBlogViewController (final @Nonnull BlogView view, final @Nonnull SiteNode siteNode) 
      {
        super(view, siteNode);
      }
    
    @Override
    protected void addPost (final @Nonnull Content post)
      throws IOException, NotFoundException 
      {
        final ResourceProperties properties = post.getProperties();

        htmlBuilder.append("<h3>").append(properties.getProperty(PROPERTY_TITLE)).append("</h3>\n");
        htmlBuilder.append(getBlogDateTime(post)).append("\n");
        htmlBuilder.append(properties.getProperty(PROPERTY_FULL_TEXT)).append("\n");
      }

    @Override
    protected void render()
      {
        ((HtmlTemplateBlogView)view).addComponent(new HtmlHolder(htmlBuilder.toString()));
      }
  }
