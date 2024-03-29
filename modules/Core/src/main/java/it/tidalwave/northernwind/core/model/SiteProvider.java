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

/***********************************************************************************************************************
 *
 * A provider for a {@link Site}. It handles the initial configuration and boot, in background. It also exposes a
 * {@link #reload()} method to reload the contents.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface SiteProvider
  {
    @SuppressWarnings("squid:S1700")
    public static final Class<SiteProvider> _SiteProvider_ = SiteProvider.class;

    /*******************************************************************************************************************
     *
     * Returns the {@link Site}. It always returns an object, even though it might be not initialized yet (in this case
     * {@link #isSiteAvailable()} returns {@code false}.
     *
     * @return   the site
     *
     ******************************************************************************************************************/
    @Nonnull
    public Site getSite();

    /*******************************************************************************************************************
     *
     * Reloads the {@link Site}.
     *
     ******************************************************************************************************************/
    public void reload();

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the {@link Site} is available.
     *
     ******************************************************************************************************************/
    public boolean isSiteAvailable();

    /*******************************************************************************************************************
     *
     * Returns the version string of NorthernWind.
     *
     * @return   the version
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getVersionString();
  }
