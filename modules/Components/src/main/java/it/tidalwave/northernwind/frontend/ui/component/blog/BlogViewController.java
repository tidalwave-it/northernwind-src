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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.blog;

import java.util.List;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.CompositeSiteNodeController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface BlogViewController extends CompositeSiteNodeController
  {    
    public static final Key<List<String>> PROPERTY_CONTENTS = new Key<List<String>>("contents"); 
    
    // FIXME: these should be Integer
    public static final Key<String> PROPERTY_MAX_FULL_ITEMS = new Key<String>("maxFullItems"); 
    
    public static final Key<String> PROPERTY_MAX_ITEMS = new Key<String>("maxItems"); 
    
    public static final Key<String> PROPERTY_MAX_LEADIN_ITEMS = new Key<String>("maxLeadInItems"); 
    
    // FIXME: this should be Boolean
    public static final Key<String> PROPERTY_INDEX = new Key<String>("index"); 
    
    public static final Key<String> PROPERTY_CATEGORY = new Key<String>("category"); 
  }
