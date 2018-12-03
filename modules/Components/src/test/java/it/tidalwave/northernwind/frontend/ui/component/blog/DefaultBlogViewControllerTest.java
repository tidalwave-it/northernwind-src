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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import lombok.extern.slf4j.Slf4j;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static java.time.format.DateTimeFormatter.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
import java.time.Duration;
import static java.time.temporal.ChronoField.YEAR;
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

        public UnderTest (BlogView view,
                          SiteNode siteNode,
                          Site site,
                          RequestHolder requestHolder,
                          RequestContext requestContext)
          {
            super(view, siteNode, site, requestHolder, requestContext);
          }

        @Override
        protected void addFullPost (Content post)
          {
            fullPosts.add(post);
          }

        @Override
        protected void addLeadInPost (Content post)
          {
            leadInPosts.add(post);
          }

        @Override
        protected void addLinkToPost (Content post)
          {
            linkedPosts.add(post);
          }

        @Override
        protected void addTagCloud (Collection<TagAndCount> tagsAndCount)
          {
          }

        @Override
        protected void render()
          {
          }
      }

    private Site site;

    private BlogView view;

    private UnderTest underTest;

    private ResourceProperties viewProperties;

    private ResourceProperties siteNodeProperties;

    private RequestHolder requestHolder;

    private List<Content> posts;

    private List<ZonedDateTime> dates;

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

        final SiteNode siteNode = mock(SiteNode.class);
        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        view = mock(BlogView.class);
        when(view.getId()).thenReturn(viewId);

        Request request = mock(Request.class);
        when(request.getPathParams(same(siteNode))).thenReturn("");

        requestHolder = mock(RequestHolder.class);
        when(requestHolder.get()).thenReturn(request);

        RequestContext requestContext = mock(RequestContext.class);

        underTest = new UnderTest(view, siteNode, site, requestHolder, requestContext);

        dates = createMockDates(100);
        posts = createMockPosts(100, new ArrayList<>(dates));

        final List<String> postFolderRelativePaths = Arrays.asList("/blog");
        final Content blogFolder1 = site.find(Content).withRelativePath("/blog").result();
        when(blogFolder1.findChildren()).thenReturn((Finder8)(new ArrayListFinder8<>(posts)));

        when(viewProperties.getProperty(eq(PROPERTY_CONTENTS))).thenReturn(Optional.of(postFolderRelativePaths));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_posts()
      throws Exception
      {
        // given
        final int maxFullItems = 10;
        final int maxLeadinItems = 7;
        final int maxItems = 30;
        when(viewProperties.getIntProperty(PROPERTY_MAX_FULL_ITEMS)).thenReturn(Optional.of(maxFullItems));
        when(viewProperties.getIntProperty(PROPERTY_MAX_LEADIN_ITEMS)).thenReturn(Optional.of(maxLeadinItems));
        when(viewProperties.getIntProperty(PROPERTY_MAX_ITEMS)).thenReturn(Optional.of(maxItems));
        // when
        underTest.initialize();
        // then
        assertThat("full posts", underTest.fullPosts.size(), is(maxFullItems));
        assertThat("leadIn posts", underTest.leadInPosts.size(), is(maxLeadinItems));

        final List<Content> allPosts = new ArrayList<>();
        allPosts.addAll(underTest.fullPosts);
        allPosts.addAll(underTest.leadInPosts);
        allPosts.addAll(underTest.linkedPosts);
        assertThat("all posts", allPosts.size(), is(maxItems));

        allPosts.forEach(post -> log.info(">>>> {}", post));

        final List<ZonedDateTime> publishingDates = allPosts.stream()
                                                            .map(post -> post.getProperties().getDateTimeProperty(PROPERTY_PUBLISHING_DATE).get())
                                                            .collect(toList());
        final List<ZonedDateTime> sorted = publishingDates.stream()
                                                          .sorted(comparing(ZonedDateTime::toEpochSecond).reversed())
                                                          .collect(toList());
        assertThat("Improperly sorted", publishingDates, is(sorted));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ZonedDateTime> createMockDates (final @Nonnegative int count)
      {
        final ZonedDateTime base = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT"));
        final List<ZonedDateTime> dates = new Random(23).ints(count, 0, 10 * 365 * 24 * 60).mapToObj(m -> base.plusMinutes(m)).collect(toList());
        final ZonedDateTime max = dates.stream().max(ZonedDateTime::compareTo).get();
        final ZonedDateTime min = dates.stream().min(ZonedDateTime::compareTo).get();
        assert Duration.between(min, max).getSeconds() > 9 * 365 * 24 * 60 : "No timespan";
        return dates;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<Content> createMockPosts (final @Nonnegative int count,
                                                  final @Nonnull List<ZonedDateTime> dates)
      {
        final List<Content> posts = new ArrayList<>();

        for (int i = 0; i< count; i++)
          {
            final ZonedDateTime date = dates.remove(0);
            final Content post = mock(Content.class);
            final ResourceProperties properties = createMockProperties();
            when(post.getProperties()).thenReturn(properties);
            when(post.getProperty(any(Key.class))).thenCallRealMethod();
            when(properties.getProperty(PROPERTY_PUBLISHING_DATE)).thenReturn(Optional.of(ISO_ZONED_DATE_TIME.format(date)));
            when(properties.getProperty(PROPERTY_TITLE)).thenReturn(Optional.of("title " + date));
            when(post.toString()).thenReturn(String.format("Content(%3d) - %s", i, ISO_ZONED_DATE_TIME.format(date)));
            posts.add(post);
          }

        return posts;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceProperties createMockProperties()
      {
        final ResourceProperties properties = mock(ResourceProperties.class);
        when(properties.getProperty(any(Key.class))).thenReturn(Optional.empty()); // default
        when(properties.getDateTimeProperty(any(Key.class))).thenCallRealMethod();
        when(properties.getDateTimeProperty(any(List.class))).thenCallRealMethod();
        return properties;
      }
  }
