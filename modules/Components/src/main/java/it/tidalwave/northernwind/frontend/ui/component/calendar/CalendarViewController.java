/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.calendar;

import it.tidalwave.util.Key;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CalendarViewController
  {
    /** The list of entries of the calendar. */
    public static final Key<String> PROPERTY_ENTRIES = new Key<>("entries");

    /** The selected year of the calendar. */
    public static final Key<String> PROPERTY_SELECTED_YEAR = new Key<>("selectedYear");  // FIXME: Integer

    /** The first year in the year selector of the calendar. */
    public static final Key<String> PROPERTY_FIRST_YEAR = new Key<>("firstYear");  // FIXME: Integer

    /** The last year in the year selector of the calendar. */
    public static final Key<String> PROPERTY_LAST_YEAR = new Key<>("lastYear");  // FIXME: Integer
  }
