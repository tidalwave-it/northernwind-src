/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.calendar;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.text.DateFormatSymbols;
import java.io.IOException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class DefaultCalendarViewController implements CalendarViewController
  {
    @Nonnull
    private final CalendarView view;
    
    @Nonnull
    private final SiteNode siteNode;
    
    @Inject @Nonnull
    private Site site;

    @Inject @Nonnull
    private RequestLocaleManager requestLocaleManager;

    public DefaultCalendarViewController (final @Nonnull CalendarView view, final @Nonnull SiteNode siteNode) 
      {
        this.view = view;
        this.siteNode = siteNode;
      }
    
    @PostConstruct
    /* package */ void initialize()
      throws NotFoundException, IOException
      {
        final ResourceProperties properties = siteNode.getPropertyGroup(view.getId());
        final StringBuilder builder = new StringBuilder();
        final int currentYear = new DateTime().getYear();
        final int selectedYear = Integer.parseInt(properties.getProperty(SELECTED_YEAR, "" + currentYear));
        final int firstYear = Integer.parseInt(properties.getProperty(FIRST_YEAR, "" + Math.min(selectedYear, currentYear)));
        final int lastYear = Integer.parseInt(properties.getProperty(LAST_YEAR, "" + Math.max(selectedYear, currentYear)));
        final int columns = 4;

        // FIXME: style and border, cellpadding etc... must go in to the CSS
        builder.append("<table style='text-align: left; width: 100%;' class='nw-calendar' border='1' cellpadding='2' cellspacing='0'>\n")
               .append("<tbody>\n");

        builder.append(String.format("<tr>\n<th colspan='%d' style='font-size:150%%'>%d</th>\n</tr>\n", columns, selectedYear));

        final String[] monthNames = DateFormatSymbols.getInstance(requestLocaleManager.getLocales().get(0)).getMonths();
               
        for (int month = 1; month <= 12; month++)
          {
            if ((month - 1) % columns == 0)
              {
                builder.append("<tr>\n");
                
                for (int column = 0; column < columns; column++)
                  {
                    builder.append(String.format("<th width='%d%%'>%s</th>", 100 / columns, monthNames[month + column - 1]));
                  }
                
                builder.append("</tr>\n<tr>\n");
              }
            
            builder.append("<td>\n<ul>\n");
//          <x:forEach var="n" select="$doc/calendar/year[@id=$year]/month[@id='jan']/item">
//              <li><a href="${baseURL}<x:out select="$n/@link"/>"><x:out select="$n/@name"/></a></li>
//          </x:forEach></ul>
            builder.append("</ul>\n</td>\n");
            
            if ((month - 1) % columns == (columns - 1))
              {
                builder.append("</tr>\n");
              }
          }

        builder.append("</tbody>\n</table>\n");
        
        builder.append("<div class='nw-calendar-yearselector'>");
        String separator = "";
        
        for (int year = firstYear; year <= lastYear; year++)
          {
            builder.append(separator);
            separator = " | ";
            
            if (year != selectedYear)
              {
                final String url = site.createLink(siteNode.getRelativeUri() + "/" + year);
                // FIXME: style stuff should go to CSS
                builder.append(String.format("<b><a href='%s'>%d</a></b>\n", url, year));
              }
            else
              {
                builder.append(year);
              }
          }
        
        builder.append("</div>");
        view.setContent(builder.toString());
      }
  }
