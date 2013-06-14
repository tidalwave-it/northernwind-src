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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
@RequiredArgsConstructor
public class DefaultLayoutFinder extends FinderSupport<Layout, LayoutFinder> implements LayoutFinder
  {
    private static final long serialVersionUID = 2354576587657345L;
    
    @Nonnull
    private final List<Layout> children;

    @Nonnull
    private final Map<Id, Layout> childrenMapById;

    @CheckForNull
    private Id id;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public LayoutFinder withId (final @Nonnull Id id)
      {
        final DefaultLayoutFinder clone = (DefaultLayoutFinder)super.clone();
        clone.id = id;
        return clone;
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
