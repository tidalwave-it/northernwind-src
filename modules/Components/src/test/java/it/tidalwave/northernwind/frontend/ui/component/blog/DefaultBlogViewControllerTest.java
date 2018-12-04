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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import it.tidalwave.util.Finder8;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.ArrayListFinder8;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController.TagAndCount;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import lombok.extern.slf4j.Slf4j;
import static java.time.format.DateTimeFormatter.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
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
        public final List<Content> fullPosts = new ArrayList<>();

        public final List<Content> leadInPosts = new ArrayList<>();

        public final List<Content> linkedPosts = new ArrayList<>();

        public final List<TagAndCount> tagsAndCount = new ArrayList<>();

        public UnderTest (final @Nonnull BlogView view,
                          final @Nonnull SiteNode siteNode,
                          final @Nonnull Site site,
                          final @Nonnull RequestHolder requestHolder,
                          final @Nonnull RequestContext requestContext)
          {
            super(view, siteNode, site, requestHolder, requestContext);
          }

        @Override
        protected void addFullPost (final @Nonnull Content post)
          {
            fullPosts.add(post);
          }

        @Override
        protected void addLeadInPost (final @Nonnull Content post)
          {
            leadInPosts.add(post);
          }

        @Override
        protected void addLinkToPost (final @Nonnull Content post)
          {
            linkedPosts.add(post);
          }

        @Override
        protected void addTagCloud (final @Nonnull Collection<TagAndCount> tagsAndCount)
          {
            this.tagsAndCount.addAll(tagsAndCount);
          }

        @Override
        protected void render()
          {
          }
      }

    private Site site;

    private SiteNode siteNode;

    private BlogView view;

    private UnderTest underTest;

    private ResourceProperties viewProperties;

    private ResourceProperties siteNodeProperties;

    private Request request;

    private RequestHolder requestHolder;

    private List<Content> posts;

    private List<ZonedDateTime> dates;

    private List<String> tags;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws NotFoundException
      {
        final Id viewId = new Id("id");

        site = mock(Site.class);
        MockSiteNodeSiteFinder.registerTo(site);
        MockContentSiteFinder.registerTo(site);

        when(site.createLink(any(ResourcePath.class))).then(invocation ->
          {
            final ResourcePath path = invocation.getArgument(0);
            return String.format("http://acme.com%s", path.asString());
          });

        viewProperties = createMockProperties();
        siteNodeProperties = createMockProperties();

        siteNode = mock(SiteNode.class);
        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        view = mock(BlogView.class);
        when(view.getId()).thenReturn(viewId);

        request = mock(Request.class);
        requestHolder = mock(RequestHolder.class);
        when(requestHolder.get()).thenReturn(request);

        final RequestContext requestContext = mock(RequestContext.class);

        underTest = new UnderTest(view, siteNode, site, requestHolder, requestContext);
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
                                            final @Nonnull List<Integer> expectedFullPostIds,
                                            final @Nonnull List<Integer> expectedLeadInPostIds,
                                            final @Nonnull List<Integer> expectedLinkedPostIds)
      throws Exception
      {
        // given
        createMockData(seed);
        when(viewProperties.getIntProperty(PROPERTY_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getIntProperty(PROPERTY_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getIntProperty(PROPERTY_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        when(request.getPathParams(same(siteNode))).thenReturn(pathParams);
        // when
        underTest.initialize();
        // then
        underTest.fullPosts.forEach  (post -> log.info(">>>> full:    {}", post));
        underTest.leadInPosts.forEach(post -> log.info(">>>> lead in: {}", post));
        underTest.linkedPosts.forEach(post -> log.info(">>>> linked:  {}", post));

        final List<Integer> actualFullPostsIds   = getPostIds(underTest.fullPosts);
        final List<Integer> actualLeadInPostsIds = getPostIds(underTest.leadInPosts);
        final List<Integer> actualLinkedPostsIds = getPostIds(underTest.linkedPosts);
        assertThat("full posts",   actualFullPostsIds,   is(expectedFullPostIds));
        assertThat("leadIn posts", actualLeadInPostsIds, is(expectedLeadInPostIds));
        assertThat("all posts",    actualLinkedPostsIds, is(expectedLinkedPostIds));

        final List<Content> allPosts = concat(underTest.fullPosts, underTest.leadInPosts, underTest.linkedPosts);
        final List<ZonedDateTime> publishingDates = allPosts
                .stream()
                .map(post -> post.getProperties().getDateTimeProperty(PROPERTY_PUBLISHING_DATE).get())
                .collect(toList());
        assertSortedInReverseOrder(publishingDates);

        assertThat(underTest.tagsAndCount.size(), is(0)); // TODO: should be: method not called
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
        createMockData(seed);
        when(viewProperties.getBooleanProperty(PROPERTY_TAG_CLOUD)).thenReturn(Optional.of(true));
        // when
        underTest.initialize();
        // then
        final List<Content> allPosts = concat(underTest.fullPosts, underTest.leadInPosts, underTest.linkedPosts);
        assertThat("full posts",   underTest.fullPosts.size(), is(0));    // TODO: should be: method not called
        assertThat("leadIn posts", underTest.leadInPosts.size(), is(0));  // TODO: should be: method not called
        assertThat("all posts",    allPosts.size(), is(0));               // TODO: should be: method not called

        final List<TagAndCount> actualTacs = underTest.tagsAndCount
                            .stream()
                            .sorted(comparing(TagAndCount::getCount).reversed().thenComparing(TagAndCount::getTag))
                            .collect(toList());
        actualTacs.stream().forEach(tac -> log.info(">>>> {} ", tac));
        assertThat(actualTacs, is(expectedTacs));
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
           // seed full leadin max  pathParams          // expected post ids (full / leadIn / linked)
            { 45,  10,  7,     30,  "",                 asList(69, 57, 63, 86, 44, 89, 18, 73, 16, 94),
                                                        asList(12, 64, 39, 25,  4, 19, 32),
                                                        asList( 3, 71, 80, 11, 99, 97, 62, 96, 38, 13, 90, 21, 48) },

            { 45,  10,  7,     30,  "/tag/tag3",        asList(44, 18, 16, 94, 25, 19, 32, 71, 11, 99),
                                                        asList(21, 84, 30, 55, 74, 78, 45),
                                                        asList(51, 43, 72, 68, 37, 46, 85, 77, 26, 76, 47, 17, 65) },

            { 45,  10,  7,     30,  "/tag/inexistent",  asList(),
                                                        asList(),
                                                        asList() },

            { 87,  10,  7,     30,  "",                 asList(88, 47, 25, 80, 28,  9, 13,  3, 43, 51),
                                                        asList(30, 36, 22,  0, 35, 44, 49),
                                                        asList(61, 29, 18, 90, 15, 32, 69, 45, 82, 20, 92, 33, 99) }
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
           // seed
            { 45,  Arrays.asList(new TagAndCount("tag8",  58, "1"),
                                 new TagAndCount("tag9",  57, "2"),
                                 new TagAndCount("tag1",  54, "3"),
                                 new TagAndCount("tag10", 52, "4"),
                                 new TagAndCount("tag5",  52, "4"),
                                 new TagAndCount("tag7",  52, "4"),
                                 new TagAndCount("tag2",  48, "5"),
                                 new TagAndCount("tag4",  47, "6"),
                                 new TagAndCount("tag3",  44, "7"),
                                 new TagAndCount("tag6",  41, "8")) },

            { 87,  Arrays.asList(new TagAndCount("tag10", 55, "1"),
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
    private void createMockData (final int seed)
      throws NotFoundException
      {
        tags  = IntStream.rangeClosed(1, 10).mapToObj(i -> "tag" + i).collect(toList());
        dates = createMockDateTimes(100, seed);
        posts = createMockPosts(100, dates, tags, seed);

        // TODO: use multiple folders
        final List<String> postFolderRelativePaths = Arrays.asList("/blog");
        final Content blogFolder1 = site.find(Content).withRelativePath("/blog").result();
        when(blogFolder1.findChildren()).thenReturn((Finder8)(new ArrayListFinder8<>(posts)));

        when(viewProperties.getProperty(eq(PROPERTY_CONTENTS))).thenReturn(Optional.of(postFolderRelativePaths));
      }

    /*******************************************************************************************************************
     *
     * Creates the given number of mock dates and times, spanned in the decade 1/1/2018 - 31/12/2028.
     *
     * @param       count       the count of dates
     * @param       seed        the seed for the pseudo-random sequence
     * @return                  the dates
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ZonedDateTime> createMockDateTimes (final @Nonnegative int count, final int seed)
      {
        final ZonedDateTime base = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT"));
        final List<ZonedDateTime> dates = new Random(seed).ints(count, 0, 10 * 365 * 24 * 60)
                                                          .mapToObj(base::plusMinutes)
                                                          .collect(toList());
        final ZonedDateTime max = dates.stream().max(ZonedDateTime::compareTo).get();
        final ZonedDateTime min = dates.stream().min(ZonedDateTime::compareTo).get();
        assert Duration.between(min, max).getSeconds() > 9 * 365 * 24 * 60 : "No timespan";
        return dates;
      }

    /*******************************************************************************************************************
     *
     * Create the given number of mock {@link Content} instances represeting blog posts.
     * Each one is assigned:
     *
     * <ul>
     * <li>a {@code PROPERTY_PUBLISHING_DATE} taken from the given collection of dateTimes;</li>
     * <li>a {@code PROPERTY_TITLE} set as {@code "Title #&lt;num&gt;"}</li>
     * <li>a set of tags taken from the given collection, each one having 50% of chances of being set.</li>
     * </ul>
     *
     * All used random sequences are reproducible for the sake of test assertions.
     *
     * @param       count       the required number of posts
     * @param       dateTimes   a collection of datetimes used as the publishing date of each post
     * @param       tags        a collection of tags that are randomly assigned to posts
     * @param       seed        the seed for the pseudo-random sequence
     * @return                  the mock posts
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<Content> createMockPosts (final @Nonnegative int count,
                                                  final @Nonnull List<ZonedDateTime> dateTimes,
                                                  final @Nonnull List<String> tags,
                                                  final int seed)
      {
        final List<Content> posts = new ArrayList<>();
        final Random random = new Random(seed);

        for (int i = 0; i< count; i++)
          {
            final String title = "Title #" + i;
            final ZonedDateTime dateTime = dateTimes.get(i);
            final Content post = mock(Content.class);
            final ResourceProperties properties = createMockProperties();
            when(post.getProperties()).thenReturn(properties);
            when(post.getProperty(any(Key.class))).thenCallRealMethod();
            when(properties.getProperty(PROPERTY_PUBLISHING_DATE)).thenReturn(Optional.of(ISO_ZONED_DATE_TIME.format(dateTime)));
            when(properties.getProperty(PROPERTY_TITLE)).thenReturn(Optional.of(title));

            final String tagsAsString = tags.stream().filter(__ -> random.nextDouble() > 0.5).collect(joining(","));

            if (!tagsAsString.equals(""))
              {
                when(properties.getProperty(PROPERTY_TAGS)).thenReturn(Optional.of(tagsAsString));
              }

            when(post.toString()).thenReturn(String.format("Content(%3d) - %s - %s - %s", i, ISO_ZONED_DATE_TIME.format(dateTime), title, tagsAsString));
            posts.add(post);
            log.info(">>>> post {}", post);
          }

        return posts;
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
                             .map(s -> s.replaceAll("^.*\\( *([0-9]+)\\).*$", "$1"))
                             .map(Integer::parseInt)
                             .collect(toList());
      }
  }
