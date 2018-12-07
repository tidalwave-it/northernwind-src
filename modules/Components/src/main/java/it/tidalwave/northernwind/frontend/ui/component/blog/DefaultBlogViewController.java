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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import it.tidalwave.util.Finder8;
import it.tidalwave.util.spi.SimpleFinder8Support;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import static lombok.AccessLevel.PACKAGE;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static javax.servlet.http.HttpServletResponse.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
import static it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerViewController.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
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

    /*******************************************************************************************************************
     *
     * A {@link Finder8} which returns virtual {@link SiteNode}s representing the multiple contents served by the
     * {@link SiteNode} associated to this controller. This is typically used to create site maps.
     *
     ******************************************************************************************************************/
    // TODO: add eventual localized versions
    @RequiredArgsConstructor
    private static class VirtualSiteNodeFinder extends SimpleFinder8Support<SiteNode>
      {
        private static final long serialVersionUID = 1L;

        @Nonnull
        private final transient DefaultBlogViewController controller;

        public VirtualSiteNodeFinder (final @Nonnull VirtualSiteNodeFinder other, final @Nonnull Object override)
          {
            super(other, override);
            final VirtualSiteNodeFinder source = getSource(VirtualSiteNodeFinder.class, other, override);
            this.controller = source.controller;
          }

        @Override @Nonnull
        protected List<? extends SiteNode> computeResults()
          {
            return controller.findAllPosts(controller.getViewProperties())
                    .stream()
                    .flatMap(post -> createVirtualNode(post).map(Stream::of).orElseGet(Stream::empty)) // TODO: simplified in Java 9
                    .collect(toList());
          }

        @Nonnull
        private Optional<VirtualSiteNode> createVirtualNode (final @Nonnull Content post)
          {
            final SiteNode siteNode = controller.siteNode;
            return post.getExposedUri().map(uri -> new VirtualSiteNode(siteNode,
                                                                       siteNode.getRelativeUri().appendedWith(uri),
                                                                       post.getProperties()));
          }
      }

    protected enum RenderMode
      {
        FULL, LEAD_IN, LINK
      }

    protected static final List<Key<String>> DATE_KEYS = Arrays.asList(P_PUBLISHING_DATE, P_CREATION_DATE);

    public static final ZonedDateTime TIME0 = Instant.ofEpochMilli(0).atZone(ZoneId.of("GMT"));

    private static final int NO_LIMIT = 9999;

    private static final String INDEX_PREFIX = "index";

    private static final ResourcePath TAG_CLOUD = new ResourcePath("tags");

    private static final String TAG_PREFIX = "tag";

    private Optional<String> tag = Optional.empty();

    private Optional<String> uriOrCategory = Optional.empty();

    private boolean indexMode = false;

    private boolean tagCloudMode = false;

    protected Optional<String> title = Optional.empty();

    private static final Comparator<Content> REVERSE_DATE_COMPARATOR = (post1, post2) ->
      {
        final ZonedDateTime dateTime1 = post1.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
        final ZonedDateTime dateTime2 = post2.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
        return dateTime2.compareTo(dateTime1);
      };

    @Nonnull
    protected final BlogView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    /* VisibleForTesting */ final List<Content> fullPosts = new ArrayList<>();

    /* VisibleForTesting */ final List<Content> leadInPosts = new ArrayList<>();

    /* VisibleForTesting */ final List<Content> linkedPosts = new ArrayList<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder8<SiteNode> findVirtualSiteNodes()
      {
        return new VirtualSiteNodeFinder(this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void prepareRendering (final @Nonnull RenderContext context)
      throws HttpStatusException
      {
        log.info("prepareRendering(RenderContext) for {}", siteNode);

        final ResourceProperties viewProperties = getViewProperties();
        indexMode  = viewProperties.getBooleanProperty(P_INDEX).orElse(false);
        ResourcePath pathParams = context.getPathParams(siteNode);
        tagCloudMode = viewProperties.getBooleanProperty(P_TAG_CLOUD).orElse(false);

        if (pathParams.equals(TAG_CLOUD))
          {
            tagCloudMode = true;
          }
        else if (pathParams.startsWith(INDEX_PREFIX))
          {
            indexMode = true;
            pathParams = pathParams.withoutLeading();
          }

        if (pathParams.startsWith(TAG_PREFIX) && (pathParams.getSegmentCount() == 2)) // matches(TAG_PREFIX, ".*")
          {
            tag = Optional.of(pathParams.getTrailing());
          }
        else if (pathParams.getSegmentCount() == 1)
          {
            uriOrCategory = Optional.of(pathParams.getLeading());
          }
        else if (!pathParams.isEmpty())
          {
            throw new HttpStatusException(SC_BAD_REQUEST);
          }

        if (tagCloudMode)
          {
            setTitle(context);
          }
        else
          {
            prepareBlogPosts(context, viewProperties);

            if ((fullPosts.size() == 1) && leadInPosts.isEmpty() && linkedPosts.isEmpty())
              {
                setDynamicProperties(context, fullPosts.get(0));
              }
            else
              {
                setTitle(context);
              }
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc }
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        log.info("renderView() for {}", siteNode);

        if (tagCloudMode)
          {
            generateTagCloud(context, getViewProperties());
          }
        else
          {
            fullPosts.forEach(post -> addPost(post, RenderMode.FULL));
            leadInPosts.forEach(post -> addPost(post, RenderMode.LEAD_IN));
            linkedPosts.forEach(post -> addPost(post, RenderMode.LINK));
          }

        render();
      }

    /*******************************************************************************************************************
     *
     * Prepares the blog posts.
     *
     ******************************************************************************************************************/
    protected void prepareBlogPosts (final @Nonnull RenderContext context, final @Nonnull ResourceProperties properties)
      throws HttpStatusException
      {
        final int maxFullItems   = indexMode ? 0        : properties.getIntProperty(P_MAX_FULL_ITEMS).orElse(NO_LIMIT);
        final int maxLeadinItems = indexMode ? 0        : properties.getIntProperty(P_MAX_LEADIN_ITEMS).orElse(NO_LIMIT);
        final int maxItems       = indexMode ? NO_LIMIT : properties.getIntProperty(P_MAX_ITEMS).orElse(NO_LIMIT);

        log.debug(">>>> preparing blog posts for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {} (index: {}, tag: {}, uri: {})",
                  view.getId(), maxFullItems, maxLeadinItems, maxItems, indexMode, tag.orElse(""), uriOrCategory.orElse(""));

        final List<Content> posts = findPostsInReverseDateOrder(context, properties)
                .stream()
                .filter(post -> post.getProperty(P_TITLE).isPresent())
                .collect(toList());

        final List<List<Content>> split = split(posts, 0, maxFullItems, maxFullItems + maxLeadinItems, maxItems);
        fullPosts.addAll(split.get(0));
        leadInPosts.addAll(split.get(1));
        linkedPosts.addAll(split.get(2));
      }

    /*******************************************************************************************************************
     *
     * Renders the tag cloud.
     *
     ******************************************************************************************************************/
    private void generateTagCloud (final @Nonnull RenderContext context, final @Nonnull ResourceProperties properties)
      {
        final Collection<TagAndCount> tagsAndCount = findAllPosts(properties)
                .stream()
                .flatMap(post -> post.getProperty(P_TAGS).map(t -> t.split(",")).map(Stream::of).orElseGet(Stream::empty)) // TODO: simplify in Java 9
                .collect(toMap(tag -> tag, TagAndCount::new, TagAndCount::reduced))
                .values()
                .stream()
                .sorted(comparing(TagAndCount::getTag))
                .collect(toList());
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
        return properties.getProperty(P_CONTENTS).orElse(emptyList()).stream()
                .flatMap(path -> site.find(Content).withRelativePath(path).stream()
                                                                          .flatMap(folder -> folder.findChildren().stream()))
                .collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    // TODO: use some short circuit to prevent from loading unnecessary data
    @Nonnull
    private List<Content> findPostsInReverseDateOrder (final @Nonnull RenderContext context,
                                                       final @Nonnull ResourceProperties properties)
      throws HttpStatusException
      {
        final ResourcePath pathParams = context.getPathParams(siteNode);
        final boolean filtering  = tag.isPresent() || uriOrCategory.isPresent();
        final boolean allowEmpty = false; // indexMode || !filtering;
        final List<Content> allPosts = findAllPosts(properties);
        final List<Content> posts = new ArrayList<>();
        //
        // The thing works differently in function of pathParams:
        //      when no pathParams, return all the posts;
        //      when it matches a category, return all the posts in that category;
        //      when it matches an exposed URI of a single specific post:
        //          if not in 'index' mode, return only that post;
        //          if in 'index' mode, returns all the posts.
        //
        if (indexMode && !filtering)
          {
            posts.addAll(allPosts);
          }
        else
          {
            if (tag.isPresent())
              {
                posts.addAll(filteredByTag(allPosts, tag.get()));
              }
            else
              {
                posts.addAll(filteredByExposedUri(allPosts, pathParams)
                            // pathParams matches an exposedUri; thus it's not a category, so an index wants all
                            .map(singlePost -> indexMode ? allPosts : singletonList(singlePost))
                            // pathParams didn't match an exposedUri, so it's interpreted as a category to filter posts
                            .orElseGet(() -> filteredByCategory(allPosts, uriOrCategory)));
              }
          }

        if (!allowEmpty && posts.isEmpty())
          {
            throw new HttpStatusException(SC_NOT_FOUND);
          }

        Collections.sort(posts, REVERSE_DATE_COMPARATOR);

        log.debug(">>>> found {} items", posts.size());

        return posts;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private void setDynamicProperties (final @Nonnull RenderContext context, final @Nonnull Content post)
      {
        post.getProperty(P_ID).ifPresent(id -> context.setDynamicNodeProperty(PD_ID, id));
        context.setDynamicNodeProperty(PD_TITLE, computeTitle(post));
        post.getExposedUri().map(this::createLink)
                .ifPresent(l -> context.setDynamicNodeProperty(PD_URL, l));
        post.getProperty(P_IMAGE_ID).ifPresent(id -> context.setDynamicNodeProperty(PD_IMAGE_ID, id));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void setTitle (final @Nonnull RenderContext context)
      {
        if (tagCloudMode)
          {
            title = Optional.of("Tags");
          }
        else if (indexMode)
          {
            title = Optional.of("Post index");

            if (tag.isPresent())
              {
                title = Optional.of(String.format("Posts tagged as '%s'", tag.get()));
              }
            else if (uriOrCategory.isPresent())
              {
                title = Optional.of(String.format("Posts in category '%s'", uriOrCategory.get()));
              }
          }
        else
          {
            title = getViewProperties().getProperty(P_TITLE).map(String::trim).flatMap(this::filterEmptyString);
          }

        title.ifPresent(s -> view.setTitle(s));
        title.ifPresent(s -> context.setDynamicNodeProperty(PD_TITLE, s));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeTitle (final @Nonnull Content post)
      {
        final String prefix    = siteNode.getProperty(P_TITLE).orElse("");
        final String title     = post.getProperties().getProperty(P_TITLE).orElse("");
        final String separator = prefix.equals("") || title.equals("") ? "": " - ";

        return prefix + separator + title;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String createLink (final @Nonnull ResourcePath path)
      {
        return site.createLink(siteNode.getRelativeUri().appendedWith(path));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String createTagLink (final String tag)
      {
        try
          {
            // TODO: shouldn't ResourcePath always encode incoming strings?
            String link = site.createLink(siteNode.getRelativeUri().appendedWith(TAG_PREFIX)
                                                                   .appendedWith(URLEncoder.encode(tag, "UTF-8")));

            // TODO: Workaround because createLink() doesn't append trailing / if the link contains a dot.
            // Refactor by passing a parameter to createLink that overrides the default behaviour.
            if (!link.endsWith("/") && !link.contains("?"))
              {
                link += "/";
              }

            return link;
          }
        catch (UnsupportedEncodingException e)
          {
            log.error("", e);
            return "";
          }
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
    private static List<Content> filteredByCategory (final @Nonnull List<Content> posts,
                                                     final @Nonnull Optional<String> category)
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
    protected abstract void addPost (@Nonnull Content post, @Nonnull RenderMode renderMode);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void addTagCloud (@Nonnull Collection<TagAndCount> tagsAndCount);

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
    private static boolean hasCategory (final @Nonnull Content post, final @Nonnull Optional<String> category)
      {
        return !category.isPresent() || post.getProperty(P_CATEGORY).equals(category);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private static boolean hasTag (final @Nonnull Content post, final @Nonnull String tag)
      {
        return Arrays.asList(post.getProperty(P_TAGS).orElse("").split(",")).contains(tag);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<String> filterEmptyString (final @Nonnull String s)
      {
        final Optional<String> x = (s.equals("") ? Optional.empty() : Optional.of(s));
        return x;
      }
  }
