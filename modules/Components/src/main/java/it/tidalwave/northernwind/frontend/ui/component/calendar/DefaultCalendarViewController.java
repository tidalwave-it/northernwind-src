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
package it.tidalwave.northernwind.frontend.ui.component.calendar;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.time.ZonedDateTime;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static javax.servlet.http.HttpServletResponse.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultCalendarViewController implements CalendarViewController
  {
    @Nonnull
    private final CalendarView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    @Nonnull
    private final RequestLocaleManager requestLocaleManager;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        final int currentYear = getCurrentYear(context.getPathParams(siteNode));
        final ResourceProperties siteNodeProperties = siteNode.getProperties();
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());

//        try
//          {
//            siteNodeProperties.getProperty(P_ENTRIES);
//          }
//        catch (NotFoundException e)
//          {
//            throw new HttpStatusException(SC_NOT_FOUND);
//          }

        final String entries = siteNodeProperties.getProperty(P_ENTRIES).orElse("");
        final StringBuilder builder = new StringBuilder();
        final int selectedYear = viewProperties.getProperty(P_SELECTED_YEAR).orElse(currentYear);
        final int firstYear = viewProperties.getProperty(P_FIRST_YEAR).orElse(Math.min(selectedYear, currentYear));
        final int lastYear = viewProperties.getProperty(P_LAST_YEAR).orElse(Math.max(selectedYear, currentYear));
        final int columns = 4;

        builder.append("<div class='nw-calendar'>\n");
        appendTitle(builder, siteNodeProperties);

        builder.append("<table class='nw-calendar-table'>\n")
               .append("<tbody>\n");

        builder.append(String.format("<tr>%n<th colspan='%d' class='nw-calendar-title'>%d</th>%n</tr>%n", columns, selectedYear));

        final String[] monthNames = DateFormatSymbols.getInstance(requestLocaleManager.getLocales().get(0)).getMonths();
        final String[] shortMonthNames = DateFormatSymbols.getInstance(Locale.ENGLISH).getShortMonths();

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document document = db.parse(new InputSource(new StringReader(entries)));
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        for (int month = 1; month <= 12; month++)
          {
            if ((month - 1) % columns == 0)
              {
                builder.append("<tr>\n");

                for (int column = 0; column < columns; column++)
                  {
                    builder.append(String.format("<th width='%d%%'>%s</th>",
                                                 100 / columns, monthNames[month + column - 1]));
                  }

                builder.append("</tr>\n<tr>\n");
              }

            builder.append("<td>\n<ul>\n");
            final String pathTemplate = "/calendar/year[@id='%d']/month[@id='%s']/item";
            final String jq1 = String.format(pathTemplate, selectedYear, shortMonthNames[month - 1].toLowerCase());
            final XPathExpression jx1 = xPath.compile(jq1);
            final NodeList nodes = (NodeList)jx1.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++)
              {
                // FIXME: verbose XML code below
                final Node node = nodes.item(i);
                final String link = site.createLink(ResourcePath.of(node.getAttributes().getNamedItem("link").getNodeValue()));

                String linkClass = "";
                Node typeNode = node.getAttributes().getNamedItem("type");

                if (typeNode != null)
                  {
                    linkClass = String.format(" class='nw-calendar-table-link-%s'", typeNode.getNodeValue());
                  }

                final String name = node.getAttributes().getNamedItem("name").getNodeValue();
                builder.append(String.format("<li><a href='%s'%s>%s</a></li>%n", link, linkClass, name));
              }

            builder.append("</ul>\n</td>\n");

            if ((month - 1) % columns == (columns - 1))
              {
                builder.append("</tr>\n");
              }
          }

        builder.append("</tbody>\n</table>\n");

        appendYearSelector(builder, firstYear, lastYear, selectedYear);
        builder.append("</div>\n");
        view.setContent(builder.toString());
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void appendTitle (final @Nonnull StringBuilder builder,
                              final @Nonnull ResourceProperties siteNodeProperties)
      {
        siteNodeProperties.getProperty(P_TITLE).ifPresent(title -> builder.append(String.format("<h2>%s</h2>%n", title)));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void appendYearSelector (final @Nonnull StringBuilder builder,
                                     final @Nonnegative int firstYear,
                                     final @Nonnegative int lastYear,
                                     final @Nonnegative int selectedYear)
      {
        builder.append("<div class='nw-calendar-yearselector'>\n");
        String separator = "";

        for (int year = firstYear; year <= lastYear; year++)
          {
            builder.append(separator);
            separator = " | ";

            if (year != selectedYear)
              {
                final String url = site.createLink(siteNode.getRelativeUri().appendedWith("" + year));
                builder.append(String.format("<a href='%s'>%d</a>%n", url, year));
              }
            else
              {
                builder.append(year);
              }
          }

        builder.append("</div>\n");
      }

    /*******************************************************************************************************************
     *
     * Returns the current year reading it from the path params, or by default from the calendar.
     *
     ******************************************************************************************************************/
    @Nonnegative
    private int getCurrentYear (final @Nonnull ResourcePath pathParams)
      throws HttpStatusException
      {
        if (pathParams.getSegmentCount() > 1)
          {
            throw new HttpStatusException(SC_BAD_REQUEST);
          }

        try
          {
            return pathParams.isEmpty() ? ZonedDateTime.now().getYear() : Integer.parseInt(pathParams.getLeading());
          }
        catch (NumberFormatException e)
          {
            throw new HttpStatusException(SC_NOT_FOUND);
          }
      }
  }
