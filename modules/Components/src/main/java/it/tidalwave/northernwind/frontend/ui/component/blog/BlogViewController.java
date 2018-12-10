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
    public static final Key<Integer> P_MAX_FULL_ITEMS = new Key<Integer>("maxFullItems") {};

    public static final Key<Integer> P_MAX_ITEMS = new Key<Integer>("maxItems") {};

    public static final Key<Integer> P_MAX_LEADIN_ITEMS = new Key<Integer>("maxLeadInItems") {};

    public static final Key<Boolean> P_INDEX = new Key<Boolean>("index") {};

    public static final Key<String> P_CATEGORY = new Key<String>("category") {};

    public static final Key<Boolean> P_TAG_CLOUD = new Key<Boolean>("tagCloud") {};

    /** The id of the image to be associated with a blog post. */
    public static final Key<String> P_IMAGE_ID = new Key<String>("imageId") {};
  }
