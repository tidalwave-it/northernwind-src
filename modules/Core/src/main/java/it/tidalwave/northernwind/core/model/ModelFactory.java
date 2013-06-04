/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.ui.Layout;

/***********************************************************************************************************************
 *
 * A factory for creating domain objects.
 * 
 * FIXME: creation of a Site use a builder, while other entities are directly created - perhaps use a builder for all?
 *
 * @author  Fabrizio Giudici
 * @version $Id: $
 *
 **********************************************************************************************************************/
public interface ModelFactory
  {
    /*******************************************************************************************************************
     *
     * Creates a new {@link Resource}.
     *
     * @param  file  the file for the {@code Resource}
     * @return       the {@code Resource}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Resource createResource (@Nonnull ResourceFile file);

    /*******************************************************************************************************************
     *
     * Creates a new {@link Content}.
     *
     * @param  file  the file for the {@code Content}
     * @return       the {@code Content}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Content createContent (@Nonnull ResourceFile file);

    /*******************************************************************************************************************
     *
     * Creates a new {@link Media}.
     *
     * @param  file  the file for the {@code Media}
     * @return       the {@code Media}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Media createMedia (@Nonnull ResourceFile file);

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
    public Layout createLayout (@Nonnull Id id, @Nonnull String type);

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
    public ResourceProperties createProperties (@Nonnull Id id);

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
