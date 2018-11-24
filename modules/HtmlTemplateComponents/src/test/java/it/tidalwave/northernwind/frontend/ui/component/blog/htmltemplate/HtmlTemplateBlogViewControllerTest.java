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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
        mockNodeProperty(viewId, PROPERTY_TITLE, title);
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
    public void must_properly_render_the_date (final @Nonnull String viewId,
                                               final @Nonnull String localeCode,
                                               final @CheckForNull String dateFormat,
                                               final @Nonnull ZonedDateTime dateTime,
                                               final @Nonnull String expectedRendering)
      throws Exception
      {
        // given
        final Locale locale = new Locale(localeCode);
        // default of RequestLocaleManager
        final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(locale).withZone(ZoneId.of("CET"));
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
    private void mockNodeProperty (final @Nonnull String viewId,
                                   final @Nonnull Key<String> propertyKey,
                                   final @CheckForNull String propertyValue)
      throws Exception
      {
        final Id id = new Id(viewId);
        when(view.getId()).thenReturn(id);
        final ResourceProperties properties = mock(ResourceProperties.class);
        when(node.getPropertyGroup(eq(id))).thenReturn(properties);

        if (propertyValue != null)
          {
            when(properties.getProperty(eq(propertyKey))).thenReturn(propertyValue);
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
        final ZonedDateTime dateTime = Instant.ofEpochMilli(1344353463985L).atZone(ZoneId.of("GMT"));

        return new Object[][]
          {
            { "id1", "en", null,                         dateTime, "<span class='nw-publishDate'>Tuesday, August 7, 2012 5:31:03 PM CEST</span>\n"},
            { "id2", "it", null,                         dateTime, "<span class='nw-publishDate'>marted\u00ec 7 agosto 2012 17.31.03 CEST</span>\n"},
            { "id3", "en", "EEEEEEEEEE, MMMMMM d, yyyy", dateTime, "<span class='nw-publishDate'>Tuesday, August 7, 2012</span>\n"},
            { "id4", "it", "EEEEEEEEEE, MMMMMM d, yyyy", dateTime, "<span class='nw-publishDate'>marted\u00ec, agosto 7, 2012</span>\n"},
            { "id5", "en", "S-",                         dateTime, "<span class='nw-publishDate'>8/7/12</span>\n"},
            { "id6", "it", "S-",                         dateTime, "<span class='nw-publishDate'>07/08/12</span>\n"},
            { "id5", "en", "M-",                         dateTime, "<span class='nw-publishDate'>Aug 7, 2012</span>\n"},
            { "id6", "it", "M-",                         dateTime, "<span class='nw-publishDate'>7-ago-2012</span>\n"},
            { "id5", "en", "L-",                         dateTime, "<span class='nw-publishDate'>August 7, 2012</span>\n"},
            { "id6", "it", "L-",                         dateTime, "<span class='nw-publishDate'>7 agosto 2012</span>\n"},
            { "id5", "en", "F-",                         dateTime, "<span class='nw-publishDate'>Tuesday, August 7, 2012</span>\n"},
            { "id6", "it", "F-",                         dateTime, "<span class='nw-publishDate'>marted\u00ec 7 agosto 2012</span>\n"},
          };
      }
  }
