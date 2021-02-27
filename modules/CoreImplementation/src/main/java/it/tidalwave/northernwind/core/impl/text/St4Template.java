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
package it.tidalwave.northernwind.core.impl.text;

import javax.annotation.Nonnull;
import org.stringtemplate.v4.ST;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.asList;

/******************************************************************************************************************************
 *
 * An implementation of {@link Template} based on StringTemplate ({@link ST}.
 *
 * @author  Fabrizio Giudici
 *
 *****************************************************************************************************************************/
@Slf4j
public class St4Template implements Template
  {
//    @Nonnull
//    private final STGroup stg;

    @Nonnull
    private final ST st;

    /**************************************************************************************************************************
     *
     *************************************************************************************************************************/
    public St4Template (final @Nonnull String templateText, final char delimiter)
      {
        log.trace("Creating template: {} - {}", templateText, delimiter);
//        stg = new STGroup(delimiter, delimiter);
//        stg.defineTemplate("main", templateText);
//        st = stg.getInstanceOf("main");
        st = new ST(templateText, delimiter, delimiter);
      }

//    public void include (final @Nonnull String name, final @Nonnull Template template)
//      {
//        stg.defineTemplate(name, template.templateText);
//      }

    /**************************************************************************************************************************
     *
     * {@inheritDoc}
     *
     *************************************************************************************************************************/
    @Override
    public Template addAttribute (final @Nonnull String name, final @Nonnull Object value)
      {
        st.add(name, value);
        return this;
      }

    /**************************************************************************************************************************
     *
     * {@inheritDoc}
     *
     *************************************************************************************************************************/
    @Override @Nonnull
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
