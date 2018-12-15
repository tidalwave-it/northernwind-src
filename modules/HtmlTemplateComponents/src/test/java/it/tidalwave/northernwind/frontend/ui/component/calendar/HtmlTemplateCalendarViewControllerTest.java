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

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate.HtmlTemplateCalendarView;
import it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate.HtmlTemplateCalendarViewController;
import it.tidalwave.util.Id;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateCalendarViewControllerTest
  {
    private DefaultCalendarViewController underTest;

    private HtmlTemplateCalendarView view;

    private SiteNode siteNode;

    private Site site;

    private RequestLocaleManager requestLocaleManager;

    private RenderContext context;

    private Id viewId = new Id("viewId");

    private ResourceProperties viewProperties;

    private final Path base = Paths.get("src/test/resources/HtmlTemplateCalendarViewControllerTest");

    private final Path actualResults = Paths.get("target/test-results/HtmlTemplateCalendarViewControllerTest");

    private final Path expectedResults = base.resolve("expected-results");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        site = createMockSite();

        view = new HtmlTemplateCalendarView(viewId, site); // this is an integration test

        siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("diary"));
        final ResourceProperties siteNodeProperties = createMockProperties();

        final Path path = base.resolve("entries.xml");
        final String entries = new String(Files.readAllBytes(path), UTF_8);
        when(siteNodeProperties.getProperty(eq(P_ENTRIES))).thenReturn(Optional.of(entries));

        viewProperties = createMockProperties();

        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        requestLocaleManager = mock(RequestLocaleManager.class);
        when(requestLocaleManager.getLocales()).thenReturn(asList(Locale.ENGLISH));

        final Request request = mock(Request.class);
        final RequestContext requestContext = mock(RequestContext.class);
        context = new DefaultRenderContext(request, requestContext);

        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.EMPTY);

        final Instant mockTime = Instant.ofEpochSecond(1502150400); // 2017/08/08
        final CalendarDao dao = new XmlCalendarDao();

        underTest = new HtmlTemplateCalendarViewController(view, siteNode, requestLocaleManager, dao, () -> mockTime);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(eq(P_FIRST_YEAR))).thenReturn(Optional.of(2000));
        when(viewProperties.getProperty(eq(P_LAST_YEAR))).thenReturn(Optional.of(2018));
        when(viewProperties.getProperty(eq(P_SELECTED_YEAR))).thenReturn(Optional.of(2013));
        underTest.prepareRendering(context);
        // when
        underTest.renderView(context);
        // then
        assertFileContents("diary.xhtml");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertFileContents (final @Nonnull String fileName)
      throws IOException
      {
        final Path actualPath = actualResults.resolve(fileName);
        final Path excpectedPath = expectedResults.resolve(fileName);
        Files.createDirectories(actualResults);
        Files.write(actualPath, view.asBytes(UTF_8));
        assertSameContents(excpectedPath.toFile(), actualPath.toFile());
      }
  }
