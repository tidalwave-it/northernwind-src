/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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

import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.ViewController;

/***********************************************************************************************************************
 *
 * A controller for rendering a calendar.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface CalendarViewController extends ViewController
  {
    /** The list of entries of the calendar. */
    public static final Key<String> P_ENTRIES = Key.of("entries", String.class);

    /** The selected year of the calendar. */
    public static final Key<Integer> P_SELECTED_YEAR = Key.of("selectedYear", Integer.class);

    /** The first year in the year selector of the calendar. */
    public static final Key<Integer> P_FIRST_YEAR = Key.of("firstYear", Integer.class);

    /** The last year in the year selector of the calendar. */
    public static final Key<Integer> P_LAST_YEAR = Key.of("lastYear", Integer.class);

    /** The number of columns of the table with the calendar. */
    public static final Key<Integer> P_COLUMNS = Key.of("columns", Integer.class);
  }
