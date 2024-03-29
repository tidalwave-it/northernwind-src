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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.spi.RequestResettable;

/***********************************************************************************************************************
 *
 * The context for a {@link Request} provides access to some items that are only available during the processing of
 * the request.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface RequestContext extends RequestResettable
  {
    /*******************************************************************************************************************
     *
     * Sets the current {@link Content}.
     *
     * @param  content  the current {@code Content}
     *
     ******************************************************************************************************************/
    public void setContent (@Nonnull Content content);

    /*******************************************************************************************************************
     *
     * Returns the current {@link Content} properties.
     *
     * @return  the properties
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getContentProperties();

    /*******************************************************************************************************************
     *
     * Clears the current {@link Resource}.
     *
     ******************************************************************************************************************/
    public void clearContent();

    /*******************************************************************************************************************
     *
     * Sets the current {@link SiteNode}.
     *
     * @param  node  the current {@code SiteNode}
     *
     ******************************************************************************************************************/
    public void setNode (@Nonnull SiteNode node);

    /*******************************************************************************************************************
     *
     * Returns the current {@link SiteNode} properties.
     *
     * @return  the properties
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getNodeProperties();

    /*******************************************************************************************************************
     *
     * Clears the current {@link SiteNode}.
     *
     ******************************************************************************************************************/
    public void clearNode();

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
    public <T> void setDynamicNodeProperty (@Nonnull Key<T> key, @Nonnull T value);
  }
