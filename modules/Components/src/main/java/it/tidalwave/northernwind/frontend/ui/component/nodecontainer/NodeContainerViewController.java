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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import java.util.List;
import it.tidalwave.util.Key;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface NodeContainerViewController 
  {  
    public static final Key<String> PROPERTY_TITLE_PREFIX = new Key<String>("titlePrefix");
    
    public static final Key<List<String>> PROPERTY_SCREEN_STYLE_SHEETS = new Key<List<String>>("screenStyleSheets");
    
    public static final Key<List<String>> PROPERTY_PRINT_STYLE_SHEETS = new Key<List<String>>("printStyleSheets");
    
    public static final Key<List<String>> PROPERTY_RSS_FEEDS = new Key<List<String>>("rssFeeds");
    
    public static final Key<List<String>> PROPERTY_SCRIPTS = new Key<List<String>>("scripts");
    
    public static final Key<List<String>> PROPERTY_INLINED_SCRIPTS = new Key<List<String>>("inlinedScripts");
    
    public static final Key<String> PROPERTY_TEMPLATE_RESOURCE = new Key<String>("template");
  }
