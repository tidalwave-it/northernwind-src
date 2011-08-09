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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import it.tidalwave.northernwind.core.model.Site;
import javax.inject.Inject;
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
import org.springframework.beans.factory.annotation.Configurable;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    private final Map<DateTime, String> blogSortedMapByDate = new TreeMap<DateTime, String>(REVERSE_DATE_COMPARATOR);
    
    @Inject @Nonnull
    private Site site;
    
    @Nonnull
    private final SiteNode siteNode;

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public HtmlTemplateBlogViewController (final @Nonnull BlogView view, final @Nonnull SiteNode siteNode) 
      {
        super(view, siteNode);
        this.siteNode = siteNode;
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
        final String idPrefix = "nw-blogpost-" + blogDateTime.toDate().getTime();
        htmlBuilder.append(String.format("<div id='%s' class='nw-blog-post'>\n", idPrefix));
        htmlBuilder.append(String.format("<h3>%s</h3>\n", properties.getProperty(PROPERTY_TITLE)));
        htmlBuilder.append("<div class='nw-blog-post-meta'>");
        htmlBuilder.append(String.format("<span class='nw-publishDate'>%s</span>\n", requestLocaleManager.getDateTimeFormatter().print(blogDateTime)));
        
        try
          {
           final String link = site.getContextPath() + siteNode.getRelativeUri() + "/" + getExposedUri(post) + "/";
           htmlBuilder.append(String.format("&nbsp;<a href='%s'>Permalink</a>\n", link));
          }
        catch (NotFoundException e)
          {
            // ok, no link  
          }
            
        htmlBuilder.append("</div>");        
        htmlBuilder.append(String.format("<div  class='nw-blog-post-content'>%s</div>\n", properties.getProperty(PROPERTY_FULL_TEXT)));
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
