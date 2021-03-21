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
package it.tidalwave.northernwind.frontend.ui;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * A factory for Views.
 *
 * @stereotype  Factory
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@FunctionalInterface
public interface ViewFactory
  {
    @RequiredArgsConstructor @Getter @ToString
    public static class ViewAndController
      {
        @Nonnull
        private final Object view;

        @Nonnull
        private final ViewController controller;

        /***************************************************************************************************************
         *
         * Call the controller to have the view rendered, and return it.
         *
         * @param       context         the render context
         * @return                      the rendered view
         * @exception   Exception       if something failed
         *
         **************************************************************************************************************/
        @Nonnull
        public Object renderView (@Nonnull final RenderContext context)
          throws Exception
          {
            controller.renderView(context);
            return view;
          }
     }

    /*******************************************************************************************************************
     *
     * Creates a new pair of View and Controller.
     *
     * @param   viewTypeUri         the view type URI
     * @param   viewId              the view id
     * @param   siteNode            the node this view is created for
     * @return                      the new pair
     * @throws  NotFoundException   if no view component is found
     * @throws  HttpStatusException if a component asked to return a specific HTTP status
     *
     ******************************************************************************************************************/
    @Nonnull
    public ViewAndController createViewAndController (@Nonnull String viewTypeUri,
                                                      @Nonnull Id viewId,
                                                      @Nonnull SiteNode siteNode)
      throws NotFoundException, HttpStatusException;
  }
