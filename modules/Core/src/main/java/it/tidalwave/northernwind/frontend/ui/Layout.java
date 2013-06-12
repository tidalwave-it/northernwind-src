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
package it.tidalwave.northernwind.frontend.ui;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.role.Identifiable;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface Layout extends As, Identifiable
  {
    /*******************************************************************************************************************
     *
     * A builder of a {@link Layout}.
     *
     ******************************************************************************************************************/
    @AllArgsConstructor(access = AccessLevel.PRIVATE) @NoArgsConstructor
    @Wither @Getter @ToString(exclude = "callBack")
    public final class Builder
      {
        // Workaround for a Lombok limitation with Wither and subclasses
        public static interface CallBack
          {
            @Nonnull
            public Layout build (@Nonnull Layout.Builder builder);
          }

        private Layout.Builder.CallBack callBack;
        private Id id;
        private String type;

        @Nonnull
        public Layout build()
          {
            return callBack.build(this);
          }
      }

    /*******************************************************************************************************************
     *
     * Returns the type URI for this layout.
     *
     * @return  the type URI
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getTypeUri();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Layout withLayout (@Nonnull Layout layout);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Layout withOverride (@Nonnull Layout override);

    /*******************************************************************************************************************
     *
     * @throws  NotFoundException   if no view component is found
     * @throws  HttpStatusException if a component asked to return a specific HTTP status
     *
     ******************************************************************************************************************/
    @Nonnull
    public ViewAndController createViewAndController (@Nonnull SiteNode siteNode)
      throws NotFoundException, HttpStatusException;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull // TODO: refactor with Composite
    public List<Layout> getChildren();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull // TODO: refactor with Composite
    public <Type> Type accept (@Nonnull Visitor<Layout, Type> visitor)
      throws NotFoundException;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Layout findSubComponentById (@Nonnull Id id)
      throws NotFoundException;
  }
