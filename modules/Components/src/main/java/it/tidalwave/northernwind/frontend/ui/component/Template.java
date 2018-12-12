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
package it.tidalwave.northernwind.frontend.ui.component;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import org.stringtemplate.v4.ST;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

/******************************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 *****************************************************************************************************************************/
@Slf4j
public class Template
  {
    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public static class Aggregate
      {
        @Getter
        private final Map<String, Object> map = new HashMap<>();

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        public Aggregate with (final @Nonnull String name, final @Nonnull Object value)
          {
            map.put(name, value);
            return this;
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        public Aggregate with (final @Nonnull String name, final @Nonnull Optional<? extends Object> value)
          {
            value.ifPresent(v -> map.put(name, v));
            return this;
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        @Override @Nonnull
        public String toString()
          {
            return map.toString();
          }
      }

    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public static class Aggregates implements Iterable<Aggregate>
      {
        public static final Aggregates EMPTY = new Aggregates("", emptyList());
        
        @Getter @Nonnull
        private final String name;

        @Getter
        private final List<Aggregate> aggregates = new ArrayList<>();

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        public Aggregates (final @Nonnull String name, final @Nonnull List<Aggregate> aggregates)
          {
            this.name = name;
            this.aggregates.addAll(aggregates);
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        public boolean isEmpty()
          {
            return aggregates.isEmpty();
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        @Override @Nonnull
        public String toString()
          {
            return String.format("{%s: %s}", name, aggregates);
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        @Override
        public Iterator<Aggregate> iterator()
          {
            return aggregates.iterator();
          }

        /**********************************************************************************************************************
         *
         * Returns a {@link Collector} that produces an instance of {@link Aggregates} with the given name.
         *
         * @param       name            the name
         * @return                      the collector
         *
         *********************************************************************************************************************/
        @Nonnull
        public static Collector<Aggregate, ?, Aggregates> toAggregates (final @Nonnull String name)
          {
            return collectingAndThen(toList(), list -> new Aggregates(name, list));
          }

        /**********************************************************************************************************************
         *
         *********************************************************************************************************************/
        private int getSize()
          {
            return aggregates.size();
          }
      }

    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public Template (final @Nonnull String templateText)
      {
        log.trace("Creating template: {}", templateText);
//        stg = new STGroup('$', '$');
//        stg.defineTemplate("main", templateText);
//        st = stg.getInstanceOf("main");
        st = new ST(templateText, '$', '$');
//        this.templateText = templateText;
      }

//    @Nonnull
//    private final STGroup stg;

    @Nonnull
    private final ST st;

//    @Nonnull
//    private final String templateText;

//    public void include (final @Nonnull String name, final @Nonnull Template template)
//      {
//        stg.defineTemplate(name, template.templateText);
//      }

    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public void addAttribute (final @Nonnull String name, final @Nonnull String value)
      {
        st.add(name, value);
      }

    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public String render (final @Nonnull Aggregates ... aggregatesSet)
      {
        for (final Aggregates aggregates : aggregatesSet)
          {
            if (aggregates.getSize() == 1)
              {
                st.add(aggregates.getName(), asList(aggregates.iterator().next().getMap()));
              }
            else
              {
                for (final Aggregate aggregate : aggregates)
                  {
                    st.add(aggregates.getName(), aggregate.getMap());
                  }
              }
          }

        return st.render();
      }
  }
