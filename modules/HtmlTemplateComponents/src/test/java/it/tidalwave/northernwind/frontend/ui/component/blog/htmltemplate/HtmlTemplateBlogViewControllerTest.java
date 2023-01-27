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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import it.tidalwave.util.Id;
import it.tidalwave.util.LocalizedDateTimeFormatters;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.blog.MockPosts;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.util.CollectionUtils.split;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate.HtmlTemplateBlogViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateBlogViewControllerTest
  {
    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    private HtmlTemplateBlogViewController underTest;

    private HtmlTemplateBlogView view;

    private SiteNode node;

    private Site site;

    private RequestLocaleManager requestLocaleManager;

    private final Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        site = createMockSite();

        final SiteFinder<Content> finder = createMockSiteFinder();
        when(site.find(eq(Content.class))).thenReturn(finder);

        view = new HtmlTemplateBlogView(viewId, site);

        final var nodeProperties = createMockProperties();
        node = createMockSiteNode(site);
        when(node.getProperties()).thenReturn(nodeProperties);
        when(node.getRelativeUri()).thenReturn(ResourcePath.of("blog"));
        mockViewProperty(node, viewId, P_TEMPLATE_POSTS_PATH, Optional.empty());
        mockViewProperty(node, viewId, P_TEMPLATE_TAG_CLOUD_PATH, Optional.empty());

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
        final var locale = Locale.UK;
        final var dtf = LocalizedDateTimeFormatters.getDateTimeFormatterFor(FormatStyle.FULL, locale)
                                                   .withZone(ZoneId.of(DEFAULT_TIMEZONE));
        when(requestLocaleManager.getLocales()).thenReturn(List.of(locale));
        when(requestLocaleManager.getDateTimeFormatter()).thenReturn(dtf);
        mockViewProperty(node, viewId, P_DATE_FORMAT, Optional.of("F-"));
        mockViewProperty(node, viewId, P_TIME_ZONE, Optional.of("GMT"));

        final var mockPosts = new MockPosts(site, null);
        mockPosts.createMockData(43);
        final var posts = split(mockPosts.getPosts(), 0, 3, 5, 7);
        // when
        underTest.renderPosts(posts.get(0), posts.get(1), posts.get(2));
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), "blog.xhtml");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_tag_cloud()
      throws Exception
      {
        // given
        final var rnd = new Random(17);
        final var tags = IntStream.range(1, 10)
                                  .mapToObj(i -> new TagAndCount("tag" + i, rnd.nextInt(100), "" + rnd.nextInt(10)))
                                  .collect(toList());
        // when
        underTest.renderTagCloud(tags);
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), "tag_cloud.xhtml");
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
  }
