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
    public static final Key<String> P_TITLE_PREFIX = new Key<String>("titlePrefix") {};

    /** The list of relative paths or URLs for screen CSS style sheets. */
    public static final Key<List<String>> P_SCREEN_STYLE_SHEETS = new Key<List<String>>("screenStyleSheets") {};

    /** The list of relative paths or URLs for print CSS style sheets. */
    public static final Key<List<String>> P_PRINT_STYLE_SHEETS = new Key<List<String>>("printStyleSheets") {};

    /** The list of relative paths for inlined RSS feeds. */
    public static final Key<List<String>> P_RSS_FEEDS = new Key<List<String>>("rssFeeds") {};

    /** The list of relative paths or URLs for external JavaScript scripts. */
    public static final Key<List<String>> P_SCRIPTS = new Key<List<String>>("scripts") {};

    /** The list of relative paths for inlined JavaScript scripts. */
    public static final Key<List<String>> P_INLINED_SCRIPTS = new Key<List<String>>("inlinedScripts") {};

    /** Dynamic property generated during rendering with the title of the blog post. */
    public static final Key<String> PD_TITLE = new Key<String>("@title") {};

    /** Dynamic property generated during rendering with the permalink of the blog post. */
    public static final Key<String> PD_URL = new Key<String>("@url") {};

    /** Dynamic property generated during rendering with the unique id of the blog post. */
    public static final Key<String> PD_ID = new Key<String>("@id") {};

    /** Dynamic property generated during rendering with the id of the primary image to be associated to this view. */
    public static final Key<String> PD_IMAGE_ID = new Key<String>("@imageId") {};
  }
