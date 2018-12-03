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

import javax.annotation.PostConstruct;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import static lombok.AccessLevel.PACKAGE;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
  {
    @AllArgsConstructor(access = PACKAGE) @Getter @EqualsAndHashCode
    protected static class TagAndCount
      {
        public final String tag;
        public final int count;

        @Wither
        public final String rank;

        public TagAndCount (final @Nonnull String tag)
          {
            this(tag, 1, "");
          }

        @Nonnull
        public TagAndCount reduced (final @Nonnull TagAndCount other)
          {
            if (!this.tag.equals(other.tag))
              {
                throw new IllegalArgumentException("Mismatching " + this + " vs " + other);
              }
            
            return new TagAndCount(tag, this.count + other.count, "");
          }

        @Override @Nonnull
        public String toString()
          {
            return String.format("TagAndCount(%s, %d, %s)", tag, count, rank);
          }
      }

    protected static final List<Key<String>> DATE_KEYS = Arrays.asList(PROPERTY_PUBLISHING_DATE, PROPERTY_CREATION_DATE);

    public static final ZonedDateTime TIME0 = Instant.ofEpochMilli(0).atZone(ZoneId.of("GMT"));

    protected static final String TAG_PREFIX = "tag/";

    private static final Comparator<Content> REVERSE_DATE_COMPARATOR = (post1, post2) ->
      {
        final ZonedDateTime dateTime1 = post1.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
        final ZonedDateTime dateTime2 = post2.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
        return dateTime2.compareTo(dateTime1);
      };

    private static final Comparator<TagAndCount> TAG_COUNT_COMPARATOR =
        (tac1, tac2) -> (int)Math.signum(tac2.count - tac1.count);

    @Nonnull
    protected final BlogView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    @Nonnull
    private final RequestHolder requestHolder;

    @Nonnull
    protected final RequestContext requestContext;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    // FIXME: add eventual localized versions
    @Override @Nonnull
    public Finder<SiteNode> findChildrenSiteNodes()
      {
        return new SimpleFinderSupport<SiteNode>()
          {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<? extends SiteNode> computeResults()
              {
                return findAllPosts(getViewProperties())
                        .stream()
                        .flatMap(post -> createChildSiteNode(post).map(Stream::of).orElseGet(Stream::empty)) // FIXME: simplified in Java 9
                        .collect(toList());
              }

            @Nonnull
            private Optional<ChildSiteNode> createChildSiteNode (final @Nonnull Content post)
              {
                return post.getExposedUri().map(uri -> new ChildSiteNode(siteNode,
                                                                         siteNode.getRelativeUri().appendedWith(uri),
                                                                         post.getProperties()));
              }
          };
      }

    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    protected void initialize()
      throws Exception
      {
        // FIXME: ugly workaround for a design limitation. See NW-110.
        if (isCalledBySitemapController()) // called as a CompositeContentsController
          {
            return;
          }

        log.info("Initializing for {}", siteNode);
        // called at initialization
//        try
//          {
            final ResourceProperties viewProperties = getViewProperties();
            final boolean tagCloud = viewProperties.getBooleanProperty(PROPERTY_TAG_CLOUD).orElse(false);

            if (tagCloud)
              {
                generateTagCloud(viewProperties);
              }
            else
              {
                generateBlogPosts(viewProperties);
              }

            render();
//          }
//        // FIXME: this happens when somebody tries to render a blog folder, which shouldn't happen
//        catch (NotFoundException e)
//          {
//            log.warn("While reading property group at initialization", e);
//          }
      }

    /*******************************************************************************************************************
     *
     * Renders the blog posts.
     *
     ******************************************************************************************************************/
    private void generateBlogPosts (final @Nonnull ResourceProperties properties)
      throws HttpStatusException
      {
        final int maxFullItems   = properties.getIntProperty(PROPERTY_MAX_FULL_ITEMS).orElse(99);
        final int maxLeadinItems = properties.getIntProperty(PROPERTY_MAX_LEADIN_ITEMS).orElse(99);
        final int maxItems       = properties.getIntProperty(PROPERTY_MAX_ITEMS).orElse(99);

        log.debug(">>>> rendering blog posts for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {}",
                  view.getId(), maxFullItems, maxLeadinItems, maxItems);

        final List<Content> posts = findPostsInReverseDateOrder(properties)
                .stream()
                .filter(post -> post.getProperty(PROPERTY_TITLE).isPresent())
                .collect(toList());

        final List<List<Content>> split = split(posts, 0, maxFullItems, maxFullItems + maxLeadinItems, maxItems);
        split.get(0).forEach(this::addFullPost);
        split.get(1).forEach(this::addLeadInPost);
        split.get(2).forEach(this::addLinkToPost);
      }

    /*******************************************************************************************************************
     *
     * Renders the blog posts.
     *
     ******************************************************************************************************************/
    private void generateTagCloud (final @Nonnull ResourceProperties properties)
      {
        final Collection<TagAndCount> tagsAndCount = findAllPosts(properties)
                .stream()
                .flatMap(post -> post.getProperty(PROPERTY_TAGS).map(t -> t.split(",")).map(Stream::of).orElseGet(Stream::empty)) // FIXME: simplify in Java 9
                .collect(toMap(tag -> tag, TagAndCount::new, TagAndCount::reduced))
                .values();
        addTagCloud(withRanks(tagsAndCount));
      }

    /*******************************************************************************************************************
     *
     * Finds all the posts.
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<Content> findAllPosts (final @Nonnull ResourceProperties properties)
      {
        return properties.getProperty(PROPERTY_CONTENTS).orElse(emptyList()).stream()
                .flatMap(path -> site.find(Content).withRelativePath(path).stream()
                                                                          .flatMap(folder -> folder.findChildren().stream()))
                .collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    // TODO: embed the sort by reverse date in the finder
    @Nonnull
    private List<Content> findPostsInReverseDateOrder (final @Nonnull ResourceProperties properties)
      throws HttpStatusException
      {
        final String pathParams = requestHolder.get().getPathParams(siteNode).replaceFirst("^/", "");
        final boolean index = properties.getBooleanProperty(PROPERTY_INDEX).orElse(false);
        final List<Content> allPosts = findAllPosts(properties);
        final List<Content> posts = new ArrayList<>();
        //
        // The thing work differently in function of pathParams:
        // + when no pathParams, return all the posts;
        // + when it matches a category, return all the posts in that category;
        // + when it matches an exposed URI of a single specific post:
        //      + if not in 'index' mode, return only that post;
        //      + if in 'index' mode, returns all the posts.
        //
        if ("".equals(pathParams))
          {
            posts.addAll(allPosts);
          }
        else
          {
            if (pathParams.startsWith(TAG_PREFIX))
              {
                final String tag = pathParams.replaceFirst("^" + TAG_PREFIX, "");
                posts.addAll(filteredByTag(allPosts, tag));
              }
            else
              {
                posts.addAll(filteredByExposedUri(allPosts, new ResourcePath(pathParams))
                            // pathParams matches an exposedUri; thus it's not a category, so an index wants all
                            .map(singlePost -> index ? allPosts : singletonList(singlePost))
                            // pathParams didn't match an exposedUri, so it's interpreted as a category to filter posts
                            .orElse(filteredByCategory(allPosts, pathParams)));
              }
          }

        // If not index mode, nothing found and searched for something in path params, return 404
        if (!index && !"".equals(pathParams) && posts.isEmpty())
          {
            throw new HttpStatusException(404);
          }

        Collections.sort(posts, REVERSE_DATE_COMPARATOR);

        log.debug(">>>> found {} items", posts.size());

        return posts;
      }

    /*******************************************************************************************************************
     *
     * Filters the {@code sourcePosts} that matches the selected {@code category}; returns all posts if the category is
     * empty.
     *
     * @param  posts          the source posts
     * @param  category       the category
     * @return                the filtered posts
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<Content> filteredByCategory (final @Nonnull List<Content> posts, final @Nonnull String category)
      {
        return posts.stream().filter(post -> hasCategory(post, category)).collect(toList());
      }

    /*******************************************************************************************************************
     *
     * Filters the {@code sourcePosts} that matches the selected{@code tag}; returns all
     * posts if the category is empty.
     *
     * @param  posts          the source posts
     * @param  tag            the tag
     * @return                the filtered posts
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<Content> filteredByTag (final @Nonnull List<Content> posts, final @Nonnull String tag)
      {
        return posts.stream().filter(post -> hasTag(post, tag)).collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Optional<Content> filteredByExposedUri (final @Nonnull List<Content> posts,
                                                           final @Nonnull ResourcePath exposedUri)
      {
        return posts.stream().filter(post -> post.getExposedUri().map(exposedUri::equals).orElse(false)).findFirst();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addFullPost (@Nonnull Content post);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addLeadInPost (@Nonnull Content post);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addLinkToPost (@Nonnull Content post);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract  void addTagCloud (@Nonnull Collection<TagAndCount> tagsAndCount);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void render()
      throws Exception;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceProperties getViewProperties()
      {
        return siteNode.getPropertyGroup(view.getId());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private List<TagAndCount> withRanks (final @Nonnull Collection<TagAndCount> tagsAndCount)
      {
        final List<Integer> counts = tagsAndCount.stream()
                                                 .map(TagAndCount::getCount)
                                                 .distinct()
                                                 .sorted(reverseOrder())
                                                 .collect(toList());

        return tagsAndCount.stream()
                           .map(tac -> tac.withRank(rankOf(tac.count, counts)))
                           .collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String rankOf (final int count, final List<Integer> counts)
      {
        assert counts.contains(count);
        final int rank = counts.indexOf(count) + 1;
        return (rank <= 10) ? Integer.toString(rank) : "Others";
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean hasCategory (final @Nonnull Content post, final @Nonnull String category)
      {
        return category.equals("") || post.getProperty(PROPERTY_CATEGORY).map(category::equals).orElse(false);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean hasTag (final @Nonnull Content post, final @Nonnull String tag)
      {
        return Arrays.asList(post.getProperty(PROPERTY_TAGS).orElse("").split(",")).contains(tag);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean isCalledBySitemapController()
      {
        for (final StackTraceElement element : Thread.currentThread().getStackTrace())
          {
            if (element.getClassName().contains("SitemapViewController"))
              {
                return true;
              }
          }

        return false;
      }
  }
