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
package it.tidalwave.northernwind.frontend.ui;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.util.Key;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * The common ancestor of all controllers of views.
 *
 * @stereotype  Presentation Controller
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface ViewController
  {
    @RequiredArgsConstructor @ToString @Getter
    public static class RenderContext // FIXME: make an interface
      {
        @Nonnull
        private final RequestContext requestContext;

        /*******************************************************************************************************************
         *
         * Sets a dynamic node property. These properties can be associated to the current {@link SiteNode}, created in
         * a dynamic fashion while processing the {@link Request} and available only in the {@link RequestContext}.
         *
         * This property will be made available by {@link #getNodeProperties()}, which e.g. is used by the
         * {@code $nodeProperty(...)$} macro.
         *
         * @param  key    the property key
         * @param  value  the property value
         *
         ******************************************************************************************************************/
        public <T> void setDynamicNodeProperty (@Nonnull Key<T> key, @Nonnull T value)
          {
            getRequestContext().setDynamicNodeProperty(key, value);
          }
      }

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
     * Initializes the component giving it a chance to make changes to the {@link RenderContext}, for instance by
     * setting a dynamic property. If the class has a superclass, remember to call {@code super.initialize(context)}.
     *
     * @param       context         the context for rendering
     * @throws      Exception       in case of problems
     *
     ******************************************************************************************************************/
    default public void initialize (final @Nonnull RenderContext context)
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
  }
