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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import it.tidalwave.northernwind.core.model.Content;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultNodeContainerViewController implements NodeContainerViewController
  {
    @Inject @Nonnull
    private Site site;
    
    private final NodeContainerView view;
    
    private final SiteNode siteNode;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link NodeContainerView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultNodeContainerViewController (final @Nonnull NodeContainerView view, final @Nonnull SiteNode siteNode) 
      {
        this.view = view;
        this.siteNode = siteNode;
      }
    
    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize() 
      {
        try
          {
            // FIXME: use a loop, should catch exception for each property
            view.addAttribute("titlePrefix", siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE_PREFIX, ""));
            view.addAttribute("description", siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_DESCRIPTION, ""));
            view.addAttribute("title", siteNode.getProperties().getProperty(PROPERTY_TITLE, ""));
            view.addAttribute("screenCssSection", computeScreenCssSection());
            view.addAttribute("rssFeeds", computeRssFeedsSection());
            view.addAttribute("scripts", computeScriptsSection());
            view.addAttribute("inlinedScripts", computeInlinedScriptsSection());
          }
        catch (IOException e)
          {
            log.error("", e);
          }        
        catch (NotFoundException e)
          {
            // ok, no css  
          }        
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScreenCssSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
        try
          {
            final String contextPath = site.getContextPath();

            for (final String uri : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_SCREEN_STYLE_SHEETS))
              {
                builder.append("@import url(\"").append(contextPath).append(uri).append("\");\n");  
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }        
        catch (NotFoundException e)
          {
            // ok, no css  
          }      
        
        return builder.toString();
      }
    
    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeRssFeedsSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
        try
          {
            final String contextPath = site.getContextPath();

            for (final String relativePath : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_RSS_FEEDS))
              {
                final SiteNode rssSiteNode = site.find(SiteNode.class).withRelativePath(relativePath).result();
                final String mimeType = "application/rss+xml";
                final String relativeUri = rssSiteNode.getRelativeUri();
                builder.append(String.format("<link rel=\"alternate\" type=\"%s\" title=\"RSS\" href=\"%s%s\" />", mimeType, contextPath, relativeUri));
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }        
        catch (NotFoundException e)
          {
            // ok, no css  
          }      
        
        return builder.toString();
      }
            /*
                     <c:forEach items="{rssFeeds}" var="rssFeed" >
            <link rel="alternate" type="application/rss+xml" title="RSS" href="{rssFeed.url}" />
        </c:forEach>
        <c:forEach items="{scripts}" var="script" >
            <script type="text/javascript" src="{script.url}"></script>
        </c:forEach>
        <c:forEach items="{inlinedScripts}" var="inlinedScript">
            <c:set var="inlinedScriptId" value="{inlinedScript.id}"/>
            <% Integer contentId = (Integer)pageContext.findAttribute("inlinedScriptId"); %>
            <%= ((BasicTemplateController)request.getAttribute("templateLogic")).getParsedContentAttribute(contentId, "Template") %>
        </c:forEach>

             */
    
    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScriptsSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
//        try
//          {
//            final String contextPath = site.getContextPath();
//
//            for (final String uri : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_SCREEN_STYLE_SHEETS))
//              {
//                builder.append("@import url(\"").append(contextPath).append(uri).append("\");\n");  
//              }
//          }
//        catch (IOException e)
//          {
//            log.error("", e);
//          }        
//        catch (NotFoundException e)
//          {
//            // ok, no css  
//          }      
        
        return builder.toString();
      }
    
    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeInlinedScriptsSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
        try
          {
            for (final String relativePath : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_INLINED_SCRIPTS))
              {
                final Content script = site.find(Content.class).withRelativePath(relativePath).result();
                final String template = script.getProperties().getProperty(PROPERTY_TEMPLATE);
                builder.append(template);  
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }        
        catch (NotFoundException e)
          {
            // ok, no css  
          }      
        
        return builder.toString();
      }
  }
