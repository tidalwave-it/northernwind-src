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

import java.util.Comparator;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import java.util.Map;
import java.io.IOException;
import org.joda.time.DateTime;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: $
 *
 **********************************************************************************************************************/
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    private static final Comparator<DateTime> REVERSE_DATE_COMPARATOR = new Comparator<DateTime>() 
      {
        @Override
        public int compare (final @Nonnull DateTime dateTime1, final @Nonnull DateTime dateTime2) 
          {
            return -dateTime1.compareTo(dateTime2);
          }
      };
    
    private final Map<DateTime, String> blogSortedMapByDate = new TreeMap<DateTime, String>(REVERSE_DATE_COMPARATOR);

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public HtmlTemplateBlogViewController (final @Nonnull BlogView view, final @Nonnull SiteNode siteNode) 
      {
        super(view, siteNode);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addPost (final @Nonnull Content post)
      throws IOException, NotFoundException 
      {
        final ResourceProperties properties = post.getProperties();

        final DateTime blogDateTime = getBlogDateTime(post);
        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(String.format("<div id='blogpost-%s'>\n", blogDateTime.toDate().getTime()));
        htmlBuilder.append(String.format("<h3>%s</h3>\n", properties.getProperty(PROPERTY_TITLE)));
        htmlBuilder.append(String.format("<span class='.nw-publishDate'>%s</span>\n", requestLocaleManager.getDateTimeFormatter().print(blogDateTime)));
        htmlBuilder.append(String.format("%s\n", properties.getProperty(PROPERTY_FULL_TEXT)));
        htmlBuilder.append(String.format("</div>\n"));
        blogSortedMapByDate.put(blogDateTime, htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void render()
      {
        final StringBuilder htmlBuilder = new StringBuilder();
       
        for (final String html : blogSortedMapByDate.values())
          {
            htmlBuilder.append(html);
          }
        
        ((HtmlTemplateBlogView)view).addComponent(new HtmlHolder(htmlBuilder.toString()));
      }
  }
