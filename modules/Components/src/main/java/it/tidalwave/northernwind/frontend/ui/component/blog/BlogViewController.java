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
package it.tidalwave.northernwind.frontend.ui.component.blog;

import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.CompositeSiteNodeController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface BlogViewController extends CompositeSiteNodeController, ViewController
  {
    // FIXME: these should be Integer
    public static final Key<String> PROPERTY_MAX_FULL_ITEMS = new Key<>("maxFullItems");

    public static final Key<String> PROPERTY_MAX_ITEMS = new Key<>("maxItems");

    public static final Key<String> PROPERTY_MAX_LEADIN_ITEMS = new Key<>("maxLeadInItems");

    // FIXME: this should be Boolean
    public static final Key<String> PROPERTY_INDEX = new Key<>("index");

    public static final Key<String> PROPERTY_CATEGORY = new Key<>("category");

    // FIXME: this should be Boolean
    public static final Key<String> PROPERTY_TAG_CLOUD = new Key<>("tagCloud");

    // TODO: find a proper name space, possibly merging with other - or defining @ as dynamic properties
    /** Dynamic property generated during rendering with the title of the blog post. */
    public static final Key<String> PROPERTY_DYNAMIC_TITLE = new Key<>("@title");

    /** Dynamic property generated during rendering with the permalink of the blog post. */
    public static final Key<String> PROPERTY_DYNAMIC_URL = new Key<>("@url");

    /** Dynamic property generated during rendering with the unique id of the blog post. */
    public static final Key<String> PROPERTY_DYNAMIC_ID = new Key<>("@id");

  }
