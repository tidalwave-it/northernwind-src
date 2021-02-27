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
import it.tidalwave.northernwind.core.model.Request;

/***********************************************************************************************************************
 *
 * The controller of {@link SiteView}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface SiteViewController
  {
    /*******************************************************************************************************************
     *
     * Processes a {@link Request} and updates the view of the site. This can happen by producing a response that must
     * be returned to the HTTP response (typical for HTML-based technologies or for straightly exposing resources such
     * as media or RSS feeds) or by side effect (typical for JavaScript-based technologies); in the latter case, the
     * returned value is meaningless.
     *
     * @param   request   the {@code Request}
     * @return            the response
     *
     ******************************************************************************************************************/
    @Nonnull
    public <ResponseType> ResponseType processRequest (@Nonnull Request request);
  }
