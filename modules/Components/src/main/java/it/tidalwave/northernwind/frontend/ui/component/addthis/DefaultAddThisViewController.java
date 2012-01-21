/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.addthis;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.DefaultStaticHtmlFragmentViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class DefaultAddThisViewController extends DefaultStaticHtmlFragmentViewController implements AddThisViewController
  { 
    @Nonnull
    private final AddThisView view;
    
    @Nonnull
    private final SiteNode siteNode;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link AddThisView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultAddThisViewController (final @Nonnull AddThisView view, final @Nonnull SiteNode siteNode) 
      {
        super(view, siteNode);
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
        final ResourceProperties properties = siteNode.getPropertyGroup(view.getId());
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("username", properties.getProperty(PROPERTY_USER_NAME, "")); 
        attributes.put("url", properties.getProperty(PROPERTY_URL, "")); 
        populate("AddThisHtmlFragment.txt", attributes); 
      }
  }
