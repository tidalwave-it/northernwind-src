/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;
import lombok.Getter;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface Template
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static class Aggregate
      {
        @Getter
        private final Map<String, Object> map = new HashMap<>();

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public static Aggregate of (final @Nonnull String name, final @Nonnull Object value)
          {
            return new Aggregate().with(name, value);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public static Aggregate of (final @Nonnull String name, final @Nonnull Optional<? extends Object> value)
          {
            return new Aggregate().with(name, value);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Aggregate with (final @Nonnull String name, final @Nonnull Object value)
          {
            map.put(name, value);
            return this;
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Aggregate with (final @Nonnull String name, final @Nonnull Optional<? extends Object> value)
          {
            value.ifPresent(v -> map.put(name, v));
            return this;
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Optional<Object> get (final @Nonnull String name)
          {
            return Optional.ofNullable(map.get(name));
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String toString()
          {
            return map.toString();
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static class Aggregates implements Iterable<Aggregate>
      {
        public static final Aggregates EMPTY = new Aggregates("", emptyList());

        @Getter @Nonnull
        private final String name;

        @Getter @SuppressWarnings("squid:S1700")
        private final List<Aggregate> aggregates = new ArrayList<>();

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        public Aggregates (final @Nonnull String name, final @Nonnull List<Aggregate> aggregates)
          {
            this.name = name;
            this.aggregates.addAll(aggregates);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        public boolean isEmpty()
          {
            return aggregates.isEmpty();
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String toString()
          {
            return String.format("{%s: %s}", name, aggregates);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public Iterator<Aggregate> iterator()
          {
            return aggregates.iterator();
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Stream<Aggregate> stream()
          {
            return aggregates.stream();
          }

        /***************************************************************************************************************
         *
         * Returns a {@link Collector} that produces an instance of {@link Aggregates} with the given name.
         *
         * @param       name            the name
         * @return                      the collector
         *
         **************************************************************************************************************/
        @Nonnull
        public static Collector<Aggregate, ?, Aggregates> toAggregates (final @Nonnull String name)
          {
            return collectingAndThen(toList(), list -> new Aggregates(name, list));
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        public int getSize()
          {
            return aggregates.size();
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public Template addAttribute (@Nonnull String name, @Nonnull Object value);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public String render (@Nonnull final Aggregates ... aggregatesSet);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public default String render (@Nonnull final List<Aggregates> aggregatesSet)
      {
        return render(aggregatesSet.toArray(new Aggregates[0]));
      }
  }
