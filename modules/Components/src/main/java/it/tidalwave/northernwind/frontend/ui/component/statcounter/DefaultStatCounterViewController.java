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
package it.tidalwave.northernwind.frontend.ui.component.statcounter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.DefaultStaticHtmlFragmentViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultStatCounterViewController extends DefaultStaticHtmlFragmentViewController implements StatCounterViewController
  {  
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link StatCounterView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  viewId            the id of the view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultStatCounterViewController (final @Nonnull StatCounterView view, 
                                             final @Nonnull Id viewId,
                                             final @Nonnull SiteNode siteNode) 
      throws IOException 
      {
        super(view, viewId, siteNode);
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("project", "5834368"); // FIXME: get from siteNode properties
        attributes.put("security", "91675212"); // FIXME: get from siteNode properties
        populate("StatCounterHtmlFragment.html", attributes);
      }
  }
