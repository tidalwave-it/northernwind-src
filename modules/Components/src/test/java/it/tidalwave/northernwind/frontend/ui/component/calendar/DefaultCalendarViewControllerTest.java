/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.IntStream;
import java.time.Instant;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import it.tidalwave.util.Id;
import it.tidalwave.util.InstantProvider;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.CalendarDao;
import it.tidalwave.northernwind.frontend.ui.component.calendar.spi.XmlCalendarDao;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultCalendarViewControllerTest
  {
    class UnderTest extends DefaultCalendarViewController
      {
        public Map<Integer, List<Entry>> byMonth;

        public UnderTest (final CalendarView view,
                          final SiteNode siteNode,
                          final RequestLocaleManager requestLocaleManager,
                          final CalendarDao dao,
                          final InstantProvider instantProvider)
          {
            super(view, siteNode, requestLocaleManager, dao, instantProvider);
          }

        @Override
        protected void render (final Optional<String> title,
                               final int year,
                               final int firstYear,
                               final int lastYear,
                               final SortedMap<Integer, List<Entry>> byMonth,
                               final int columns)
          {
            this.byMonth = byMonth;
          }
      }

    private UnderTest underTest;

    private CalendarView view;

    private SiteNode siteNode;

    private Site site;

    private RequestLocaleManager requestLocaleManager;

    private Request request;

    private RenderContext context;

    private Id viewId = new Id("viewId");

    private ResourceProperties viewProperties;

    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        site = createMockSite();

        view = mock(CalendarView.class);
        when(view.getId()).thenReturn(viewId);

        siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("diary"));
        final ResourceProperties siteNodeProperties = createMockProperties();

        final Path path = fileTestHelper.resolve("entries.xml");
        final String entries = new String(Files.readAllBytes(path), UTF_8);
        when(siteNodeProperties.getProperty(eq(P_ENTRIES))).thenReturn(Optional.of(entries));

        viewProperties = createMockProperties();

        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        requestLocaleManager = mock(RequestLocaleManager.class);
        when(requestLocaleManager.getLocales()).thenReturn(asList(Locale.ENGLISH));

        request = mock(Request.class);
        final RequestContext requestContext = mock(RequestContext.class);
        context = new DefaultRenderContext(request, requestContext);

        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.EMPTY);

        final Instant mockTime = Instant.ofEpochSecond(1502150400); // 2017/08/08
        final CalendarDao dao = new XmlCalendarDao();

        underTest = new UnderTest(view, siteNode, requestLocaleManager, dao, () -> mockTime);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "years")
    public void must_properly_render (final int firstYear, final int lastYear, final int selectedYear)
      throws Exception
      {
        // given
        when(viewProperties.getProperty(eq(P_FIRST_YEAR))).thenReturn(Optional.of(firstYear));
        when(viewProperties.getProperty(eq(P_LAST_YEAR))).thenReturn(Optional.of(lastYear));
        when(viewProperties.getProperty(eq(P_SELECTED_YEAR))).thenReturn(Optional.of(selectedYear));
        underTest.prepareRendering(context);
        // when
        underTest.renderView(context);
        // then
        final String fileName = String.format("diary-%d-%d_%d.txt", firstYear, lastYear, selectedYear);
        assertFileContents(fileName);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "renderingTestDataNotFound",
          expectedExceptions = HttpStatusException.class,
          expectedExceptionsMessageRegExp = ".*httpStatus=404.*")
    public void must_throw_NotFound (final int firstYear,
                                     final int lastYear,
                                     final Optional<Integer> selectedYear,
                                     final String pathParams)
      throws Exception
      {
        // given
        when(viewProperties.getProperty(eq(P_FIRST_YEAR))).thenReturn(Optional.of(firstYear));
        when(viewProperties.getProperty(eq(P_LAST_YEAR))).thenReturn(Optional.of(lastYear));
        when(viewProperties.getProperty(eq(P_SELECTED_YEAR))).thenReturn(selectedYear);
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        underTest.prepareRendering(context);
        // when
        underTest.renderView(context);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "renderingTestDataBadRequest",
          expectedExceptions = HttpStatusException.class,
          expectedExceptionsMessageRegExp = ".*httpStatus=400.*")
    public void must_throw_BadRequest (final int firstYear,
                                       final int lastYear,
                                       final Optional<Integer> selectedYear,
                                       final String pathParams)
      throws Exception
      {
        // given
        when(viewProperties.getProperty(eq(P_FIRST_YEAR))).thenReturn(Optional.of(firstYear));
        when(viewProperties.getProperty(eq(P_LAST_YEAR))).thenReturn(Optional.of(lastYear));
        when(viewProperties.getProperty(eq(P_SELECTED_YEAR))).thenReturn(selectedYear);
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        underTest.prepareRendering(context);
        // when
        underTest.renderView(context);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertFileContents (final @Nonnull String fileName)
      throws IOException
      {
        final String s = underTest.byMonth.entrySet().stream().flatMap(e ->
                e.getValue().stream().map(i -> String.format("%2d: %2d %-80s %30s %10s",
                                                             e.getKey(), i.month, i.name, i.link, i.type.orElse(""))))
                            .collect(joining("\n"));

        fileTestHelper.assertFileContents(s.getBytes(UTF_8), fileName);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] years()
      {
        return IntStream.rangeClosed(1999, 2017)
                        .mapToObj(i -> new Object[] { 1999, 2017, i})
                        .collect(toList())
                        .toArray(new Object[0][0]);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] renderingTestDataNotFound()
      {
        return new Object[][]
          {
           // first  last   selected                path params
            { 1999,  2017,  Optional.of(1997),      ""      },
            { 1999,  2017,  Optional.of(1998),      ""      },
            { 1999,  2017,  Optional.of(2018),      ""      },
            { 1999,  2017,  Optional.of(2019),      ""      },
            { 1999,  2017,  Optional.empty(),       "/1997" },
            { 1999,  2017,  Optional.empty(),       "/1998" },
            { 1999,  2017,  Optional.empty(),       "/2018" },
            { 1999,  2017,  Optional.empty(),       "/2019" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] renderingTestDataBadRequest()
      {
        return new Object[][]
          {
           // first  last   selected                path params
            { 1999,  2017,  Optional.empty(),       "/1r94"    },
            { 1999,  2017,  Optional.empty(),       "/2010/2"  }
          };
      }
  }
