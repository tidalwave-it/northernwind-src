/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.SiteNode;

/***********************************************************************************************************************
 *
 * This class models a site of the site. It is repopulated or reloaded (in function of the technology) every time in
 * function of the user navigation.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface SiteView
  {
    public static final String NW = "nw-";

    /*******************************************************************************************************************
     *
     * Sets the caption.
     *
     * @param  caption   the caption
     *
     ******************************************************************************************************************/
    public void setCaption (@Nonnull String caption);

    /*******************************************************************************************************************
     *
     * Sets the layout.
     *
     * @param  layout  the layout
     *
     ******************************************************************************************************************/
    public void renderSiteNode (@Nonnull SiteNode siteNode)
      throws IOException;
  }
