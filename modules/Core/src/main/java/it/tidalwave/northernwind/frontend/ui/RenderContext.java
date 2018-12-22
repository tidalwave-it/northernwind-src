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
import java.util.Optional;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteNode;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface RenderContext
  {
    @Nonnull
    public Request getRequest();

    @Nonnull
    public RequestContext getRequestContext();

    /*******************************************************************************************************************
     *
     * Sets a dynamic node property. These properties can be associated to the current {@link SiteNode}, created in
     * a dynamic fashion while processing the {@link Request} and available only in the {@link RequestContext}.
     *
     * This property will be made available by {@link #getNodeProperties()}, which e.g. is used by the
     * {@code $nodeProperty(...)$} macro.
     *
     * @param       key         the property key
     * @param       value       the property value
     *
     ******************************************************************************************************************/
    default public <T> void setDynamicNodeProperty (@Nonnull Key<T> key, @Nonnull T value)
      {
        getRequestContext().setDynamicNodeProperty(key, value);
      }

    /*******************************************************************************************************************
     *
     * Returns a query parameter.
     *
     * @param       name        the name of the parameter
     * @return                  the value
     *
     ******************************************************************************************************************/
    @Nonnull
    default public Optional<String> getQueryParam (final @Nonnull String name)
      {
        return getRequest().getParameter(name);
      }

    /*******************************************************************************************************************
     *
     * Returns the path parameters.
     *
     * @param       siteNode    the node that needs the parameters
     * @return                  the path parameters
     *
     ******************************************************************************************************************/
    @Nonnull
    default public ResourcePath getPathParams (final @Nonnull SiteNode siteNode)
      {
        return getRequest().getPathParams(siteNode);
      }
  }

