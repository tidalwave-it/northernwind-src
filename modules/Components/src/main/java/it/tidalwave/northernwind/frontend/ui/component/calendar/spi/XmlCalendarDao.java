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
import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController.Entry;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class XmlCalendarDao implements CalendarDao
  {
    private static final String[] SHORT_MONTH_NAMES = DateFormatSymbols.getInstance(Locale.ENGLISH).getShortMonths();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Entry> findMonthlyEntries (@Nonnull final Site site,
                                           @Nonnull final String entries,
                                           @Nonnegative final int month,
                                           @Nonnegative final int year)
      {
        try
          {
            // TODO: have them injected
            final var dbf = DocumentBuilderFactory.newInstance();
            final var db = dbf.newDocumentBuilder();
            final var document = db.parse(new InputSource(new StringReader(entries)));
            final var xPathFactory = XPathFactory.newInstance();
            final var xPath = xPathFactory.newXPath();

            final var queryTemplate = "/calendar/year[@id='%d']/month[@id='%s']/item";
            final var queryString = String.format(queryTemplate, year, SHORT_MONTH_NAMES[month - 1].toLowerCase());
            final var query = xPath.compile(queryString);
            final var nodes = (NodeList)query.evaluate(document, XPathConstants.NODESET);
            return IntStream.range(0, nodes.getLength()).mapToObj(i -> toEntry(site, nodes.item(i), month)).collect(toList());
          }
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Entry toEntry (@Nonnull final Site site, @Nonnull final Node node, final int month)
      {
        final var uri = node.getAttributes().getNamedItem("link").getNodeValue();
        final var type = Optional.ofNullable(node.getAttributes().getNamedItem("type")).map(Node::getNodeValue);
        final var link = site.createLink(ResourcePath.of(uri));
        final var name = node.getAttributes().getNamedItem("name").getNodeValue();

        return new DefaultCalendarViewController.Entry(month, name, link, type);
      }
  }
