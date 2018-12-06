/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import java.util.List;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.ViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface NodeContainerViewController extends ViewController
  {
    /** The prefix to prepend to the HTML title. */
    public static final Key<String> PROPERTY_TITLE_PREFIX = new Key<>("titlePrefix");

    /** The list of relative paths or URLs for screen CSS style sheets. */
    public static final Key<List<String>> PROPERTY_SCREEN_STYLE_SHEETS = new Key<>("screenStyleSheets");

    /** The list of relative paths or URLs for print CSS style sheets. */
    public static final Key<List<String>> PROPERTY_PRINT_STYLE_SHEETS = new Key<>("printStyleSheets");

    /** The list of relative paths for inlined RSS feeds. */
    public static final Key<List<String>> PROPERTY_RSS_FEEDS = new Key<>("rssFeeds");

    /** The list of relative paths or URLs for external JavaScript scripts. */
    public static final Key<List<String>> PROPERTY_SCRIPTS = new Key<>("scripts");

    /** The list of relative paths for inlined JavaScript scripts. */
    public static final Key<List<String>> PROPERTY_INLINED_SCRIPTS = new Key<>("inlinedScripts");

    /** The id of the primary image to be associated to this page. */
    public static final Key<String> PROPERTY_DYNAMIC_IMAGE_ID = new Key<>("@imageId");
  }
