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
package it.tidalwave.northernwind.frontend.ui.component.calendar;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import it.tidalwave.util.TimeProvider;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.CalendarDao;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;
import static javax.servlet.http.HttpServletResponse.*;
import static it.tidalwave.northernwind.core.model.Content.P_TITLE;

/***********************************************************************************************************************
 *
 * <p>A default implementation of the {@link CalendarViewController} that is independent of the presentation technology.
 * This class is capable to render a yearly calendar with items and related links.</p>
 *
 * <p>It accepts a single path parameter {@code year} with selects a given year; otherwise the current year is used.</p>
 *
 * <p>Supported properties of the {@link SiteNode}:</p>
 *
 * <ul>
 * <li>{@code P_ENTRIES}: a property with XML format that describes the entries;</li>
 * <li>{@code P_SELECTED_YEAR}: the year to render (optional, otherwise the current year is used);</li>
 * <li>{@code P_FIRST_YEAR}: the first available year;</li>
 * <li>{@code P_LAST_YEAR}: the last available year ;</li>
 * <li>{@code P_TITLE}: the page title (optional);</li>
 * <li>{@code P_COLUMNS}: the number of columns of the table to render (optional, defaults to 4).</li>
 * </ul>
 *
 * <p>The property {@code P_ENTRIES} must have the following structure:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;calendar&gt;
 *     &lt;year id="2004"&gt;
 *         &lt;month id="jan"&gt;
 *             &lt;item name="Provence" type="major" link="/diary/2004/01/02/"/&gt;
 *             &lt;item name="Bocca di Magra" link="/diary/2004/01/24/"/&gt;
 *             &lt;item name="Maremma" link="/diary/2004/01/31/"/&gt;
 *         &lt;/month&gt;
 *         ...
 *     &lt;/year&gt;
 *     ...
 * &lt;/calendar&gt;
 * </pre>
 *
 * <p>Concrete implementations must provide one method for rendering the calendar:</p>
 *
 * <ul>
 * <li>{@link #render(int, int, int, java.util.Map)}</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class DefaultCalendarViewController implements CalendarViewController
  {
    @RequiredArgsConstructor @ToString
    public static class Entry
      {
        public final int month;
        public final String name;
        public final String link;
        public final Optional<String> type;
      }

    @Nonnull
    private final CalendarView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    protected final RequestLocaleManager requestLocaleManager;

    @Nonnull
    private final CalendarDao dao;

    @Nonnull
    private final TimeProvider timeProvider;

    private int year;

    private int firstYear;

    private int lastYear;

    private final SortedMap<Integer, List<Entry>> entriesByMonth = new TreeMap<>();

    /*******************************************************************************************************************
     *
     * Compute stuff here, to eventually fail fast.
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareRendering (final @Nonnull RenderContext context)
      throws HttpStatusException
      {
        final int requestedYear = getRequestedYear(context.getPathParams(siteNode));
        final ResourceProperties siteNodeProperties = siteNode.getProperties();
        final ResourceProperties viewProperties = getViewProperties();

        year      = viewProperties.getProperty(P_SELECTED_YEAR).orElse(requestedYear);
        firstYear = viewProperties.getProperty(P_FIRST_YEAR).orElse(Math.min(year, requestedYear));
        lastYear  = viewProperties.getProperty(P_LAST_YEAR).orElse(getCurrentYear());
        log.info("prepareRendering() - {} f: {} l: {} r: {} y: {}", siteNode, firstYear, lastYear, requestedYear, year);

        if ((year < firstYear) || (year > lastYear))
          {
            throw new HttpStatusException(SC_NOT_FOUND);
          }

        entriesByMonth.putAll(siteNodeProperties.getProperty(P_ENTRIES).map(e -> findEntriesForYear(e, year))
                                                                       .orElse(emptyMap()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      {
        render(siteNode.getProperty(P_TITLE), year, firstYear, lastYear, entriesByMonth, getViewProperties().getProperty(P_COLUMNS).orElse(4));
      }

    /*******************************************************************************************************************
     *
     * Renders the diary.
     *
     * @param       title       a title for the page (optional)
     * @param       year        the current year
     * @param       firstYear   the first available year
     * @param       lastYear    the last available year
     * @param       byMonth     a map of entries for the current year indexed by month
     * @param       columns     the number of columns of the table to render
     *
     ******************************************************************************************************************/
    protected abstract void render (final @Nonnull Optional<String> title,
                                    final @Nonnegative int year,
                                    final @Nonnegative int firstYear,
                                    final @Nonnegative int lastYear,
                                    final @Nonnull SortedMap<Integer, List<Entry>> byMonth,
                                    final int columns);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final ResourceProperties getViewProperties()
      {
        return siteNode.getPropertyGroup(view.getId());
      }

    /*******************************************************************************************************************
     *
     * Creates a link for the current year.
     *
     * @param       year        the year
     * @return                  the link
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final String createYearLink (final int year)
      {
        return siteNode.getSite().createLink(siteNode.getRelativeUri().appendedWith(Integer.toString(year)));
      }

    /*******************************************************************************************************************
     *
     * Retrieves a map of entries for the given year, indexed by month.
     *
     * @param       entries         the configuration data
     * @param       year            the year
     * @return                      the map
     *
     ******************************************************************************************************************/
    @Nonnull
    private Map<Integer, List<Entry>> findEntriesForYear (final @Nonnull String entries, final @Nonnegative int year)
      {
        return IntStream.rangeClosed(1, 12).boxed()
                .flatMap(month -> dao.findMonthlyEntries(siteNode.getSite(), entries, month, year).stream())
                .collect(groupingBy(e -> e.month));
      }

    /*******************************************************************************************************************
     *
     * Returns the current year reading it from the path params, or by default from the calendar.
     *
     ******************************************************************************************************************/
    @Nonnegative
    private int getRequestedYear (final @Nonnull ResourcePath pathParams)
      throws HttpStatusException
      {
        if (pathParams.getSegmentCount() > 1)
          {
            throw new HttpStatusException(SC_BAD_REQUEST);
          }

        try
          {
            return pathParams.isEmpty() ? getCurrentYear() : Integer.parseInt(pathParams.getLeading());
          }
        catch (NumberFormatException e)
          {
            throw new HttpStatusException(SC_BAD_REQUEST);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnegative
    private int getCurrentYear()
      {
        return ZonedDateTime.ofInstant(timeProvider.get(), ZoneId.of("UTC")).getYear();
      }
  }
