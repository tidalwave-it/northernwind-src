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
     * Initializes this controller.
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
  }
