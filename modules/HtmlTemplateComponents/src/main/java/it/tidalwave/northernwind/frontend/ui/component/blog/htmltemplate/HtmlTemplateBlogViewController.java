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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    private final List<String> htmlParts = new ArrayList<String>();
    
    @Inject @Nonnull
    private Site site;
    
    @Nonnull
    private final SiteNode siteNode;
    
    private boolean referencesRendered;

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
    protected void addFullPost (final @Nonnull Content post)
      throws IOException, NotFoundException 
      {
        log.debug("addFullPost()");        
        addPost(post, true);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addLeadInPost (final @Nonnull Content post)
      throws IOException, NotFoundException 
      {
        log.debug("addLeadInPost()");        
        addPost(post, false);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addReference (final @Nonnull Content post)
      throws IOException, NotFoundException 
      {
        log.debug("addReference()");
        
        final ResourceProperties properties = post.getProperties();

        final DateTime blogDateTime = getBlogDateTime(post);
        final StringBuilder htmlBuilder = new StringBuilder();
        
        if (!referencesRendered)
          {
            htmlBuilder.append("<ul>\n");
            referencesRendered = true;
          }
        
//        final String idPrefix = "nw-blogpost-" + blogDateTime.toDate().getTime();
        
        try
          {
           final String link = site.getContextPath() + siteNode.getRelativeUri() + "/" + getExposedUri(post) + "/";
           htmlBuilder.append(String.format("<li><a href='%s'>%s</a></li>\n", link, properties.getProperty(PROPERTY_TITLE)));
          }
        catch (NotFoundException e)
          {
            // ok, no link  
          }
            
        htmlParts.add(htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    private void addPost (final @Nonnull Content post, final boolean addBody)
      throws IOException, NotFoundException 
      {
        final ResourceProperties properties = post.getProperties();
        final StringBuilder htmlBuilder = new StringBuilder();
        
        final DateTime blogDateTime = getBlogDateTime(post);
        final String idPrefix = "nw-" + view.getId() + "-blogpost-" + blogDateTime.toDate().getTime();
        htmlBuilder.append(String.format("<div id='%s' class='nw-blog-post'>\n", idPrefix));
        htmlBuilder.append(String.format("<h3>%s</h3>\n", properties.getProperty(PROPERTY_TITLE)));
        
        htmlBuilder.append("<div class='nw-blog-post-meta'>");
        htmlBuilder.append(String.format("<span class='nw-publishDate'>%s</span>\n", requestLocaleManager.getDateTimeFormatter().print(blogDateTime)));
        
        try
          {  
            htmlBuilder.append(String.format("&nbsp;- <span class='nw-blog-post-category'>Filed under \"%s\"</span>", properties.getProperty(PROPERTY_CATEGORY)));
          }
        catch (NotFoundException e)
          {
            // ok, no category
          }
        
        try
          {
           final String link = site.getContextPath() + siteNode.getRelativeUri() + "/" + getExposedUri(post) + "/";
           htmlBuilder.append(String.format("&nbsp;- <a href='%s'>Permalink</a>\n", link));
          }
        catch (NotFoundException e)
          {
            // ok, no link  
          }
            
        htmlBuilder.append("</div>");        
        
        if (addBody)
          {
            htmlBuilder.append(String.format("<div class='nw-blog-post-content'>%s</div>\n", properties.getProperty(PROPERTY_FULL_TEXT)));
          }

        htmlBuilder.append(String.format("</div>\n"));
        htmlParts.add(htmlBuilder.toString());
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
        
        try 
          {
            final String title = siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE);
            htmlBuilder.append(String.format("<h2>%s</h2>\n", title));
          } 
        catch (NotFoundException e) 
          {
          }
        catch (IOException e) 
          {
          }
       
        for (final String html : htmlParts)
          {
            htmlBuilder.append(html);
          }
        
        if (referencesRendered)
          {
            htmlBuilder.append("</ul>\n");
          }
        
        ((HtmlTemplateBlogView)view).addComponent(new HtmlHolder(htmlBuilder.toString()));
      }
  }
