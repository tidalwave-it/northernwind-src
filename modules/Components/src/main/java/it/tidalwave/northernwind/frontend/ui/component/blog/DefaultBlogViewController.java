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
 * $Id: 803acd10d11b7a7194f7c5f88a85ebb723a51090 $
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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: 803acd10d11b7a7194f7c5f88a85ebb723a51090 $
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
  {
    @AllArgsConstructor @Getter @ToString
    protected static class TagAndCount
      {
        public final String tag;
        public int count;
        public String rank;
      }

    public static final List<Key<String>> DATE_KEYS = Arrays.asList(PROPERTY_PUBLISHING_DATE, PROPERTY_CREATION_DATE);

    public static final ZonedDateTime TIME0 = Instant.ofEpochMilli(0).atZone(ZoneId.of("GMT"));

    protected static final String TAG_PREFIX = "tag/";

    private final Comparator<Content> REVERSE_DATE_COMPARATOR = new Comparator<Content>()
      {
        @Override
        public int compare (final @Nonnull Content post1, final @Nonnull Content post2)
          {
            final ZonedDateTime dateTime1 = post1.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
            final ZonedDateTime dateTime2 = post2.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
            return dateTime2.compareTo(dateTime1);
          }
      };

    private final Comparator<TagAndCount> TAG_COUNT_COMPARATOR = new Comparator<TagAndCount>()
      {
        @Override
        public int compare (final @Nonnull TagAndCount tac1, final @Nonnull TagAndCount tac2)
          {
            return (int)Math.signum(tac2.count - tac1.count);
          }
      };

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
            @Override
            protected List<? extends SiteNode> computeResults()
              {
                log.info("findCompositeContents()");

                final List<SiteNode> results = findAllPosts(siteNode.getPropertyGroup(view.getId()))
                        .stream()
                        .flatMap(post -> createChildSiteNode(post).map(Stream::of).orElseGet(Stream::empty)) // FIXME: simplified in Java 9
                        .collect(toList());

                log.info(">>>> returning: {}", results);

                return results;
              }

            @Nonnull
            private Optional<ChildSiteNode> createChildSiteNode (final @Nonnull Content post)
              {
                return post.getExposedUri().map(postUri -> new ChildSiteNode(siteNode, siteNode.getRelativeUri().appendedWith(postUri), post.getProperties()));
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
        try
          {
            final ResourceProperties siteNodeProperties = siteNode.getPropertyGroup(view.getId());
            final boolean tagCloud = siteNodeProperties.getBooleanProperty(PROPERTY_TAG_CLOUD).orElse(false);

            if (tagCloud)
              {
                generateTagCloud();
              }
            else
              {
                generateBlogPosts();
              }

            render();
          }
        // FIXME: this happens when somebody tries to render a blog folder, which shouldn't happen
        catch (NotFoundException e)
          {
            log.warn("While reading property group at initialization", e);
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the blog posts.
     *
     ******************************************************************************************************************/
    private void generateBlogPosts()
      throws HttpStatusException
      {
        final ResourceProperties componentProperties = siteNode.getPropertyGroup(view.getId());
        final int maxFullItems = componentProperties.getIntProperty(PROPERTY_MAX_FULL_ITEMS).orElse(99);
        final int maxLeadinItems = componentProperties.getIntProperty(PROPERTY_MAX_LEADIN_ITEMS).orElse(99);
        final int maxItems = componentProperties.getIntProperty(PROPERTY_MAX_ITEMS).orElse(99);

        log.debug(">>>> rendering blog posts for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {}",
                  view.getId(), maxFullItems, maxLeadinItems, maxItems);

        int currentItem = 0;

        for (final Content post : findPostsInReverseDateOrder(componentProperties))
          {
            try
              {
                log.debug(">>>>>>> processing blog item #{}: {}", currentItem, post);
                // Skip folders used for categories - they have no title - FIXME: use PROPERTY_FULLTEXT
                if (post.getProperties().getProperty(PROPERTY_TITLE).isPresent())
                  {
                    if (currentItem < maxFullItems)
                      {
                        addFullPost(post);
                      }
                    else if (currentItem < maxFullItems + maxLeadinItems)
                      {
                        addLeadInPost(post);
                      }
                    else if (currentItem < maxItems)
                      {
                        addReference(post);
                      }

                    currentItem++;
                  }
              }
            catch (NotFoundException | IOException e)
              {
                log.warn("While reading property group of post", e);
              }
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the blog posts.
     *
     ******************************************************************************************************************/
    private void generateTagCloud()
      throws HttpStatusException
      {
        final Map<String, TagAndCount> tagAndCountMapByTag = new TreeMap<>();
        final ResourceProperties siteNodeProperties = siteNode.getPropertyGroup(view.getId());

        findAllPosts(siteNodeProperties).stream()
                .flatMap(post -> Stream.of(post.getProperties().getProperty(PROPERTY_TAGS).orElse("").split(",")))
                .forEach(tag ->
              {
                TagAndCount tagAndCount = tagAndCountMapByTag.get(tag);

                if (tagAndCount == null)
                  {
                    tagAndCount = new TagAndCount(tag, 0, "");
                    tagAndCountMapByTag.put(tag, tagAndCount);
                  }

                tagAndCount.count++;
              });

        final Collection<TagAndCount> tagsAndCount = tagAndCountMapByTag.values();
        computeRanks(tagsAndCount);
        addTagCloud(tagsAndCount);
      }

    /*******************************************************************************************************************
     *
     * Finds all the posts.
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<Content> findAllPosts (final @Nonnull ResourceProperties siteNodeProperties)
      {
        return siteNodeProperties.getProperty(PROPERTY_CONTENTS).orElse(emptyList()).stream()
                .flatMap(relativePath ->
                    {
                      try
                        {
                          final Content postsFolder = site.find(Content).withRelativePath(relativePath).result(); // FIXME: use optionalResult()
                          return postsFolder.findChildren().results().stream();
                        }
                      catch (NotFoundException e)
                        {
                          return Stream.empty();
                        }
                    })
                .collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    // TODO: embed the sort by reverse date in the finder
    @Nonnull
    private List<Content> findPostsInReverseDateOrder (final @Nonnull ResourceProperties siteNodeProperties)
      throws HttpStatusException
      {
        final String pathParams = requestHolder.get().getPathParams(siteNode).replaceFirst("^/", "");
        final boolean index = siteNodeProperties.getBooleanProperty(PROPERTY_INDEX).orElse(false);
        final List<Content> allPosts = findAllPosts(siteNodeProperties);
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
    protected abstract void addFullPost (@Nonnull Content post)
      throws IOException, NotFoundException;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addLeadInPost (@Nonnull Content post)
      throws IOException, NotFoundException;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addReference (@Nonnull Content post)
      throws IOException, NotFoundException;

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
    private void computeRanks (final @Nonnull Collection<TagAndCount> tagsAndCount)
      {
        final List<TagAndCount> tagsAndCountByCountDescending = new ArrayList<>(tagsAndCount);
        Collections.sort(tagsAndCountByCountDescending, TAG_COUNT_COMPARATOR);

        int rank = 1;
        int previousCount = 0;

        for (final TagAndCount tac : tagsAndCountByCountDescending)
          {
            tac.rank = (rank <= 10) ? Integer.toString(rank) : "Others";

            if (previousCount != tac.count)
              {
                rank++;
              }

            previousCount = tac.count;
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean hasCategory (final @Nonnull Content post, final @Nonnull String category)
      {
        return category.equals("") || category.equals(post.getProperties().getProperty(PROPERTY_CATEGORY).orElse("---"));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean hasTag (final @Nonnull Content post, final @Nonnull String tag)
      {
        return Arrays.asList(post.getProperties().getProperty(PROPERTY_TAGS).orElse("").split(",")).contains(tag);
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
