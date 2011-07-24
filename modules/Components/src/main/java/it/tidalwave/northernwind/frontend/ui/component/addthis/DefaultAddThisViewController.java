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
package it.tidalwave.northernwind.frontend.ui.component.addthis;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.DefaultStaticHtmlFragmentViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultAddThisViewController extends DefaultStaticHtmlFragmentViewController implements AddThisViewController
  {  
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link AddThisView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  viewInstanceName  the name of the view instance
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultAddThisViewController (final @Nonnull AddThisView view, 
                                         final @Nonnull String viewInstanceName,
                                         final @Nonnull SiteNode siteNode) 
      throws IOException 
      {
        super(view, viewInstanceName, siteNode);
        populate("AddThisHtmlFragment.html"); // FIXME: interpolate with properties
      }
  }
