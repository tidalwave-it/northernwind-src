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
package it.tidalwave.northernwind.frontend.ui.component.blog;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.ZonedDateTime;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.LocalizedDateTimeFormatters;
import it.tidalwave.role.ContextManager;
import it.tidalwave.role.spi.DefaultContextManagerProvider;
import it.tidalwave.northernwind.core.model.Content;
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
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController.TagAndCount;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.asList;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController.DEFAULT_TIMEZONE;
import static it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerViewController.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultBlogViewControllerTest
  {
    static class UnderTest extends DefaultBlogViewController
      {
        public final List<Content> _fullPosts = new ArrayList<>();

        public final List<Content> _leadInPosts = new ArrayList<>();

        public final List<Content> _linkedPosts = new ArrayList<>();

        public final List<TagAndCount> tagsAndCount = new ArrayList<>();

        public UnderTest (final @Nonnull SiteNode siteNode,
                          final @Nonnull BlogView view,
                          final @Nonnull RequestLocaleManager requestLocaleManager)
          {
            super(siteNode, view, requestLocaleManager);
          }

        @Override
        protected void renderPosts (final @Nonnull List<Content> fullPosts,
                                    final @Nonnull List<Content> leadinPosts,
                                    final @Nonnull List<Content> linkedPosts)
          {
            _fullPosts.addAll(fullPosts);
            _leadInPosts.addAll(leadinPosts);
            _linkedPosts.addAll(linkedPosts);
          }

        @Override
        protected void renderTagCloud (final @Nonnull Collection<TagAndCount> tagsAndCount)
          {
            this.tagsAndCount.addAll(tagsAndCount);
          }

      }

    private static final String SITE_NODE_RELATIVE_URI = "/blogNode";

    private Site site;

    private SiteNode siteNode;

    private BlogView view;

    private UnderTest underTest;

    private ResourceProperties viewProperties;

    private ResourceProperties siteNodeProperties;

    private RenderContext renderContext;

    private Request request;

    private RequestContext requestContext;

    private RequestLocaleManager requestLocaleManager;

    private MockPosts mockPosts;

    private Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        ContextManager.Locator.set(new DefaultContextManagerProvider()); // TODO: try to get rid of this

        site = createMockSite();
        MockSiteNodeSiteFinder.registerTo(site);
        MockContentSiteFinder.registerTo(site);

        viewProperties = createMockProperties();
        siteNodeProperties = createMockProperties();

        siteNode = createMockSiteNode(site);
        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of(SITE_NODE_RELATIVE_URI));

        view = mock(BlogView.class);
        when(view.getId()).thenReturn(viewId);

        request = mock(Request.class);
        requestContext = mock(RequestContext.class);
        renderContext = new DefaultRenderContext(request, requestContext);
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.EMPTY);

        requestLocaleManager = mock(RequestLocaleManager.class);

        mockPosts = new MockPosts(site, viewProperties);

        underTest = new UnderTest(siteNode, view, requestLocaleManager);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "postRenderingTestData")
    public void must_properly_render_posts (final int seed,
                                            final int maxFullItems,
                                            final int maxLeadinItems,
                                            final int maxItems,
                                            final @Nonnull String pathParams,
                                            final @Nonnull String expectedTitle,
                                            final @Nonnull List<Integer> expectedFullPostIds,
                                            final @Nonnull List<Integer> expectedLeadInPostIds,
                                            final @Nonnull List<Integer> expectedLinkedPostIds)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(viewProperties.getProperty(P_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getProperty(P_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getProperty(P_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        underTest.prepareRendering(renderContext);
        // when
        underTest.renderView(renderContext);
        // then
        underTest._fullPosts.forEach  (post -> log.info(">>>> full:    {}", post));
        underTest._leadInPosts.forEach(post -> log.info(">>>> lead in: {}", post));
        underTest._linkedPosts.forEach(post -> log.info(">>>> linked:  {}", post));

        final List<Integer> actualFullPostsIds   = getPostIds(underTest._fullPosts);
        final List<Integer> actualLeadInPostsIds = getPostIds(underTest._leadInPosts);
        final List<Integer> actualLinkedPostsIds = getPostIds(underTest._linkedPosts);
        assertThat("full posts",   actualFullPostsIds,   is(expectedFullPostIds));
        assertThat("leadIn posts", actualLeadInPostsIds, is(expectedLeadInPostIds));
        assertThat("all posts",    actualLinkedPostsIds, is(expectedLinkedPostIds));

        final List<Content> allPosts = concat(underTest._fullPosts, underTest._leadInPosts, underTest._linkedPosts);
        final List<ZonedDateTime> publishingDates = allPosts
                .stream()
                .map(post -> post.getProperty(P_PUBLISHING_DATE).get())
                .collect(toList());
        assertSortedInReverseOrder(publishingDates);

        if (!pathParams.equals("/tags"))
          {
            assertThat(underTest.tagsAndCount.size(), is(0)); // TODO: should be: method not called
          }
        else
          {
            assertThat(underTest.tagsAndCount.size(), is(10));
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "postRenderingTestDataNotFound",
          expectedExceptions = HttpStatusException.class,
          expectedExceptionsMessageRegExp = ".*httpStatus=404.*")
    public void must_throw_NotFound (final int seed,
                                     final int maxFullItems,
                                     final int maxLeadinItems,
                                     final int maxItems,
                                     final @Nonnull String pathParams)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(viewProperties.getProperty(P_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getProperty(P_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getProperty(P_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        underTest.prepareRendering(renderContext);
        // when
        underTest.renderView(renderContext);
        // then should throw exception
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "postRenderingTestDataBadRequest",
          expectedExceptions = HttpStatusException.class,
          expectedExceptionsMessageRegExp = ".*httpStatus=400.*")
    public void must_throw_BadRequest (final int seed,
                                       final int maxFullItems,
                                       final int maxLeadinItems,
                                       final int maxItems,
                                       final @Nonnull String pathParams)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(viewProperties.getProperty(P_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getProperty(P_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getProperty(P_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        underTest.prepareRendering(renderContext);
        // when
        underTest.renderView(renderContext);
        // then should throw exception
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "tagCloudRenderingTestData")
    public void must_properly_render_tag_cloud (final int seed,
                                                final List<TagAndCount> expectedTacs)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(viewProperties.getProperty(P_TAG_CLOUD)).thenReturn(Optional.of(true));
        underTest.prepareRendering(renderContext);
        // when
        underTest.renderView(renderContext);
        // then
        final List<Content> allPosts = concat(underTest._fullPosts, underTest._leadInPosts, underTest._linkedPosts);
        assertThat("full posts",   underTest._fullPosts.size(), is(0));    // TODO: should be: method not called
        assertThat("leadIn posts", underTest._leadInPosts.size(), is(0));  // TODO: should be: method not called
        assertThat("all posts",    allPosts.size(), is(0));                // TODO: should be: method not called

        final List<TagAndCount> actualTacs = underTest.tagsAndCount
                            .stream()
                            .sorted(comparing(TagAndCount::getCount).reversed().thenComparing(TagAndCount::getTag))
                            .collect(toList());
        actualTacs.stream().forEach(tac -> log.info(">>>> {} ", tac));
        assertThat(actualTacs, is(expectedTacs));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "tagCloudRenderingTestData")
    public void must_properly_render_tag_cloud2 (final int seed, // TODO: dup code from the previous test
                                                final List<TagAndCount> expectedTacs)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of("tags"));
        underTest.prepareRendering(renderContext);
        // when
        underTest.renderView(renderContext);
        // then
        final List<Content> allPosts = concat(underTest._fullPosts, underTest._leadInPosts, underTest._linkedPosts);
        assertThat("full posts",   underTest._fullPosts.size(), is(0));    // TODO: should be: method not called
        assertThat("leadIn posts", underTest._leadInPosts.size(), is(0));  // TODO: should be: method not called
        assertThat("all posts",    allPosts.size(), is(0));                // TODO: should be: method not called

        final List<TagAndCount> actualTacs = underTest.tagsAndCount
                            .stream()
                            .sorted(comparing(TagAndCount::getCount).reversed().thenComparing(TagAndCount::getTag))
                            .collect(toList());
        actualTacs.stream().forEach(tac -> log.info(">>>> {} ", tac));
        assertThat(actualTacs, is(expectedTacs));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "postRenderingTestData")
    public void must_properly_set_dynamic_properties (final int seed,
                                                      final int maxFullItems,
                                                      final int maxLeadinItems,
                                                      final int maxItems,
                                                      final @Nonnull String pathParams,
                                                      final @Nonnull String expectedTitle,
                                                      final @Nonnull List<Integer> expectedFullPostIds,
                                                      final @Nonnull List<Integer> expectedLeadInPostIds,
                                                      final @Nonnull List<Integer> expectedLinkedPostIds)
      throws Exception
      {
        // given
        mockPosts.createMockData(seed);
        when(viewProperties.getProperty(P_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getProperty(P_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getProperty(P_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        when(request.getPathParams(same(siteNode))).thenReturn(ResourcePath.of(pathParams));
        // when
        underTest.prepareRendering(renderContext);
        // then
        if ((underTest.fullPosts.size() == 1) && underTest.leadInPosts.isEmpty() && underTest.linkedPosts.isEmpty())
          {
            final Integer id = expectedFullPostIds.get(0);
            assertThat(underTest.fullPosts.get(0), is(mockPosts.getPosts().get(id)));
            verify(requestContext).setDynamicNodeProperty(eq(PD_TITLE),    eq(expectedTitle));

            final String sid = String.format("%2d", id);
            verify(requestContext).setDynamicNodeProperty(eq(PD_ID),       eq("id#" + sid));
            verify(requestContext).setDynamicNodeProperty(eq(PD_URL),      eq("http://acme.com/blogNode/post-" + sid + "/"));

            if (mockPosts.getPosts().get(id).getProperty(P_IMAGE_ID).isPresent())
              {
                verify(requestContext).setDynamicNodeProperty(eq(PD_IMAGE_ID), eq("imageId#" + sid));
              }
            // TODO: verify no more invocations
          }
        else if (pathParams.startsWith("/index") || pathParams.startsWith("/tags"))
          {
            verify(requestContext).setDynamicNodeProperty(eq(PD_TITLE),    eq(expectedTitle));
            // TODO: verify no more invocations
          }
        else
          {
            verify(requestContext, never()).setDynamicNodeProperty(any(Key.class), any(Object.class));
          }
      }

    /*******************************************************************************************************************
     *
     * TODO: parameterise
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_retrieve_children_virtual_nodes()
      throws Exception
      {
        // given
        mockPosts.createMockData(45);
        // when
        final List<? extends SiteNode> children = underTest.findVirtualSiteNodes().results();
        // then
        final List<String> expectedUris = mockPosts.getPosts().stream()
                                               .map(c -> c.getExposedUri().get().prependedWith(SITE_NODE_RELATIVE_URI).asString())
                                               .sorted()
                                               .collect(toList());
        final List<String> actualUris = children.stream()
                                                .map(n -> n.getRelativeUri().asString())
                                                .sorted()
                                                .collect(toList());
        assertThat(actualUris, is(expectedUris));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider="dateTestDataProvider")
    public void must_properly_render_the_date (final @Nonnull String localeCode,
                                               final @Nonnull Optional<String> dateFormat,
                                               final @Nonnull ZonedDateTime dateTime,
                                               final @Nonnull Optional<String> timeZone,
                                               final @Nonnull String expectedValue)
      throws Exception
      {
        // given
        final Locale locale = new Locale(localeCode, localeCode);
        // default of RequestLocaleManager
        final DateTimeFormatter dtf = LocalizedDateTimeFormatters.getDateTimeFormatterFor(FormatStyle.FULL, locale)
                                                                 .withZone(ZoneId.of(DEFAULT_TIMEZONE));
        when(requestLocaleManager.getLocales()).thenReturn(Arrays.asList(locale));
        when(requestLocaleManager.getDateTimeFormatter()).thenReturn(dtf);
        mockViewProperty(siteNode, viewId, P_DATE_FORMAT, dateFormat);
        mockViewProperty(siteNode, viewId, P_TIME_ZONE, timeZone);
        // when
        final String actualValue = underTest.formatDateTime(dateTime);
        // then
        assertThat(actualValue, is(expectedValue));
      }

    /*******************************************************************************************************************
     *
     * TODO: variations of the max parameters (including cases in which there are less posts than expected)
     * TODO: test categories
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] postRenderingTestData()
      {
        return new Object[][]
          {
           // seed full leadin max  pathParams          title   expected post ids (full / leadIn / linked)
            { 45,  10,  7,     30,  "",                 "",     asList(69, 57, 63, 86, 44, 89, 18, 73, 16, 94),
                                                                asList(12, 64, 39, 25,  4, 19, 32),
                                                                asList( 3, 71, 80, 11, 99, 97, 62, 96, 38, 13, 90, 21, 48) },

            { 45,  10,  7,     30,  "/post-10",         "Title #10",
                                                                asList(10),
                                                                asList(),
                                                                asList() },

            { 45,  10,  7,     30,  "/post-11",         "Title #11",
                                                                asList(11),
                                                                asList(),
                                                                asList() },

            { 45,  10,  7,     30,  "/post-43",         "Title #43",
                                                                asList(43),
                                                                asList(),
                                                                asList() },

            { 45,  10,  7,     30,  "/tag/tag3",        "",     asList(44, 18, 16, 94, 25, 19, 32, 71, 11, 99),
                                                                asList(21, 84, 30, 55, 74, 78, 45),
                                                                asList(51, 43, 72, 68, 37, 46, 85, 77, 26, 76, 47, 17, 65) },

            { 45,  10,  7,     30,  "/tag/tag5",        "",     asList(57, 63, 89, 18, 94, 39, 25, 19, 32,  3),
                                                                asList(71, 11, 62, 38, 13, 21, 84),
                                                                asList(36, 30, 14, 74,  7, 31, 45, 52, 83,  2, 72, 68,  9) },

            { 45,  10,  7,     30,  "/category1",       "",     asList(44, 18, 12, 25, 19, 71, 99, 97, 90, 48),
                                                                asList(75, 84, 54, 42, 15, 45, 51),
                                                                asList(83, 72, 58, 26, 95, 28, 60, 93, 56, 59, 82, 91)     },

            { 45,  10,  7,     30,  "/category2",       "",     asList(89, 94, 64,  3, 80, 38, 30, 29,  6, 14),
                                                                asList(74, 78, 34, 43, 24, 52, 41),
                                                                asList(46, 79, 85, 76, 53, 47, 65, 66, 61, 35, 49, 92, 23) },

            { 45,  10,  7,     30,  "/index",           "Post index",
                                                                asList(),
                                                                asList(),
                                                                asList(69, 57, 63, 86, 44, 89, 18, 73, 16, 94, 12, 64, 39,
                                                                       25,  4, 19, 32,  3, 71, 80, 11, 99, 97, 62, 96, 38,
                                                                       13, 90, 21, 48, 75, 10, 84, 36, 30, 54, 29, 55,  6,
                                                                       42, 14, 74, 87, 20, 15,  7, 31, 78, 34, 45, 51, 43,
                                                                       24, 52, 83,  2, 72, 68,  9, 37, 58, 41, 46, 79, 85,
                                                                       77, 26, 88, 76, 33, 53, 47, 17, 65, 70,  5, 66,  1,
                                                                       61, 35, 40, 95, 28, 49, 60, 92, 23, 27, 93, 56, 59,
                                                                       82, 81, 98, 50, 67, 22,  8,  0, 91) },

            { 45,  10,  7,     30,  "/index/tag/tag3",  "Posts tagged as 'tag3'",
                                                                asList(),
                                                                asList(),
                                                                asList(44, 18, 16, 94, 25, 19, 32, 71, 11, 99, 21, 84, 30,
                                                                       55, 74, 78, 45, 51, 43, 72, 68, 37, 46, 85, 77, 26,
                                                                       76, 47, 17, 65, 70, 66,  1, 35, 28, 49, 60, 92, 27,
                                                                       93, 56, 98, 67, 91) },

            { 45,  10,  7,     30,  "/index/tag/tag5",  "Posts tagged as 'tag5'",
                                                                asList(),
                                                                asList(),
                                                                asList(57, 63, 89, 18, 94, 39, 25, 19, 32,  3, 71, 11, 62,
                                                                       38, 13, 21, 84, 36, 30, 14, 74,  7, 31, 45, 52, 83,
                                                                        2, 72, 68,  9, 37, 58, 79, 85, 77, 26, 88, 76, 47,
                                                                       17, 65, 70, 66, 40, 95, 28, 49, 27, 56, 59, 50, 22) },

            { 45,  10,  7,     30,  "/index/category1", "Posts in category 'category1'",
                                                                asList(),
                                                                asList(),
                                                                asList(44, 18, 12, 25, 19, 71, 99, 97, 90, 48, 75, 84, 54,
                                                                       42, 15, 45, 51, 83, 72, 58, 26, 95, 28, 60, 93, 56,
                                                                       59, 82, 91) },

            { 45,  10,  7,     30,  "/index/category2", "Posts in category 'category2'",
                                                                asList(),
                                                                asList(),
                                                                asList(89, 94, 64,  3, 80, 38, 30, 29,  6, 14, 74, 78, 34,
                                                                       43, 24, 52, 41, 46, 79, 85, 76, 53, 47, 65, 66, 61,
                                                                       35, 49, 92, 23, 27, 8) },

            { 45,  10,  7,     30,  "/tags",            "Tags",
                                                                asList(),
                                                                asList(),
                                                                asList() },


            { 87,  10,  7,     30,  "",                 "",     asList(88, 47, 25, 80, 28,  9, 13,  3, 43, 51),
                                                                asList(30, 36, 22,  0, 35, 44, 49),
                                                                asList(61, 29, 18, 90, 15, 32, 69, 45, 82, 20, 92, 33, 99) }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] postRenderingTestDataNotFound()
      {
        return new Object[][]
          {
           // seed full leadin max  pathParams
            { 45,  10,  7,     30,  "/inexistent",            },
            { 45,  10,  7,     30,  "/tag/inexistent",        },
            { 45,  10,  7,     30,  "/index/inexistent",      },
            { 45,  10,  7,     30,  "/index/tag/inexistent",  }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] postRenderingTestDataBadRequest()
      {
        return new Object[][]
          {
           // seed full leadin max  pathParams
            { 45,  10,  7,     30,  "/uri/extra-stuff",            },
            { 45,  10,  7,     30,  "/tag/tag5/extra-stuff"        },
            { 45,  10,  7,     30,  "/index/category/extra-stuff"  },
            { 45,  10,  7,     30,  "/index/tag/tagX/extra-stuff"  }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] tagCloudRenderingTestData()
      {
        return new Object[][]
          {
           // seed  expected result
            { 45,   asList(new TagAndCount("tag8",  58, "1"),
                           new TagAndCount("tag9",  57, "2"),
                           new TagAndCount("tag1",  54, "3"),
                           new TagAndCount("tag10", 52, "4"),
                           new TagAndCount("tag5",  52, "4"),
                           new TagAndCount("tag7",  52, "4"),
                           new TagAndCount("tag2",  48, "5"),
                           new TagAndCount("tag4",  47, "6"),
                           new TagAndCount("tag3",  44, "7"),
                           new TagAndCount("tag6",  41, "8")) },

            { 87,   asList(new TagAndCount("tag10", 55, "1"),
                           new TagAndCount("tag1",  53, "2"),
                           new TagAndCount("tag8",  52, "3"),
                           new TagAndCount("tag9",  52, "3"),
                           new TagAndCount("tag3",  48, "4"),
                           new TagAndCount("tag4",  46, "5"),
                           new TagAndCount("tag5",  46, "5"),
                           new TagAndCount("tag2",  44, "6"),
                           new TagAndCount("tag6",  43, "7"),
                           new TagAndCount("tag7",  43, "7")) }
            };
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

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertSortedInReverseOrder (final @Nonnull List<ZonedDateTime> dates)
      {
        final List<ZonedDateTime> sorted = dates.stream()
                                                .sorted(comparing(ZonedDateTime::toEpochSecond).reversed())
                                                .collect(toList());
        assertThat("Improperly sorted", dates, is(sorted));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static List<Integer> getPostIds (final @Nonnull List<Content> posts)
      {
        return posts.stream().map(c -> c.toString())
                             .map(s -> s.replaceAll("^.*Title # *([0-9]+).*$", "$1"))
                             .map(Integer::parseInt)
                             .collect(toList());
      }
  }
