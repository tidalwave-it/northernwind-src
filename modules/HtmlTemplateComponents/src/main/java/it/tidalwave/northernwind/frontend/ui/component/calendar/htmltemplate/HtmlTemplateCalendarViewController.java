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
package it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.text.DateFormatSymbols;
import java.util.Optional;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.IntStream;
import it.tidalwave.util.InstantProvider;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController.Entry;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.CalendarDao;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link CalendarViewController} based on HTML templates.</p>
 *
 * <p>The template for rendering the page can be specified by means of the property {@code P_TEMPLATE_PATH}.</p>
 *
 * <p>This controller calls render methods to the view by passing {@link Aggregates} to be used with templates:</p>
 *
 * <ul>
 * <li>{@code month1 ... month12}: the month names in the proper language;</li>
 * <li>{@code selectedYear}: the year for the current rendering;</li>
 * <li>{@code years}: all the available years;</li>
 * <li>{@code entries1 ... entries12}: the entries for each month.</li>
 * </ul>
 *
 * <p>Each {@code entry} is an {@link Aggregate} of the following fields:</p>
 *
 * <ul>
 * <li>{@code label}: the label of the entry;</li>
 * <li>{@code link}: the link of the entry;</li>
 * <li>{@code class}: the CSS class of the entry.</li>
 * </ul>
 *
 * <p>Each {@code year} is an {@link Aggregate} of the following fields:</p>
 *
 * <ul>
 * <li>{@code number}: the number of the year;</li>
 * <li>{@code link}: the target URL (not present for the current year).</li>
 * </ul>
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
    public HtmlTemplateCalendarViewController (final @Nonnull HtmlTemplateCalendarView view,
                                               final @Nonnull SiteNode siteNode,
                                               final @Nonnull RequestLocaleManager requestLocaleManager,
                                               final @Nonnull CalendarDao dao,
                                               final @Nonnull InstantProvider instantProvider)
      {
        super(view, siteNode, requestLocaleManager, dao, instantProvider);
        this.view = view;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void render (final Optional<String> title,
                           final @Nonnegative int year,
                           final @Nonnegative int firstYear,
                           final @Nonnegative int lastYear,
                           final @Nonnull SortedMap<Integer, List<Entry>> byMonth,
                           final @Nonnegative int columns)
      {
        final Aggregates years = IntStream.rangeClosed(firstYear, lastYear)
                                          .mapToObj(y -> toAggregate(y, y == year))
                                          .collect(toAggregates("years"));

        final Map<String, String> months = new TreeMap<>();
        final String[] monthNames = DateFormatSymbols.getInstance(requestLocaleManager.getLocales().get(0)).getMonths();
        IntStream.rangeClosed(1, monthNames.length).forEach(i -> months.put("" + i, monthNames[i - 1]));

        final Map<String, List<Map<String, Object>>> entries = new TreeMap<>();
                IntStream.rangeClosed(1, 12).forEach(m -> entries.put("" + m, byMonth.getOrDefault(m, emptyList())
                                                                                      .stream()
                                                                                      .map(x -> toAggregate(x).getMap())
                                                                                      .collect(toList())));
        log.debug("monthNames: {}", months);
        log.debug("entries:    {}", entries);
        view.render(title,
                    getViewProperties().getProperty(P_TEMPLATE_PATH),
                    months,
                    Integer.toString(year),
                    years,
                    entries,
                    columns);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (final @Nonnull Entry entry)
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
