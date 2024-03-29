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
package it.tidalwave.northernwind.frontend.ui.component.calendar.spi;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController.Entry;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface CalendarDao
  {
    /*******************************************************************************************************************
     *
     * Retrieves a list of entries for the given month and year.
     *
     * @param       site            the site
     * @param       entries         the configuration data
     * @param       month           the month
     * @param       year            the year
     * @return                      the list
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Entry> findMonthlyEntries (@Nonnull Site site,
                                           @Nonnull String entries,
                                           @Nonnegative int month,
                                           @Nonnegative int year);
  }
