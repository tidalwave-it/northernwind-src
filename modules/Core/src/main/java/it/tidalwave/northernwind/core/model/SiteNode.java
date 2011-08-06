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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.Layout;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface SiteNode extends Resource
  {
    public static final Class<SiteNode> SiteNode = SiteNode.class;
    
    /** The label used for creating navigation links to this {@code SiteNode}. */
    public static final Key<String> PROPERTY_NAVIGATION_LABEL = new Key<String>("navigationLabel");
    
    /** The local portion of relativeUri by which this {@code SiteNode} is exposed to the web. If this property is not 
     *  defined, the local portion of the relative path is used. */
    public static final Key<String> PROPERTY_EXPOSED_URI = new Key<String>("exposedUri");
    
    /*******************************************************************************************************************
     *
     * Returns the {@link Layout} of this {@code SiteNode}.
     * 
     * @return   the {@code Layout}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Layout getLayout();

    /*******************************************************************************************************************
     *
     * Returns the relative URI of this {@code SiteNode}, which is the one exposed to the web. By default the relative
     * URI is the same as the relative path of the associated file, but each {@code SiteNode} can override it by setting
     * {@link #PROPERTY_EXPOSED_URI}.
     * 
     * @return  the relative URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getRelativeUri();
  }