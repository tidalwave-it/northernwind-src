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
package it.tidalwave.northernwind.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import static java.util.Collections.*;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE)
public final class CollectionFunctions
  {
    /*******************************************************************************************************************
     *
     * Returns a concatenation of the given {@link Collection}s.
     *
     * @param       <T>             the static type
     * @param       collections     the input collections
     * @return                      the concatenation
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> List<T>   concat (@Nonnull final Collection<T>... collections)
      {
        final List<T> result = new ArrayList<>();

        for (final Collection<T> collection : collections)
          {
            result.addAll(collection);
          }

        return result;
      }

    /*******************************************************************************************************************
     *
     * Return a sublist of the original {@link List}, from the given being and end index. If the end index is lower
     * than the start index or if an attempt is made to read past the end of the list, truncation silently occurs.
     *
     * @param       <T>             the static type
     * @param       list            the original list
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @return                      the sublist
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> List<T> safeSubList (@Nonnull final List<T> list, final int from, final int to)
      {
        final int to2 = Math.min(list.size(), to);
        return (from >= to2) ? emptyList() : list.subList(from, to2);
      }

    /*******************************************************************************************************************
     *
     * Splits a given {@link List} at a set of boundaries.
     *
     * @param       <T>             the static type
     * @param       list            the original list
     * @param       boundary        the boundaries
     * @return                      a list of sublists
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> List<List<T>> split (@Nonnull final List<T> list, final int ... boundary)
      {
        final List<List<T>> result = new ArrayList<>();

        for (int i = 0; i < boundary.length - 1; i++)
          {
            result.add(safeSubList(list, boundary[i], boundary[i + 1]));
          }

        return result;
      }
  }
