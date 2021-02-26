/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.sitemap;

import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface SitemapViewController extends ViewController
  {
    /** The priority of the {@link SiteNode}. */
    public static final Key<Float> P_SITEMAP_PRIORITY = new Key<Float>("siteMap.priority") {};

    /** The priority of children of the {@link SiteNode} */
    public static final Key<Float> P_SITEMAP_CHILDREN_PRIORITY = new Key<Float>("siteMap.childrenPriority") {};

    /** The change frequency of {@link SiteNode} */
    public static final Key<String> P_SITEMAP_CHANGE_FREQUENCY = new Key<String>("siteMap.changeFrequency") {};

    /** The optional path to the template for rendering the sitemap. This property is different than
     * {@link P_TEMPLATE_PATH} because it refers to a specific rendering - {@code P_TEMPLATE_PATH} is for HTML pages
     * and it's usually also set for the root node, hence inherited by a sitemap. */
    public static final Key<ResourcePath> P_SITEMAP_TEMPLATE_PATH = new Key<ResourcePath>("sitemapTemplatePath") {};
  }
