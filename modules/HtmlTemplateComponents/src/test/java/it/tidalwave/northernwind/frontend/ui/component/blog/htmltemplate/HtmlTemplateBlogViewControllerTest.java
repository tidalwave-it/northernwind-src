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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import it.tidalwave.util.LocalizedDateTimeFormatters;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
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
import static it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate.HtmlTemplateBlogViewController.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.is;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
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
                                               final @Nonnull Optional<String> timeZone,
                                               final @Nonnull String expectedDTRendering)
      throws Exception
      {
        // given
        final Locale locale = new Locale(localeCode, localeCode);
        final Id viewId = new Id("id");
        // default of RequestLocaleManager
        final DateTimeFormatter dtf = LocalizedDateTimeFormatters.getDateTimeFormatterFor(FormatStyle.FULL, locale)
                                                                 .withZone(ZoneId.of(DEFAULT_TIMEZONE));
        when(requestLocaleManager.getLocales()).thenReturn(Arrays.asList(locale));
        when(requestLocaleManager.getDateTimeFormatter()).thenReturn(dtf);
        mockNodeProperty(viewId, PROPERTY_DATE_FORMAT, dateFormat);
        mockNodeProperty(viewId, PROPERTY_TIME_ZONE, timeZone);
        // when
        final StringBuilder builder = new StringBuilder();
        underTest.renderDate(builder, dateTime);
        // then
        final String expectedValue = String.format("<span class='nw-publishDate'>%s</span>\n", expectedDTRendering);
        assertThat(builder.toString(), is(expectedValue));
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
        ResourceProperties properties = node.getPropertyGroup(viewId);

        if (properties == null) // not mocked yet
          {
            properties = mock(ResourceProperties.class);
            when(node.getPropertyGroup(eq(viewId))).thenReturn(properties);
          }

        when(properties.getProperty(eq(propertyKey))).thenReturn(propertyValue);
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

        final Optional<String> noPattern   = Optional.empty();
        final Optional<String> pattern     = Optional.of("EEEEEEEEEE, MMMMMM d, yyyy");
        final Optional<String> shortStyle  = Optional.of("S-");
        final Optional<String> mediumStyle = Optional.of("M-");
        final Optional<String> longStyle   = Optional.of("L-");
        final Optional<String> fullStyle   = Optional.of("F-");

        final Optional<String> tzNone      = Optional.empty();
        final Optional<String> tzGMT       = Optional.of("GMT");
        final Optional<String> tzCET       = Optional.of("CET");
        final Optional<String> tzPDT       = Optional.of("America/Los_Angeles");
        final Optional<String> tzGMT10     = Optional.of("GMT+10");

        return new Object[][]
          {
           // loc.  format         value   timezone    expected value
            { "en", noPattern,     dt,     tzNone,     "Tuesday, August 7, 2012 5:31:03 PM CEST"},
            { "en", noPattern,     dt,     tzGMT,      "Tuesday, August 7, 2012 3:31:03 PM GMT"},
            { "en", noPattern,     dt,     tzCET,      "Tuesday, August 7, 2012 5:31:03 PM CEST"},
            { "en", noPattern,     dt,     tzPDT,      "Tuesday, August 7, 2012 8:31:03 AM PDT"},
            { "en", noPattern,     dt,     tzGMT10,    "Wednesday, August 8, 2012 1:31:03 AM GMT+10:00"},

            { "it", noPattern,     dt,     tzNone,     "marted\u00ec 7 agosto 2012 17:31:03 CEST"},
            { "it", noPattern,     dt,     tzGMT,      "marted\u00ec 7 agosto 2012 15:31:03 GMT"},
            { "it", noPattern,     dt,     tzCET,      "marted\u00ec 7 agosto 2012 17:31:03 CEST"},
            { "it", noPattern,     dt,     tzPDT,      "marted\u00ec 7 agosto 2012 8:31:03 PDT"},
            { "it", noPattern,     dt,     tzGMT10,    "mercoled\u00ec 8 agosto 2012 1:31:03 GMT+10:00"},


            { "en", pattern,       dt,     tzNone,     "Tuesday, August 7, 2012"},
            { "en", pattern,       dt,     tzGMT,      "Tuesday, August 7, 2012"},
            { "en", pattern,       dt,     tzCET,      "Tuesday, August 7, 2012"},
            { "en", pattern,       dt,     tzPDT,      "Tuesday, August 7, 2012"},
            { "en", pattern,       dt,     tzGMT10,    "Wednesday, August 8, 2012"},

            { "it", pattern,       dt,     tzNone,     "marted\u00ec, agosto 7, 2012"}, // FIXME: should be '7 agosto'
            { "it", pattern,       dt,     tzGMT,      "marted\u00ec, agosto 7, 2012"},
            { "it", pattern,       dt,     tzCET,      "marted\u00ec, agosto 7, 2012"},
            { "it", pattern,       dt,     tzPDT,      "marted\u00ec, agosto 7, 2012"},
            { "it", pattern,       dt,     tzGMT10,    "mercoled\u00ec, agosto 8, 2012"},


            { "en", shortStyle,    dt,     tzNone,     "8/7/12 5:31 PM"},
            { "en", shortStyle,    dt,     tzGMT,      "8/7/12 3:31 PM"},
            { "en", shortStyle,    dt,     tzCET,      "8/7/12 5:31 PM"},
            { "en", shortStyle,    dt,     tzPDT,      "8/7/12 8:31 AM"},
            { "en", shortStyle,    dt,     tzGMT10,    "8/8/12 1:31 AM"},

            { "it", shortStyle,    dt,     tzNone,     "07/08/12 17:31"},
            { "it", shortStyle,    dt,     tzGMT,      "07/08/12 15:31"},
            { "it", shortStyle,    dt,     tzCET,      "07/08/12 17:31"},
            { "it", shortStyle,    dt,     tzPDT,      "07/08/12 8:31"},
            { "it", shortStyle,    dt,     tzGMT10,    "08/08/12 1:31"},


            { "en", mediumStyle,   dt,     tzNone,     "Aug 7, 2012 5:31 PM"},
            { "en", mediumStyle,   dt,     tzGMT,      "Aug 7, 2012 3:31 PM"},
            { "en", mediumStyle,   dt,     tzCET,      "Aug 7, 2012 5:31 PM"},
            { "en", mediumStyle,   dt,     tzPDT,      "Aug 7, 2012 8:31 AM"},
            { "en", mediumStyle,   dt,     tzGMT10,    "Aug 8, 2012 1:31 AM"},

            { "it", mediumStyle,   dt,     tzNone,     "7-ago-2012 17:31"},
            { "it", mediumStyle,   dt,     tzGMT,      "7-ago-2012 15:31"},
            { "it", mediumStyle,   dt,     tzCET,      "7-ago-2012 17:31"},
            { "it", mediumStyle,   dt,     tzPDT,      "7-ago-2012 8:31"},
            { "it", mediumStyle,   dt,     tzGMT10,    "8-ago-2012 1:31"},


            { "en", longStyle,     dt,     tzNone,     "August 7, 2012 5:31:03 PM"},
            { "en", longStyle,     dt,     tzGMT,      "August 7, 2012 3:31:03 PM"},
            { "en", longStyle,     dt,     tzCET,      "August 7, 2012 5:31:03 PM"},
            { "en", longStyle,     dt,     tzPDT,      "August 7, 2012 8:31:03 AM"},
            { "en", longStyle,     dt,     tzGMT10,    "August 8, 2012 1:31:03 AM"},

            { "it", longStyle,     dt,     tzNone,     "7 agosto 2012 17:31:03"},
            { "it", longStyle,     dt,     tzGMT,      "7 agosto 2012 15:31:03"},
            { "it", longStyle,     dt,     tzCET,      "7 agosto 2012 17:31:03"},
            { "it", longStyle,     dt,     tzPDT,      "7 agosto 2012 8:31:03"},
            { "it", longStyle,     dt,     tzGMT10,    "8 agosto 2012 1:31:03"},


            { "en", fullStyle,     dt,     tzNone,     "Tuesday, August 7, 2012 5:31:03 PM CEST"},
            { "en", fullStyle,     dt,     tzGMT,      "Tuesday, August 7, 2012 3:31:03 PM GMT"},
            { "en", fullStyle,     dt,     tzCET,      "Tuesday, August 7, 2012 5:31:03 PM CEST"},
            { "en", fullStyle,     dt,     tzPDT,      "Tuesday, August 7, 2012 8:31:03 AM PDT"},
            { "en", fullStyle,     dt,     tzGMT10,    "Wednesday, August 8, 2012 1:31:03 AM GMT+10:00"},

            { "it", fullStyle,     dt,     tzNone,     "marted\u00ec 7 agosto 2012 17:31:03 CEST"},
            { "it", fullStyle,     dt,     tzGMT,      "marted\u00ec 7 agosto 2012 15:31:03 GMT"},
            { "it", fullStyle,     dt,     tzCET,      "marted\u00ec 7 agosto 2012 17:31:03 CEST"},
            { "it", fullStyle,     dt,     tzPDT,      "marted\u00ec 7 agosto 2012 8:31:03 PDT"},
            { "it", fullStyle,     dt,     tzGMT10,    "mercoled\u00ec 8 agosto 2012 1:31:03 GMT+10:00"},
          };
      }
  }
