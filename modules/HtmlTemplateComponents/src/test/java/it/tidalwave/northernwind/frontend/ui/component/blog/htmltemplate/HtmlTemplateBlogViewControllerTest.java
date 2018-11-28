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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import it.tidalwave.util.LocalizedDateTimeFormatters;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateBlogViewControllerTest
  {
    private HtmlTemplateBlogViewController underTest;

    private BlogView view;

    private SiteNode node;

    private Site site;

    private RequestHolder requestHolder;

    private RequestContext requestContext;

    private RequestLocaleManager requestLocaleManager;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        view = mock(BlogView.class);
        node = mock(SiteNode.class);
        site = mock(Site.class);
        requestHolder = mock(RequestHolder.class);
        requestContext = mock(RequestContext.class);
        requestLocaleManager = mock(RequestLocaleManager.class);
        underTest = new HtmlTemplateBlogViewController(view, node, site, requestHolder, requestContext, requestLocaleManager);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider="mainTitleTestDataProvider")
    public void must_properly_render_the_main_title (final @Nonnull String viewId,
                                                     final @Nonnull String title,
                                                     final @Nonnull String expectedRendering)
      throws Exception
      {
        // given
        mockNodeProperty(new Id(viewId), PROPERTY_TITLE, Optional.of(title));
        // when
        final StringBuilder builder = new StringBuilder();
        underTest.renderMainTitle(builder);
        // then
        assertThat(builder.toString(), is(expectedRendering));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider="dateTestDataProvider")
    public void must_properly_render_the_date (final @Nonnull String localeCode,
                                               final @Nonnull Optional<String> dateFormat,
                                               final @Nonnull ZonedDateTime dateTime,
                                               final @Nonnull String expectedRendering)
      throws Exception
      {
        // given
        final Locale locale = new Locale(localeCode, localeCode);
        final Id viewId = new Id("id");
        // default of RequestLocaleManager
        final DateTimeFormatter dtf = LocalizedDateTimeFormatters.getDateTimeFormatterFor(FormatStyle.FULL, locale)
                                                                 .withZone(ZoneId.of("CET"));
        log.info(">>>> locale is {} - {}", locale, System.identityHashCode(dtf));
        when(requestLocaleManager.getLocales()).thenReturn(Arrays.asList(locale));
        when(requestLocaleManager.getDateTimeFormatter()).thenReturn(dtf);
        mockNodeProperty(viewId, PROPERTY_DATE_FORMAT, dateFormat);
        // when
        final StringBuilder builder = new StringBuilder();
        underTest.renderDate(builder, dateTime);
        // then
        assertThat(builder.toString(), is(expectedRendering));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void mockNodeProperty (final @Nonnull Id viewId,
                                   final @Nonnull Key<String> propertyKey,
                                   final @Nonnull Optional<String> propertyValue)
      throws Exception
      {
        when(view.getId()).thenReturn(viewId);
        final ResourceProperties properties = mock(ResourceProperties.class);
        when(node.getPropertyGroup(eq(viewId))).thenReturn(properties);

        if (propertyValue.isPresent())
          {
            when(properties.getProperty(eq(propertyKey))).thenReturn(propertyValue.get());
          }
        else
          {
            when(properties.getProperty(eq(propertyKey))).thenThrow(new NotFoundException());
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] mainTitleTestDataProvider()
      {
        return new Object[][]
          {
            { "id1", "title1",  "<h2>title1</h2>\n"  },
            { "id2", "title 2", "<h2>title 2</h2>\n" },
            { "id3", "",        ""                   },
            { "id4", "  ",      ""                   }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] dateTestDataProvider()
      {
        final ZonedDateTime dt = Instant.ofEpochMilli(1344353463985L).atZone(ZoneId.of("GMT"));

        final String pattern = "EEEEEEEEEE, MMMMMM d, yyyy";

        return new Object[][]
          {
           // loc.  date format           value   expected value
            { "en", Optional.empty(),     dt,     "<span class='nw-publishDate'>Tuesday, August 7, 2012 5:31:03 PM CEST</span>\n"},
            { "it", Optional.empty(),     dt,     "<span class='nw-publishDate'>marted\u00ec 7 agosto 2012 17:31:03 CEST</span>\n"},

            { "en", Optional.of(pattern), dt,     "<span class='nw-publishDate'>Tuesday, August 7, 2012</span>\n"},
            { "it", Optional.of(pattern), dt,     "<span class='nw-publishDate'>marted\u00ec, agosto 7, 2012</span>\n"},

            { "en", Optional.of("S-"),    dt,     "<span class='nw-publishDate'>8/7/12 5:31 PM</span>\n"},
            { "it", Optional.of("S-"),    dt,     "<span class='nw-publishDate'>07/08/12 17:31</span>\n"},

            { "en", Optional.of("M-"),    dt,     "<span class='nw-publishDate'>Aug 7, 2012 5:31 PM</span>\n"},
            { "it", Optional.of("M-"),    dt,     "<span class='nw-publishDate'>7-ago-2012 17:31</span>\n"},

            { "en", Optional.of("L-"),    dt,     "<span class='nw-publishDate'>August 7, 2012 5:31:03 PM</span>\n"},
            { "it", Optional.of("L-"),    dt,     "<span class='nw-publishDate'>7 agosto 2012 17:31:03</span>\n"},

            { "en", Optional.of("F-"),    dt,     "<span class='nw-publishDate'>Tuesday, August 7, 2012 5:31:03 PM CEST</span>\n"},
            { "it", Optional.of("F-"),    dt,     "<span class='nw-publishDate'>marted\u00ec 7 agosto 2012 17:31:03 CEST</span>\n"},
          };
      }
  }
