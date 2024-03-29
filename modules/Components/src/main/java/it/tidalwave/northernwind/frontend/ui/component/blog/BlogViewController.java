/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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

/***********************************************************************************************************************
 *
 * A controller for rendering a blog.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface BlogViewController extends ViewController
  {
    /** The max. number of posts to render in full. */
    public static final Key<Integer> P_MAX_FULL_ITEMS = Key.of("maxFullItems", Integer.class);

    /** The max. number of posts to render with lead-in text. */
    public static final Key<Integer> P_MAX_LEADIN_ITEMS = Key.of("maxLeadInItems", Integer.class);

    /** The max. number of posts to render. */
    public static final Key<Integer> P_MAX_ITEMS = Key.of("maxItems", Integer.class);

    /** Flag to switch to 'index' mode. */
    public static final Key<Boolean> P_INDEX = Key.of("index", Boolean.class);

    /** Flag to switch to 'tag cloud' mode. */
    public static final Key<Boolean> P_TAG_CLOUD = Key.of("tagCloud", Boolean.class);

    /** The category associated to a post. */
    public static final Key<String> P_CATEGORY = Key.of("category", String.class);

    /** The id of the image to be associated with a blog post. */
    public static final Key<String> P_IMAGE_ID = Key.of("imageId", String.class);
  }
