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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;
import java.nio.file.Files;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate.HtmlTemplateCalendarView;
import it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate.HtmlTemplateCalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.CalendarDao;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.XmlCalendarDao;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarViewController.*;
import static org.mockito.Mockito.*;
import org.testng.annotations.DataProvider;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateCalendarViewControllerTest
  {
    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    private DefaultCalendarViewController underTest;

    private HtmlTemplateCalendarView view;

    private RenderContext context;

    private final Id viewId = new Id("viewId");

    private ResourceProperties viewProperties;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        final var site = createMockSite();

        view = new HtmlTemplateCalendarView(viewId, site); // this is an integration test

        final var siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("diary"));
        final var siteNodeProperties = createMockProperties();

        final var path = fileTestHelper.resolve("entries.xml");
        final var entries = Files.readString(path);
        when(siteNodeProperties.getProperty(eq(P_ENTRIES))).thenReturn(Optional.of(entries));

        viewProperties = createMockProperties();

        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        final var requestLocaleManager = mock(RequestLocaleManager.class);
        when(requestLocaleManager.getLocales()).thenReturn(List.of(Locale.ENGLISH));

        final var request = mock(Request.class);
        final var requestContext = mock(RequestContext.class);
        context = new DefaultRenderContext(request, requestContext);

        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.EMPTY);

        final var mockTime = Instant.ofEpochSecond(1502150400); // 2017/08/08
        final CalendarDao dao = new XmlCalendarDao();

        underTest = new HtmlTemplateCalendarViewController(view, siteNode, requestLocaleManager, dao, () -> mockTime);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "columnProvider")
    public void must_properly_render (final int columns)
      throws Exception
      {
        // given
        when(viewProperties.getProperty(eq(P_FIRST_YEAR))).thenReturn(Optional.of(2000));
        when(viewProperties.getProperty(eq(P_LAST_YEAR))).thenReturn(Optional.of(2018));
        when(viewProperties.getProperty(eq(P_SELECTED_YEAR))).thenReturn(Optional.of(2013));
        when(viewProperties.getProperty(eq(P_COLUMNS))).thenReturn(Optional.of(columns));
        underTest.prepareRendering(context);
        // when
        underTest.renderView(context);
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), String.format("calendar-%dx%d.xhtml", columns, 12 / columns));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] columnProvider()
      {
        return new Object[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 6 }, { 12 } };
      }
  }
