/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.io.IOException;
import java.io.StringReader;
import org.joda.time.DateTime;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
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

    @Nonnull
    private final RequestHolder requestHolder;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      throws NotFoundException,
             IOException,
             ParserConfigurationException,
             SAXException,
             XPathExpressionException,
             HttpStatusException
      {
        final String pathParams = requestHolder.get().getPathParams(siteNode);
        final int currentYear = getCurrentYear(pathParams);

        final ResourceProperties siteNodeProperties = siteNode.getProperties();
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());

//        try
//          {
//            siteNodeProperties.getProperty(PROPERTY_ENTRIES);
//          }
//        catch (NotFoundException e)
//          {
//            throw new HttpStatusException(404);
//          }

        final String entries = siteNodeProperties.getProperty(PROPERTY_ENTRIES);
        final StringBuilder builder = new StringBuilder();
        final int selectedYear = viewProperties.getIntProperty(PROPERTY_SELECTED_YEAR, currentYear);
        final int firstYear = viewProperties.getIntProperty(PROPERTY_FIRST_YEAR, Math.min(selectedYear, currentYear));
        final int lastYear = viewProperties.getIntProperty(PROPERTY_LAST_YEAR, Math.max(selectedYear, currentYear));
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
                final String link = site.createLink(new ResourcePath(node.getAttributes().getNamedItem("link").getNodeValue()));
                
                String linkClass = "";
                Node typeNode = node.getAttributes().getNamedItem("type");
                
                if (typeNode != null)
                  {
                    linkClass = String.format(" class='nw-calendar-table-link-%s'", typeNode.getNodeValue()); 
                  }
                
                final String name = node.getAttributes().getNamedItem("name").getNodeValue();
                builder.append(String.format("<li><a href='%s'%s/>%s</a></li>%n", link, linkClass, name));
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
      throws IOException
      {
        try
          {
            builder.append(String.format("<h2>%s</h2>%n", siteNodeProperties.getProperty(PROPERTY_TITLE)));
          }
        catch (NotFoundException e)
          {
            // ok, no title
          }
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
    private int getCurrentYear (final @Nonnull String pathParams)
      throws HttpStatusException
      {
        try
          {
            return "".equals(pathParams) ? new DateTime().getYear()
                                         : Integer.parseInt(pathParams.replaceAll("/$", "").replaceAll("^/", ""));
//            return "".equals(pathParams) ? new DateTime().getYear() : Integer.parseInt(pathParams.replaceAll("/", ""));
          }
        catch (NumberFormatException e)
          {
            throw new HttpStatusException(404);
          }
      }
  }
