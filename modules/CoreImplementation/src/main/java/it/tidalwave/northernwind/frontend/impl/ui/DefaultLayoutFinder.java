/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import it.tidalwave.util.Id;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.LayoutFinder;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * A default implementation of {@link LayoutFinder}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable
@RequiredArgsConstructor
public class DefaultLayoutFinder extends FinderSupport<Layout, LayoutFinder> implements LayoutFinder
  {
    private static final long serialVersionUID = 2354576587657345L;

    @Nonnull
    private final List<Layout> children;

    @Nonnull
    private final Map<Id, Layout> childrenMapById;

    @CheckForNull
    private final Id id;

    /*******************************************************************************************************************
     *
     * Constructor used to create an instance with the given data.
     *
     * @param children          the list of children
     * @param childrenMapById   the map of children indexed by their id
     *
     ******************************************************************************************************************/
    public DefaultLayoutFinder (final @Nonnull List<Layout> children, final @Nonnull Map<Id, Layout> childrenMapById)
      {
        this.children = children;
        this.childrenMapById = childrenMapById;
        this.id = null;
      }

    /*******************************************************************************************************************
     *
     * Clone constructor. See documentation of {@link FinderSupport} for more information.
     *
     * @param other     the {@code Finder} to clone
     * @param override  the override object
     *
     ******************************************************************************************************************/
    protected DefaultLayoutFinder (final @Nonnull DefaultLayoutFinder other, final @Nonnull Object override)
      {
        super(other, override);
        final DefaultLayoutFinder source = getSource(DefaultLayoutFinder.class, other, override);
        this.children = source.children;
        this.childrenMapById = source.childrenMapById;
        this.id = source.id;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public LayoutFinder withId (final @Nonnull Id id)
      {
        return clone(new DefaultLayoutFinder(children, childrenMapById, id));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected List<? extends Layout> computeNeededResults()
      {
        final ArrayList<Layout> result = new ArrayList<>();

        if (id != null)
          {
            final Layout child = childrenMapById.get(id);

            if (child != null)
              {
                result.add(child);
              }

          }
        else
          {
            result.addAll(children);
          }

        return result;
      }
  }
