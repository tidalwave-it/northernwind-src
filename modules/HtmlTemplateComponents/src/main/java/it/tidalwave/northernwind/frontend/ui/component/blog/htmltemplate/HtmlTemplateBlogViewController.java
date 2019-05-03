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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.time.ZonedDateTime;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link BlogViewController} based on HTML templates.</p>
 *
 * <p>The templates for rendering the page can be specified by means of the following properties:</p>
 *
 * <ul>
 * <li>{@code P_TEMPLATE_POSTS_PATH}: the template for rendering posts;</li>
 * <li>{@code P_TEMPLATE_TAG_CLOUD_PATH}: the template for rendering the tag cloud.</li>
 * </ul>
 *
 * <p>This controller calls render methods to the view by passing {@link Aggregates} to be used with templates.</p>
 * <p>In case of post rendering, the following aggregates are defined:</p>
 *
 * <ul>
 * <li>{@code fullPosts}: the posts to be rendered in full;</li>
 * <li>{@code leadinPosts}: the posts to be rendered as lead-in text;</li>
 * <li>{@code linkedPosts}: the posts to be rendered as links.</li>
 * </ul>
 *
 * <p>Each item is an {@link Aggregate} of the following fields:</p>
 *
 * <ul>
 * <li>{@code title}: the title of the post;</li>
 * <li>{@code text}: the text of the post;</li>
 * <li>{@code link}: the URL of the post;</li>
 * <li>{@code id}: the unique id of the post;</li>
 * <li>{@code publishDate}: the publishing date of the post;</li>
 * <li>{@code category}: the category of the post;</li>
 * <li>{@code tags}: a list of tags of the post.</li>
 * </ul>
 *
 * <p>Each tag is an {@code Aggregate} of two attributes:</p>
 *
 * <ul>
 * <li>{@code name}: the name of the tag;</li>
 * <li>{@code link}: the target URL.</li>
 * </ul>
 *
 * <p>In case of tag cloud rendering, the {@code Aggregate} is named {@code tags} and each contained tag, in addition to the
 * previous attributes, has got:</p>
 *
 * <ul>
 * <li>{@code rank}: the rank of the tag;</li>
 * <li>{@code count}: the count of the tagged posts.</li>
 * </ul>
 *
 * @see     HtmlTemplateBlogView
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    /** The relative path to the {@link Content} that contains a template for rendering posts. */
    public static final Key<ResourcePath> P_TEMPLATE_POSTS_PATH = new Key<ResourcePath>("postsTemplate") {};

    /** The relative path to the {@link Content} that contains a template for rendering the tag cloud. */
    public static final Key<ResourcePath> P_TEMPLATE_TAG_CLOUD_PATH = new Key<ResourcePath>("tagCloudTemplate") {};

    @Nonnull
    private final HtmlTemplateBlogView view;

    /*******************************************************************************************************************
     *
     * Creates an instance,
     *
     * @param       siteNode                the {@link SiteNode} for which the view is to be rendered
     * @param       view                    the view to render
     * @param       requestLocaleManager    the {@link RequestLocaleManager}
     *
     ******************************************************************************************************************/
    public HtmlTemplateBlogViewController (final @Nonnull SiteNode siteNode,
                                           final @Nonnull HtmlTemplateBlogView view,
                                           final @Nonnull RequestLocaleManager requestLocaleManager)
      {
        super(siteNode, view, requestLocaleManager);
        this.view = view;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void renderPosts (final @Nonnull List<Content> fullPosts,
                                final @Nonnull List<Content> leadinPosts,
                                final @Nonnull List<Content> linkedPosts)
      {
        view.renderPosts(getViewProperties().getProperty(P_TEMPLATE_POSTS_PATH),
                         fullPosts  .stream().map(p -> toAggregate(p, P_FULL_TEXT)).collect(toAggregates("fullPosts")),
                         leadinPosts.stream().map(p -> toAggregate(p, P_LEADIN_TEXT)).collect(toAggregates("leadinPosts")),
                         linkedPosts.stream().map(p -> toAggregate(p, P_LEADIN_TEXT)).collect(toAggregates("linkedPosts")));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void renderTagCloud (final Collection<TagAndCount> tagsAndCount)
      {
        view.renderTagCloud(getViewProperties().getProperty(P_TEMPLATE_TAG_CLOUD_PATH),
                            tagsAndCount.stream().map(tac -> toAggregate(tac)).collect(toAggregates("tags")));
      }

    /*******************************************************************************************************************
     *
     * Transforms a post into an {@link Aggregate}.
     *
     * @param       post                the post
     * @param       textProperty        the property to extract the text from
     * @return                          the {@code Aggregate}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (final @Nonnull Content post, final @Nonnull Key<String> textProperty)
      {
        @SuppressWarnings("squid:S3655")
        final ZonedDateTime dateTime = post.getProperty(DATE_KEYS).get();
        final String id = String.format("nw-%s-blogpost-%s", view.getId(), dateTime.toInstant().toEpochMilli());
        final List<String> tags = post.getProperty(P_TAGS).orElse(emptyList());

        return Aggregate.of(  "title",        post.getProperty(P_TITLE))
                        .with("text" ,        post.getProperty(textProperty))
                        .with("link",         post.getExposedUri().map(this::createLink))
                        .with("id",           id)
                        .with("publishDate",  formatDateTime(dateTime))
                        .with("category",     post.getProperty(P_CATEGORY))
                        .with("tags",         tags.stream()
                                                  .sorted()
                                                  .map(tag -> toAggregate(tag).getMap())
                                                  .collect(toList()));
      }

    /*******************************************************************************************************************
     *
     * Transforms a {@link TagAndCount} into an {@link Aggregate}.
     *
     * @param       tagAndCount         the tag info
     * @return                          the {@code Aggregate}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (final @Nonnull TagAndCount tagAndCount)
      {
        return toAggregate(tagAndCount.tag).with("rank", tagAndCount.rank).with("count", tagAndCount.count);
      }

    /*******************************************************************************************************************
     *
     * Transforms a tag into an {@link Aggregate}.
     *
     * @param       tag                 the tag info
     * @return                          the {@code Aggregate}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Aggregate toAggregate (final @Nonnull String tag)
      {
        return Aggregate.of("name", tag).with("link", createTagLink(tag));
      }
  }
