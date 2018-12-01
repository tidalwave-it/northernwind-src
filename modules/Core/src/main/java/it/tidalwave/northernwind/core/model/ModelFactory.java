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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.ui.Layout;

/***********************************************************************************************************************
 *
 * A factory for creating domain objects.
 *
 * TODO: use a builder for all products
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface ModelFactory
  {
    /*******************************************************************************************************************
     *
     * Creates a new {@link Resource}.
     *
     * @return       a builder for the {@code Resource}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Resource.Builder createResource();

    /*******************************************************************************************************************
     *
     * Creates a new {@link Content}.
     *
     * @return       a builder for the {@code Content}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Content.Builder createContent();

    /*******************************************************************************************************************
     *
     * Creates a new {@link Media}.
     *
     * @return       a builder for the {@code Media}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Media.Builder createMedia();

    /*******************************************************************************************************************
     *
     * Creates a new {@link SiteNode}.
     *
     * @param  site    the {@code Site} that the {@code SiteNode} belongs to
     * @param  folder  the folder representing the {@code SiteNode}
     * @return         the {@code SiteNode}
     *
     ******************************************************************************************************************/
    @Nonnull
    public SiteNode createSiteNode (@Nonnull Site site, @Nonnull ResourceFile folder)
      throws IOException, NotFoundException;

    /*******************************************************************************************************************
     *
     * Creates a new {@link Layout}.
     *
     * @param  id    the id
     * @param  type  the type
     * @return       the {@code Layout}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Layout.Builder createLayout();

    /*******************************************************************************************************************
     *
     * Creates a new {@link Request}.
     *
     * @return       the {@code Request}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Request createRequest();

    /*******************************************************************************************************************
     *
     * Creates a new {@link Request} from a given {@link HttpServletRequest}.
     *
     * @param        httpServletRequest   the {@code HttpServletRequest}
     * @return                            the {@code Request}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Request createRequestFrom (@Nonnull HttpServletRequest httpServletRequest);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties.Builder createProperties();

    /*******************************************************************************************************************
     *
     * Creates a new {@link Site}.
     *
     * @return  a builder for the new {@code Site}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Site.Builder createSite();
  }
