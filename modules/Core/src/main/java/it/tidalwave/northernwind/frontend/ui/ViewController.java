/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui;

import javax.annotation.Nonnull;
import it.tidalwave.util.Finder;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.spi.VirtualSiteNode;

/***********************************************************************************************************************
 *
 * The common ancestor of all controllers of views.
 *
 * @stereotype  Presentation Controller
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@SuppressWarnings("squid:S00112")
public interface ViewController
  {
    /*******************************************************************************************************************
     *
     * Initializes the component. If the class has a superclass, remember to call {@code super.initialize()}.
     * This method must execute quickly, as it is called whenever a new instance is created - consider that some
     * components, such as the one rendering a site map, are likely to instantiate lots of controllers.
     *
     * @throws      Exception       in case of problems
     *
     ******************************************************************************************************************/
    default public void initialize()
      throws Exception
      {
      }

    /*******************************************************************************************************************
     *
     * Prepares the component for rendering, for instance by checking preconditions or by setting dynamic properties.
     * If the class has a superclass, remember to call {@code super.prepareRendering(context)}.
     *
     * It should also do formal validation and eventually fail fast.
     *
     * @param       context         the context for rendering
     * @throws      Exception       in case of problems
     *
     ******************************************************************************************************************/
    default public void prepareRendering (final @Nonnull RenderContext context)
      throws Exception
      {
      }

    /*******************************************************************************************************************
     *
     * Renders the component to a view.
     *
     * @param       context         the context for rendering
     * @throws      Exception       in case of problems - it will cause a fatal error (such as HTTP status 500)
     *
     ******************************************************************************************************************/
    default public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
      }

    /*******************************************************************************************************************
     *
     * Controllers which manage composite site nodes must override this method and return a collection of
     * {@link SiteNode}s, one for each composite content. For instance, the controller of a gallery should return the
     * relative paths of all the media pages in the gallery; the controller of a blog should return the relative paths
     * of all the posts.
     *
     * See {@link VirtualSiteNode} for a convenient implementation of {@code SiteNode} to return.
     *
     * @see         VirtualSiteNode
     * @return                      the virtual nodes
     *
     ******************************************************************************************************************/
    @Nonnull
    default public Finder<SiteNode> findVirtualSiteNodes()
      {
        return FinderSupport.emptyFinder();
      }
  }
