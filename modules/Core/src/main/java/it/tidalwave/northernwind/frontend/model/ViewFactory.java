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
package it.tidalwave.northernwind.frontend.model;

import it.tidalwave.northernwind.frontend.ui.WebSiteNodeView;
import javax.annotation.Nonnull;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * A factory for Views.
 * 
 * @stereotype  Factory
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ViewFactory 
  {
    @Nonnull
    public WebSiteNodeView createWebSiteNodeView();
    
    /*******************************************************************************************************************
     *
     * Creates a new instance of a View.
     *
     * @param   viewName            the component view name
     * @param   instanceName        the instance name
     * @param   webSiteNode         the node this view is created for 
     * @return  
     * @throws  NotFoundException   if no view component is found
     * 
     ******************************************************************************************************************/
    @Nonnull
    public Object createView (@Nonnull String viewName, 
                              @Nonnull String instanceName, 
                              @Nonnull WebSiteNode webSiteNode) 
      throws NotFoundException;
  }