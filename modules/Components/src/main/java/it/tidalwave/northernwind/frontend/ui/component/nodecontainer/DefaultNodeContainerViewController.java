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

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
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
      throws IOException, NotFoundException 
      {
        final ResourceProperties viewProperties = getViewProperties();
        final ResourceProperties siteNodeProperties = siteNode.getProperties();
        
        try
          {
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_RESOURCE);
            final Content template = site.find(Content.class).withRelativePath(templateRelativePath).result();
            view.setTemplate(template.getProperties().getProperty(PROPERTY_TEMPLATE));
          }
        catch (NotFoundException e)
          {
            // ok, use the default template  
          }
            
        view.addAttribute("titlePrefix", viewProperties.getProperty(PROPERTY_TITLE_PREFIX, ""));
        view.addAttribute("description", viewProperties.getProperty(PROPERTY_DESCRIPTION, ""));
        view.addAttribute("title", siteNodeProperties.getProperty(PROPERTY_TITLE, ""));
        view.addAttribute("screenCssSection", computeScreenCssSection());
        view.addAttribute("rssFeeds", computeRssFeedsSection());
        view.addAttribute("scripts", computeScriptsSection());
        view.addAttribute("inlinedScripts", computeInlinedScriptsSection());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceProperties getViewProperties()
      throws NotFoundException 
      {
        return siteNode.getPropertyGroup(view.getId());
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

            for (final String uri : getViewProperties().getProperty(PROPERTY_SCREEN_STYLE_SHEETS))
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

            for (final String relativePath : getViewProperties().getProperty(PROPERTY_RSS_FEEDS))
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
    
    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScriptsSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
        try
          {
            final String contextPath = site.getContextPath();

            for (final String relativeUri : getViewProperties().getProperty(PROPERTY_SCRIPTS))
              {
                builder.append(String.format("<script type=\"text/javascript\" src=\"%s%s\"></script>\n", contextPath, relativeUri));
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
    protected String computeInlinedScriptsSection() 
      {
        final StringBuilder builder = new StringBuilder();
    
        try
          {
            for (final String relativePath : getViewProperties().getProperty(PROPERTY_INLINED_SCRIPTS))
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

