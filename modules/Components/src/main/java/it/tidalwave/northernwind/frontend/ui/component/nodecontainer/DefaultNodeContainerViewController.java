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
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class DefaultNodeContainerViewController implements NodeContainerViewController
  {
    @Inject @Nonnull
    private Site site;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link NodeContainerView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  viewId            the id of the view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultNodeContainerViewController (final @Nonnull NodeContainerView view, 
                                               final @Nonnull Id viewId,
                                               final @Nonnull SiteNode siteNode) 
      {     
        
        final StringBuilder builder = new StringBuilder();
        
        try
          {
            final String styleSheetUris = siteNode.getProperties(viewId).getProperty(PROPERTY_SCREEN_STYLE_SHEETS);
            final List<String> uris = new ArrayList<String>();
            
            for (final String styleSheetUri : styleSheetUris.split(","))
              {
                uris.add(styleSheetUri.trim());  
              }
            
            final String contextPath = site.getContextPath();

            for (final String uri : uris)
              {
                builder.append("@import url(\"").append(contextPath).append(uri).append("\");\n");  
              }

            view.setScreenCssSection(builder.toString());
          }
        catch (IOException e)
          {
            log.warn("", e);
            // ok, no css  
          }        
        catch (NotFoundException e)
          {
            // ok, no css  
          }        
      }
  }
