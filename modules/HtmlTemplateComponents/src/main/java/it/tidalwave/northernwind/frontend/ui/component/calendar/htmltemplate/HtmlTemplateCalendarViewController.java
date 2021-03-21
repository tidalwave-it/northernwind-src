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
package it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.text.DateFormatSymbols;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import it.tidalwave.util.TimeProvider;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.CalendarDao;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;
import static it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate.Pair.*;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link CalendarViewController} based on HTML templates.</p>
 *
 * <p>The template for rendering the page can be specified by means of the property {@code P_TEMPLATE_PATH}.</p>
 *
 * @see     HtmlTemplateCalendarView
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateCalendarViewController extends DefaultCalendarViewController
  {
    @Nonnull
    private final HtmlTemplateCalendarView view;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public HtmlTemplateCalendarViewController (@Nonnull final HtmlTemplateCalendarView view,
                                               @Nonnull final SiteNode siteNode,
                                               @Nonnull final RequestLocaleManager requestLocaleManager,
                                               @Nonnull final CalendarDao dao,
                                               @Nonnull final TimeProvider timeProvider)
      {
        super(view, siteNode, requestLocaleManager, dao, timeProvider);
        this.view = view;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void render (@Nonnull final Optional<String> title,
                           @Nonnegative final int year,
                           @Nonnegative final int firstYear,
                           @Nonnegative final int lastYear,
                           @Nonnull final SortedMap<Integer, List<Entry>> byMonth,
                           @Nonnegative final int columns)
      {
        final Aggregates years = IntStream.rangeClosed(firstYear, lastYear)
                                          .mapToObj(y -> toAggregate(y, y == year))
                                          .collect(toAggregates("years"));

        final String[] monthNames = DateFormatSymbols.getInstance(requestLocaleManager.getLocales().get(0)).getMonths();

        final IntFunction<List<Map<String, Object>>> entriesByMonth =
            i -> byMonth.getOrDefault(i + 1, emptyList()).stream().map(x -> toAggregate(x).getMap()).collect(toList());

        view.render(title,
                    getViewProperties().getProperty(P_TEMPLATE_PATH),
                    indexedPairStream1(monthNames).collect(pairsToMap()),
                    Integer.toString(year),
                    years,
                    indexedPairStream1(0, 12, entriesByMonth).collect(pairsToMap()),
                    columns);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Aggregate toAggregate (@Nonnull final Entry entry)
      {
        return Aggregate.of(  "label", entry.name)
                        .with("link",  entry.link)
                        .with("class", entry.type.map(s -> "nw-calendar-table-link-" + s));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (final int year, final boolean withLink)
      {
        final Aggregate a = Aggregate.of("number", year);
        return withLink ? a : a.with("link", createYearLink(year));
      }
  }
