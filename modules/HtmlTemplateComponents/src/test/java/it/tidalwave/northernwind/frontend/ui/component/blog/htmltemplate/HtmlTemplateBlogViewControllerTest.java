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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.util.LocalizedDateTimeFormatters;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.*;
import it.tidalwave.northernwind.frontend.ui.component.blog.MockPosts;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate.HtmlTemplateBlogViewController.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.split;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateBlogViewControllerTest
  {
    private HtmlTemplateBlogViewController underTest;

    private HtmlTemplateBlogView view;

    private SiteNode node;

    private Site site;

    private RequestLocaleManager requestLocaleManager;

    private ResourceProperties nodeProperties;

    private Id viewId = new Id("viewId");

    private final Path actualResults = Paths.get("target/test-results");

    private final Path expectedResults = Paths.get("src/test/resources/expected-results");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        site = createMockSite();

        final SiteFinder<Content> finder = mock(SiteFinder.class);
        when(finder.optionalResult()).thenReturn(Optional.empty());
        when(finder.withRelativePath(any(String.class))).thenReturn(finder);
        when(site.find(eq(Content.class))).thenReturn(finder);

        view = new HtmlTemplateBlogView(viewId, site);

        nodeProperties = createMockProperties();
        node = createMockSiteNode(site);
        when(node.getProperties()).thenReturn(nodeProperties);
        when(node.getRelativeUri()).thenReturn(new ResourcePath("blog"));
        when(site.createLink(any(ResourcePath.class))).then(a -> "http://acme.com" + a.getArgument(0).toString());

        mockViewProperties(viewId, P_TEMPLATE_POSTS_PATH, Optional.empty());
        mockViewProperties(viewId, P_TEMPLATE_TAG_CLOUD_PATH, Optional.empty());

        requestLocaleManager = mock(RequestLocaleManager.class);
        underTest = new HtmlTemplateBlogViewController(node, view, requestLocaleManager);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_posts()
      throws Exception
      {
        // given
        // default of RequestLocaleManager
        final Locale locale = Locale.UK;
        final DateTimeFormatter dtf = LocalizedDateTimeFormatters.getDateTimeFormatterFor(FormatStyle.FULL, locale)
                                                                 .withZone(ZoneId.of(DEFAULT_TIMEZONE));
        when(requestLocaleManager.getLocales()).thenReturn(Arrays.asList(locale));
        when(requestLocaleManager.getDateTimeFormatter()).thenReturn(dtf);
        mockViewProperties(viewId, P_DATE_FORMAT, Optional.of("F-"));
        mockViewProperties(viewId, P_TIME_ZONE, Optional.of("GMT"));

        final MockPosts mockPosts = new MockPosts(site, null);
        mockPosts.createMockData(43);
        final List<List<Content>> posts = split(mockPosts.getPosts(), 0, 3, 5, 7);
        // when
        underTest.renderPosts(posts.get(0), posts.get(1), posts.get(2));
        // then
        assertFileContents("blog.html");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_tag_cloud()
      throws Exception
      {
        // given
        final Random rnd = new Random(17);
        final List<TagAndCount> tacs = IntStream.range(1, 10)
                                                .mapToObj(i -> new TagAndCount("tag" + i, rnd.nextInt(100), "" + rnd.nextInt(10)))
                                                .collect(toList());
        // when
        underTest.renderTagCloud(tacs);
        // then
        assertFileContents("tag_cloud.html");
      }

//    /*******************************************************************************************************************
//     *
//     ******************************************************************************************************************/
//    @Test(dataProvider="mainTitleTestDataProvider")
//    public void must_properly_render_the_main_title (final @Nonnull String viewId,
//                                                     final @Nonnull String title,
//                                                     final @Nonnull String expectedRendering)
//      throws Exception
//      {
//        // given
//        mockNodeProperty(new Id(viewId), P_TITLE, Optional.of(title));
//        // when
//        final StringBuilder builder = new StringBuilder();
//        underTest.renderTitle(builder);
//        // then
//        assertThat(builder.toString(), is(expectedRendering));
//      }

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

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void mockViewProperties (final @Nonnull Id viewId,
                                     final @Nonnull Key<String> propertyKey,
                                     final @Nonnull Optional<String> propertyValue)
      throws Exception
      {
//        when(view.getId()).thenReturn(viewId);
        ResourceProperties properties = node.getPropertyGroup(viewId);

        if (properties == null) // not mocked yet
          {
            properties = createMockProperties();
            when(node.getPropertyGroup(eq(viewId))).thenReturn(properties);
          }

        when(properties.getProperty(eq(propertyKey))).thenReturn(propertyValue);
      }
  }
