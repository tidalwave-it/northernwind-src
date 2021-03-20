/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.VirtualSiteNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static javax.servlet.http.HttpServletResponse.*;
import static it.tidalwave.util.LocalizedDateTimeFormatters.getDateTimeFormatterFor;
import static it.tidalwave.northernwind.util.CollectionFunctions.*;
import static it.tidalwave.northernwind.util.UrlEncoding.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
import static it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerViewController.*;
import static lombok.AccessLevel.PUBLIC;

/***********************************************************************************************************************
 *
 * <p>A default implementation of the {@link BlogViewController} that is independent of the presentation technology.
 * This class is capable to render:</p>
 *
 * <ul>
 * <li>blog posts (in various ways)</li>
 * <li>an index of the blog</li>
 * <li>a tag cloud</li>
 * </ul>
 *
 * <p>It accepts path parameters as follows:</p>
 *
 * <ul>
 * <li>{@code <uri>}: selects a single post with the given uri;</li>
 * <li>{@code <category>}: selects posts with the given category;</li>
 * <li>{@code tags/<tag>}: selects posts with the given tag;</li>
 * <li>{@code index}: renders a post index, with links to single posts;</li>
 * <li>{@code index/<category>}: renders an index of posts with the given category;</li>
 * <li>{@code index/tag/<tag>}: renders an index of posts with the given tag.</li>
 * </ul>
 *
 * <p>Supported properties of the {@link SiteNode}:</p>
 *
 * <ul>
 * <li>{@code P_CONTENT_PATHS}: one or more {@code Content} that contains the posts to render; they are folders and can have
 *     sub-folders, which will be searched for in a recursive fashion;</li>
 * <li>{@code P_MAX_FULL_ITEMS}: the max. number of posts to be rendered in full;</li>
 * <li>{@code P_MAX_LEADIN_ITEMS}: the max. number of posts to be rendered with lead-in text;</li>
 * <li>{@code P_MAX_ITEMS}: the max. number of posts to be rendered as links;</li>
 * <li>{@code P_DATE_FORMAT}: the pattern for formatting date and times;</li>
 * <li>{@code P_TIME_ZONE}: the time zone for rendering dates (defaults to CET);</li>
 * <li>{@code P_INDEX}: if {@code true}, forces an index rendering (useful e.g. when used in sidebars);</li>
 * <li>{@code P_TAG_CLOUD}: if {@code true}, forces a tag cloud rendering (useful e.g. when used in sidebars).</li>
 * </ul>
 *
 * <p>The {@code P_DATE_FORMAT} property accepts any valid pattern in Java 8, plus the values {@code S-}, {@code M-},
 * {@code L-}, {@code F-}, which stand for small/medium/large and full patterns for a given locale.</p>
 *
 * <p>Supported properties of the {@link Content}:</p>
 *
 * <ul>
 * <li>{@code P_TITLE}: the title;</li>
 * <li>{@code P_FULL_TEXT}: the full text;</li>
 * <li>{@code P_LEADIN_TEXT}: the lead-in text;</li>
 * <li>{@code P_ID}: the unique id;</li>
 * <li>{@code P_IMAGE_ID}: the id of an image representative of the post;</li>
 * <li>{@code P_PUBLISHING_DATE}: the publishing date;</li>
 * <li>{@code P_CREATION_DATE}: the creation date;</li>
 * <li>{@code P_TAGS}: the tags;</li>
 * <li>{@code P_CATEGORY}: the category.</li>
 * </ul>
 *
 * <p>When preparing for rendering, the following dynamic properties will be set, only if a single post is rendered:</p>
 *
 * <ul>
 * <li>{@code PD_URL}: the canonical URL of the post;</li>
 * <li>{@code PD_ID}: the unique id of the post;</li>
 * <li>{@code PD_IMAGE_ID}: the id of the representative image.</li>
 * </ul>
 *
 * <p>Concrete implementations must provide two methods for rendering the blog posts and the tag cloud:</p>
 *
 * <ul>
 * <li>{@link #renderPosts(java.util.List, java.util.List, java.util.List) }</li>
 * <li>{@link #renderTagCloud(java.util.Collection)  }</li>
 * </ul>
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
    @AllArgsConstructor(access = PUBLIC) @Getter @EqualsAndHashCode
    public static class TagAndCount
      {
        public final String tag;
        public final int count;

        @With
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
     * A {@link Finder} which returns virtual {@link SiteNode}s representing the multiple contents served by the
     * {@link SiteNode} associated to this controller. This is typically used to create site maps.
     *
     ******************************************************************************************************************/
    // TODO: add eventual localized versions
    @RequiredArgsConstructor
    private static class VirtualSiteNodeFinder extends SimpleFinderSupport<SiteNode>
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
                    .peek(p -> log.trace(">>>> virtual node for: {}", p.getExposedUri()))
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

    private static final Map<String, Function<Locale, DateTimeFormatter>> DATETIME_FORMATTER_MAP_BY_STYLE = new HashMap<>();

    static
      {
        DATETIME_FORMATTER_MAP_BY_STYLE.put("S-", locale -> getDateTimeFormatterFor(FormatStyle.SHORT, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("M-", locale -> getDateTimeFormatterFor(FormatStyle.MEDIUM, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("L-", locale -> getDateTimeFormatterFor(FormatStyle.LONG, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("F-", locale -> getDateTimeFormatterFor(FormatStyle.FULL, locale));
      }

    protected static final List<Key<ZonedDateTime>> DATE_KEYS = Arrays.asList(P_PUBLISHING_DATE, P_CREATION_DATE);

    public static final ZonedDateTime TIME0 = Instant.ofEpochMilli(0).atZone(ZoneId.of("GMT"));

    public static final String DEFAULT_TIMEZONE = "CET";

    private static final int NO_LIMIT = 9999;

    private static final String INDEX_PREFIX = "index";

    private static final String TAG_PREFIX = "tag";

    private static final ResourcePath TAG_CLOUD = ResourcePath.of("tags");

    private static final Comparator<Content> REVERSE_DATE_COMPARATOR = (p1, p2) ->
        p2.getProperty(DATE_KEYS).orElse(TIME0).compareTo(p1.getProperty(DATE_KEYS).orElse(TIME0));

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final BlogView view;

    @Nonnull
    private final RequestLocaleManager requestLocaleManager;

    private Optional<String> tag = Optional.empty();

    private Optional<String> uriOrCategory = Optional.empty();

    private boolean indexMode = false;

    private boolean tagCloudMode = false;

    protected Optional<String> title = Optional.empty();

    /* VisibleForTesting */ final List<Content> fullPosts = new ArrayList<>();

    /* VisibleForTesting */ final List<Content> leadInPosts = new ArrayList<>();

    /* VisibleForTesting */ final List<Content> linkedPosts = new ArrayList<>();

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
        indexMode  = viewProperties.getProperty(P_INDEX).orElse(false);
        ResourcePath pathParams = context.getPathParams(siteNode);
        tagCloudMode = viewProperties.getProperty(P_TAG_CLOUD).orElse(false);

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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        log.info("renderView() for {}", siteNode);

        if (tagCloudMode)
          {
            renderTagCloud();
          }
        else
          {
            renderPosts(fullPosts, leadInPosts, linkedPosts);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<SiteNode> findVirtualSiteNodes()
      {
        return new VirtualSiteNodeFinder(this);
      }

    /*******************************************************************************************************************
     *
     * Renders the blog posts. Must be implemented by concrete subclasses.
     *
     * @param       fullPosts       the posts to be rendered in full
     * @param       leadinPosts     the posts to be rendered with lead in text
     * @param       linkedPosts     the posts to be rendered as references
     * @throws      Exception       if something fails
     *
     ******************************************************************************************************************/
    @SuppressWarnings("squid:S00112")
    protected abstract void renderPosts (@Nonnull List<Content> fullPosts,
                                         @Nonnull List<Content> leadinPosts,
                                         @Nonnull List<Content> linkedPosts)
      throws Exception;

    /*******************************************************************************************************************
     *
     * Renders the tag cloud. Must be implemented by concrete subclasses.
     *
     * @param       tagsAndCount    the tags
     *
     ******************************************************************************************************************/
    @SuppressWarnings("squid:S00112")
    protected abstract void renderTagCloud (@Nonnull Collection<TagAndCount> tagsAndCount);

    /*******************************************************************************************************************
     *
     * Creates a link for a {@link ResourcePath}.
     *
     * @param       path    the path
     * @return              the link
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final String createLink (final @Nonnull ResourcePath path)
      {
        return siteNode.getSite().createLink(siteNode.getRelativeUri().appendedWith(path));
      }

    /*******************************************************************************************************************
     *
     * Creates a link for a tag.
     *
     * @param       tag     the tag
     * @return              the link
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final String createTagLink (final String tag)
      {
        // TODO: shouldn't ResourcePath always encode incoming strings?
        String link = siteNode.getSite().createLink(siteNode.getRelativeUri().appendedWith(TAG_PREFIX)
                                                                             .appendedWith(encodedUtf8(tag)));

        // TODO: Workaround because createLink() doesn't append trailing / if the link contains a dot.
        // Refactor by passing a parameter to createLink that overrides the default behaviour.
        if (!link.endsWith("/") && !link.contains("?"))
          {
            link += "/";
          }

        return link;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final ResourceProperties getViewProperties()
      {
        return siteNode.getPropertyGroup(view.getId());
      }

    /*******************************************************************************************************************
     *
     * Formats a date with the settings taken from the configuration and the request settings.
     *
     * @param       dateTime        the date to render
     * @return                      the formatted date
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final String formatDateTime (final @Nonnull ZonedDateTime dateTime)
      {
        return dateTime.format(findDateTimeFormatter());
      }

    /*******************************************************************************************************************
     *
     * Prepares the blog posts.
     *
     * @param       context               the rendering context
     * @param       properties            the view properties
     * @throws      HttpStatusException   status 404 if no post found
     *
     ******************************************************************************************************************/
    protected final void prepareBlogPosts (final @Nonnull RenderContext context, final @Nonnull ResourceProperties properties)
      throws HttpStatusException
      {
        final int maxFullItems   = indexMode ? 0        : properties.getProperty(P_MAX_FULL_ITEMS).orElse(NO_LIMIT);
        final int maxLeadinItems = indexMode ? 0        : properties.getProperty(P_MAX_LEADIN_ITEMS).orElse(NO_LIMIT);
        final int maxItems       = indexMode ? NO_LIMIT : properties.getProperty(P_MAX_ITEMS).orElse(NO_LIMIT);

        log.debug(">>>> preparing blog posts for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {} (index: {}, tag: {}, uri: {})",
                  view.getId(), maxFullItems, maxLeadinItems, maxItems, indexMode, tag.orElse(""), uriOrCategory.orElse(""));

        final List<Content> posts = findPosts(context, properties)
                .stream()
                .filter(post -> post.getProperty(P_TITLE).isPresent())
                .sorted(REVERSE_DATE_COMPARATOR)
                .collect(toList());

        if (posts.isEmpty())
          {
            throw new HttpStatusException(SC_NOT_FOUND);
          }

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
    private void renderTagCloud()
      throws Exception
      {
        final Collection<TagAndCount> tagsAndCount = findAllPosts(getViewProperties())
                .stream()
                .flatMap(post -> post.getProperty(P_TAGS).map(List::stream).orElseGet(Stream::empty)) // TODO: simplify in Java 9
                .collect(toMap(t -> t, TagAndCount::new, TagAndCount::reduced))
                .values()
                .stream()
                .sorted(comparing(TagAndCount::getTag))
                .collect(toList());
        renderTagCloud(withRanks(tagsAndCount));
      }

    /*******************************************************************************************************************
     *
     * Finds all the relevant posts, applying filtering as needed.
     *
     ******************************************************************************************************************/
    // TODO: use some short circuit to prevent from loading unnecessary data
    @Nonnull
    private List<Content> findPosts (final @Nonnull RenderContext context, final @Nonnull ResourceProperties properties)
      {
        final ResourcePath pathParams = context.getPathParams(siteNode);
        final boolean filtering  = tag.isPresent() || uriOrCategory.isPresent();
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

        log.debug(">>>> found {} items", posts.size());

        return posts;
      }

    /*******************************************************************************************************************
     *
     * Finds all the posts.
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<Content> findAllPosts (final @Nonnull ResourceProperties properties)
      {
        return properties.getProperty(P_CONTENT_PATHS).orElse(emptyList()).stream()
                .flatMap(path -> siteNode.getSite().find(_Content_).withRelativePath(path).stream()
                                                                   .flatMap(folder -> folder.findChildren().stream()))
                .collect(toList());
      }

    /*******************************************************************************************************************
     *
     * Returns the proper {@link DateTimeFormatter}. It is built from an explicit pattern, if defined in the current
     * {@link SiteNode}; otherwise the one provided by the {@link RequestLocaleManager} is used. The formatter is
     * configured with the time zone defined in the {@code SiteNode}, or a default is used.
     *
     * @return      the {@code DateTimeFormatter}
     *
     ******************************************************************************************************************/
    @Nonnull
    private DateTimeFormatter findDateTimeFormatter()
      {
        final Locale locale = requestLocaleManager.getLocales().get(0);
        final ResourceProperties viewProperties = getViewProperties();
        final DateTimeFormatter dtf = viewProperties.getProperty(P_DATE_FORMAT)
            .map(s -> s.replaceAll("EEEEE+", "EEEE"))
            .map(s -> s.replaceAll("MMMMM+", "MMMM"))
            .map(p -> (((p.length() == 2) ? DATETIME_FORMATTER_MAP_BY_STYLE.get(p).apply(locale)
                                          : DateTimeFormatter.ofPattern(p)).withLocale(locale)))
            .orElse(requestLocaleManager.getDateTimeFormatter());

        final String zoneId = viewProperties.getProperty(P_TIME_ZONE).orElse(DEFAULT_TIMEZONE);
        return dtf.withZone(ZoneId.of(zoneId));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private void setDynamicProperties (final @Nonnull RenderContext context, final @Nonnull Content post)
      {
        context.setDynamicNodeProperty(PD_TITLE, computeTitle(post));
        post.getExposedUri().map(this::createLink).ifPresent(l -> context.setDynamicNodeProperty(PD_URL, l));
        post.getProperty(P_ID).ifPresent(id -> context.setDynamicNodeProperty(PD_ID, id));
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
        final String title     = post.getProperty(P_TITLE).orElse("");
        final String separator = prefix.equals("") || title.equals("") ? "": " - ";

        return prefix + separator + title;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
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
     * Filters the given posts that match the selected category; returns all the posts if the category is empty.
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
        return post.getProperty(P_TAGS).orElse(emptyList()).contains(tag);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<String> filterEmptyString (final @Nonnull String s)
      {
        return s.equals("") ? Optional.empty() : Optional.of(s);
      }
  }
