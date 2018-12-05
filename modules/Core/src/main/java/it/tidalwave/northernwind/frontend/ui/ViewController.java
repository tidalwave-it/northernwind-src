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
    /*******************************************************************************************************************
     *
     * Initializes the component. If the class has a superclass, remember to call {@code super.initialize()}.
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
     * Renders the component to a view.
     *
     * TODO: pass the Request here and drop RequestHolder.
     *
     * @throws      Exception       in case of problems - it will cause a fatal error (such as HTTP status 500)
     *
     ******************************************************************************************************************/
    default public void renderView()
      throws Exception
      {
      }
  }
