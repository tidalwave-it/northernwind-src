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
 * $Id: bdbc5f12c90c51172cf0ec6260f0438c54faa692 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.*;
import static it.tidalwave.util.LocalizedDateTimeFormatters.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: bdbc5f12c90c51172cf0ec6260f0438c54faa692 $
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    // FIXME: find a proper name space, possibly merging with other - or defining @ as dynamic properties
    private final static Key<String> PROP_ADD_TITLE = new Key<>("@title");
    private final static Key<String> PROP_ADD_URL = new Key<>("@url");
    private final static Key<String> PROP_ADD_ID = new Key<>("@id");

    protected final static String DEFAULT_TIMEZONE = "CET";

    private static final String TEMPLATE_MAIN_TITLE = "<h2>%s</h2>%n";
    private static final String TEMPLATE_REFERENCE_TITLE = "<h3>%s</h3>%n";
    private static final String TEMPLATE_DIV_BLOG_POST = "<div id='%s' class='nw-blog-post'>%n";
    private static final String TEMPLATE_FULL_TEXT = "<div class='nw-blog-post-content'>%n%s%n</div>%n";
    private static final String TEMPLATE_PERMALINK = "&nbsp;- <a href='%s'>Permalink</a>%n";
    private static final String TEMPLATE_REFERENCE_LINK = "<li><a href='%s'>%s</a></li>%n";
    private static final String TEMPLATE_CATEGORY = "&nbsp;- <span class='nw-blog-post-category'>Filed under \"%s\"</span>";
    private static final String TEMPLATE_DATE = "<span class='nw-publishDate'>%s</span>%n";
    private static final String TEMPLATE_TAG_CLOUD_LINK = "<a href=\"%s\" class=\"tagCloudItem rank%s\" rel=\"%d\">%s</a>%n";
    private static final String TEMPLATE_TAG_LINK = "%n<a class='nw-tag' href='%s'>%s</a>";

    private final static Map<String, Function<Locale, DateTimeFormatter>> DATETIME_FORMATTER_MAP_BY_STYLE = new HashMap<>();

    static
      {
        DATETIME_FORMATTER_MAP_BY_STYLE.put("S-", locale -> getDateTimeFormatterFor(FormatStyle.SHORT, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("M-", locale -> getDateTimeFormatterFor(FormatStyle.MEDIUM, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("L-", locale -> getDateTimeFormatterFor(FormatStyle.LONG, locale));
        DATETIME_FORMATTER_MAP_BY_STYLE.put("F-", locale -> getDateTimeFormatterFor(FormatStyle.FULL, locale));
      }

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    @Nonnull
    private final RequestLocaleManager requestLocaleManager;

    private boolean referencesRendered;

    private final List<String> htmlParts = new ArrayList<>();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public HtmlTemplateBlogViewController (final @Nonnull BlogView view,
                                           final @Nonnull SiteNode siteNode,
                                           final @Nonnull Site site,
                                           final @Nonnull RequestHolder requestHolder,
                                           final @Nonnull RequestContext requestContext,
                                           final @Nonnull RequestLocaleManager requestLocaleManager)
      {
        super(view, siteNode, site, requestHolder, requestContext);
        this.siteNode = siteNode;
        this.site = site;
        this.requestLocaleManager = requestLocaleManager;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addFullPost (final @Nonnull Content post)
      throws IOException, NotFoundException
      {
        log.debug("addFullPost()");
        addPost(post, true);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addLeadInPost (final @Nonnull Content post)
      throws IOException, NotFoundException
      {
        log.debug("addLeadInPost()");
        addPost(post, false);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void render()
      {
        final StringBuilder htmlBuilder = new StringBuilder();
        renderMainTitle(htmlBuilder);

        htmlParts.stream().forEach(html -> htmlBuilder.append(html));

        if (referencesRendered)
          {
            htmlBuilder.append("</ul>\n");
          }

        ((HtmlTemplateBlogView)view).addComponent(new HtmlHolder(htmlBuilder.toString()));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addReference (final @Nonnull Content post)
      throws IOException, NotFoundException
      {
        log.debug("addReference()");

        final StringBuilder htmlBuilder = new StringBuilder();

        if (!referencesRendered)
          {
            htmlBuilder.append("<ul>\n");
            referencesRendered = true;
          }

        renderReferenceLink(htmlBuilder, post);
        htmlParts.add(htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    private void addPost (final @Nonnull Content post, final boolean addBody)
      {
        log.debug("addPost({}, {})", post, addBody);

        final ResourceProperties properties = post.getProperties();
        final StringBuilder htmlBuilder = new StringBuilder();

        final ZonedDateTime blogDateTime = properties.getDateTimeProperty(DefaultBlogViewController.DATE_KEYS).orElse(DefaultBlogViewController.TIME0);
        final String idPrefix = "nw-" + view.getId() + "-blogpost-" + blogDateTime.toInstant().toEpochMilli();
        htmlBuilder.append(String.format(TEMPLATE_DIV_BLOG_POST, idPrefix));

        append(htmlBuilder, TEMPLATE_REFERENCE_TITLE, properties.getProperty(PROPERTY_TITLE));

        htmlBuilder.append("<div class='nw-blog-post-meta'>\n");
        renderDate(htmlBuilder, blogDateTime);
        renderCategory(htmlBuilder, post);
        renderTags(htmlBuilder, post);
        renderPermalink(htmlBuilder, post);
        htmlBuilder.append("</div>\n");

        if (addBody)
          {
            append(htmlBuilder, TEMPLATE_FULL_TEXT, properties.getProperty(PROPERTY_FULL_TEXT));
            properties.getProperty(PROPERTY_ID).ifPresent(id -> requestContext.setDynamicNodeProperty(PROP_ADD_ID, id));
            requestContext.setDynamicNodeProperty(PROP_ADD_TITLE, computeTitle(post));
          }

        htmlBuilder.append("</div>\n");
        htmlParts.add(htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addTagCloud (final Collection<TagAndCount> tagsAndCount)
      {
        log.debug("addTagCloud({})", tagsAndCount);

        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div class='tagCloud'>\n");

        tagsAndCount.stream().forEach(tagAndCount ->
          {
            final String tag = tagAndCount.tag;
            final String link = createTagLink(tag);
            htmlBuilder.append(String.format(TEMPLATE_TAG_CLOUD_LINK, link, tagAndCount.rank, tagAndCount.count, tag));
          });

        htmlBuilder.append("</div>\n");
        htmlParts.add(htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeTitle (final @Nonnull Content post)
      {
        final ResourceProperties properties = post.getProperties();
        final Optional<String> prefix    = siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE);
        final Optional<String> title     = properties.getProperty(PROPERTY_TITLE);
        final Optional<String> separator = prefix.flatMap(p -> title.map(s -> " - "));

        final StringBuilder buffer = new StringBuilder();
        prefix.ifPresent(buffer::append);
        separator.ifPresent(buffer::append);
        title.ifPresent(buffer::append);

        return buffer.toString();
      }

    /*******************************************************************************************************************
     *
     * Renders the general title of the blog.
     *
     * FIXME: should use computeTitle(), but we don't have the post.
     *
     ******************************************************************************************************************/
    /* package */ void renderMainTitle (final @Nonnull StringBuilder htmlBuilder)
      {
        final Optional<String> mainTitle = siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE)
                .map(String::trim)
                .flatMap(this::filterEmptyString);
        append(htmlBuilder, TEMPLATE_MAIN_TITLE, mainTitle);
      }

    /*******************************************************************************************************************
     *
     * Renders the date of the blog post.
     *
     ******************************************************************************************************************/
    /* package */ void renderDate (final @Nonnull StringBuilder htmlBuilder, final @Nonnull ZonedDateTime dateTime)
      {
        final Optional<String> dt = Optional.of(dateTime.format(findDateTimeFormatter()));
        append(htmlBuilder, TEMPLATE_DATE, dt);
      }

    /*******************************************************************************************************************
     *
     * Renders the permalink of the blog post.
     *
     ******************************************************************************************************************/
    private void renderPermalink (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      {
        final Optional<String> link  = post.getExposedUri().map(this::createLink);
        append(htmlBuilder, TEMPLATE_PERMALINK, link);
        link.ifPresent(l -> requestContext.setDynamicNodeProperty(PROP_ADD_URL, l));
      }

    /*******************************************************************************************************************
     *
     * Renders a reference link.
     *
     ******************************************************************************************************************/
    private void renderReferenceLink (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      {
        //        final String idPrefix = "nw-blogpost-" + blogDateTime.toDate().getTime();

        final Optional<String> title = post.getProperty(PROPERTY_TITLE);
        final Optional<String> link  = post.getExposedUri().map(this::createLink);
        append(htmlBuilder, TEMPLATE_REFERENCE_LINK, link, title);
      }

    /*******************************************************************************************************************
     *
     * Renders the category of the blog post.
     *
     ******************************************************************************************************************/
    private void renderCategory (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      {
        final Optional<String> category = post.getProperty(PROPERTY_CATEGORY);
        append(htmlBuilder, TEMPLATE_CATEGORY, category);
      }

    /*******************************************************************************************************************
     *
     * Renders the tags of the blog post.
     *
     ******************************************************************************************************************/
    private void renderTags (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      {
        final String tags = post.getProperty(PROPERTY_TAGS)
            .map(s -> Stream.of(s.split(",")))
            .orElse(Stream.empty())
            .sorted()
            .map(tag -> String.format(TEMPLATE_TAG_LINK, createTagLink(tag), tag))
            .collect(joining(", "));
        htmlBuilder.append(String.format("&nbsp;- <span class='nw-blog-post-tags'>Tagged as %s</span>", tags));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String createTagLink (final String tag)
      {
        try
          {
            final String tagLink = TAG_PREFIX + URLEncoder.encode(tag, "UTF-8");
            // FIXME: refactor with ResourcePath
            String link = site.createLink(siteNode.getRelativeUri().appendedWith(tagLink));

            // FIXME: workaround as createLink() doesn't append trailing / if the link contains a dot
            if (!link.endsWith("/"))
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
     * Returns the proper {@link DateTimeFormatter}. It is build from an explicit pattern, if defined in the current
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
        final ResourceProperties properties = siteNode.getPropertyGroup(view.getId());
        final DateTimeFormatter dtf = properties.getProperty(PROPERTY_DATE_FORMAT)
            .map(s -> s.replaceAll("EEEEE+", "EEEE"))
            .map(s -> s.replaceAll("MMMMM+", "MMMM"))
            .map(p -> (((p.length() == 2) ? DATETIME_FORMATTER_MAP_BY_STYLE.get(p).apply(locale)
                                          : DateTimeFormatter.ofPattern(p)).withLocale(locale)))
            .orElse(requestLocaleManager.getDateTimeFormatter());

        final String zoneId = properties.getProperty(PROPERTY_TIME_ZONE).orElse(DEFAULT_TIMEZONE);
        return dtf.withZone(ZoneId.of(zoneId));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String createLink (final @Nonnull ResourcePath path)
      {
        return site.createLink(siteNode.getRelativeUri().appendedWith(path));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static void append (final @Nonnull StringBuilder builder,
                                final @Nonnull String template,
                                final @Nonnull Optional<String> ... optionalStrings)
      {
        if (Stream.of(optionalStrings).allMatch(Optional::isPresent))
          {
            final Object[] strings = Stream.of(optionalStrings).map(Optional::get).collect(toList()).toArray();
            builder.append(String.format(template, strings));
          }
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
