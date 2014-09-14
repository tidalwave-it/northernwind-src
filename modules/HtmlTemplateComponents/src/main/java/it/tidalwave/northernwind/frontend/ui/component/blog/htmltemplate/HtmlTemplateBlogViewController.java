/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.net.URLEncoder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import java.io.UnsupportedEncodingException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlTemplateBlogViewController extends DefaultBlogViewController
  {
    // FIXME: find a proper name space, possibly merging with other - or defining @ as dynamic properties
    private final static Key<String> PROP_ADD_TITLE = new Key<>("@title");
    private final static Key<String> PROP_ADD_URL = new Key<>("@url");
    private final static Key<String> PROP_ADD_ID = new Key<>("@id");

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

        for (final String html : htmlParts)
          {
            htmlBuilder.append(html);
          }

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

//        final DateTime blogDateTime = getBlogDateTime(post);
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
      throws IOException, NotFoundException
      {
        log.debug("addPost({}, {})", post, addBody);
        final ResourceProperties properties = post.getProperties();
        final StringBuilder htmlBuilder = new StringBuilder();

        final DateTime blogDateTime = properties.getDateTimeProperty(DefaultBlogViewController.DATE_KEYS,
                                                                     DefaultBlogViewController.TIME0);
        final String idPrefix = "nw-" + view.getId() + "-blogpost-" + blogDateTime.toDate().getTime();
        htmlBuilder.append(String.format("<div id='%s' class='nw-blog-post'>%n", idPrefix));
        htmlBuilder.append(String.format("<h3>%s</h3>%n", properties.getProperty(PROPERTY_TITLE)));

        htmlBuilder.append("<div class='nw-blog-post-meta'>");
        renderDate(htmlBuilder, blogDateTime);
        renderCategory(htmlBuilder, post);
        renderTags(htmlBuilder, post);
        renderPermalink(htmlBuilder, post);
        htmlBuilder.append("</div>");

        if (addBody)
          {
            htmlBuilder.append(String.format("<div class='nw-blog-post-content'>%s</div>%n", properties.getProperty(PROPERTY_FULL_TEXT)));

            try
              {
                requestContext.setDynamicNodeProperty(PROP_ADD_ID, properties.getProperty(PROPERTY_ID));
              }
            catch (NotFoundException | IOException e)
              {
                log.debug("Can't set dynamic property " + PROP_ADD_ID, e); // ok, no id
              }

            requestContext.setDynamicNodeProperty(PROP_ADD_TITLE, computeTitle(post));
          }

        htmlBuilder.append(String.format("</div>%n"));
        htmlParts.add(htmlBuilder.toString());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void addTagCloud (final Collection<TagAndCount> tagsAndCount)
      throws IOException, NotFoundException
      {
        log.debug("addTagCloud({})", tagsAndCount);
        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div class='tagCloud'>");

        for (final TagAndCount tagAndCount : tagsAndCount)
          {
            final String tag = tagAndCount.tag;
            final String link = createTagLink(tag);
            htmlBuilder.append(String.format("<a href=\"%s\" class=\"tagCloudItem rank%s\" rel=\"%d\">%s</a>%n",
                                             link, tagAndCount.rank, tagAndCount.count, tag));
          }

        htmlBuilder.append(String.format("</div>%n"));
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
        final StringBuilder buffer = new StringBuilder();
        String separator = "";

        try
          {
            buffer.append(siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE));
            separator = " - ";
          }
        catch (NotFoundException | IOException e)
          {
            // ok, no title
          }

        try
          {
            final String t = properties.getProperty(PROPERTY_TITLE); // before append separator
            buffer.append(separator).append(t);
          }
        catch (NotFoundException | IOException e)
          {
            // ok, no title
          }

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
        try
          {
            final String title = siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE);

            if (!title.trim().equals(""))
              {
                htmlBuilder.append(String.format("<h2>%s</h2>%n", title));
              }
          }
        catch (NotFoundException | IOException e)
          {
            // ok, no title
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the date of the blog post.
     *
     ******************************************************************************************************************/
    /* package */ void renderDate (final @Nonnull StringBuilder htmlBuilder, final @Nonnull DateTime blogDateTime)
      {
        htmlBuilder.append(String.format("<span class='nw-publishDate'>%s</span>%n",
                           getDateTimeFormatter().print(blogDateTime)));
      }

    /*******************************************************************************************************************
     *
     * Renders the permalink of the blog post.
     *
     ******************************************************************************************************************/
    private void renderPermalink (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      throws IOException
      {
        try
          {
            final String link = site.createLink(siteNode.getRelativeUri().appendedWith(post.getExposedUri()));
            htmlBuilder.append(String.format("&nbsp;- <a href='%s'>Permalink</a>%n", link));
            requestContext.setDynamicNodeProperty(PROP_ADD_URL, link);
          }
        catch (NotFoundException e)
          {
            // ok, no link
          }
      }

    /*******************************************************************************************************************
     *
     * Renders a reference link.
     *
     ******************************************************************************************************************/
    private void renderReferenceLink (final @Nonnull StringBuilder htmlBuilder,
                                      final @Nonnull Content post)
      throws IOException
      {
        //        final String idPrefix = "nw-blogpost-" + blogDateTime.toDate().getTime();

        try
          {
            final String link = site.createLink(siteNode.getRelativeUri().appendedWith(post.getExposedUri()));
            final String title = post.getProperties().getProperty(PROPERTY_TITLE);
            htmlBuilder.append(String.format("<li><a href='%s'>%s</a></li>%n", link, title));
          }
        catch (NotFoundException e)
          {
            // ok, no link
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the category of the blog post.
     *
     ******************************************************************************************************************/
    private void renderCategory (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      throws IOException
      {
        try
          {
            htmlBuilder.append(String.format("&nbsp;- <span class='nw-blog-post-category'>Filed under \"%s\"</span>",
                               post.getProperties().getProperty(PROPERTY_CATEGORY)));
          }
        catch (NotFoundException e)
          {
            // ok, no category
          }
      }

    /*******************************************************************************************************************
     *
     * Renders the tags of the blog post.
     *
     ******************************************************************************************************************/
    private void renderTags (final @Nonnull StringBuilder htmlBuilder, final @Nonnull Content post)
      throws IOException
      {
        try
          {
            final List<String> tags = Arrays.asList(post.getProperties().getProperty(PROPERTY_TAGS).split(","));
            Collections.sort(tags);
            final StringBuilder buffer = new StringBuilder();
            String separator = "";

            for (final String tag : tags)
              {
                final String link = createTagLink(tag);
                buffer.append(separator).append(String.format("%n<a class='nw-tag' href='%s'>%s</a>", link, tag));
                separator = ", ";
              }

            htmlBuilder.append(String.format("&nbsp;- <span class='nw-blog-post-tags'>Tagged as %s</span>",
                               buffer.toString()));
          }
        catch (NotFoundException | IOException e)
          {
            // ok, no tags
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String createTagLink (final String tag)
      throws UnsupportedEncodingException
      {
        final String tagLink = TAG_PREFIX + URLEncoder.encode(tag, "UTF-8");
        String link = site.createLink(siteNode.getRelativeUri().appendedWith(tagLink));

        // FIXME: workaround as createLink() doesn't append trailing / if the link contains a dot
        if (!link.endsWith("/"))
          {
            link += "/";
          }

        return link;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private DateTimeFormatter getDateTimeFormatter()
      {
        try
          {
            final String pattern = siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_DATE_FORMAT);
            return ((pattern.length() == 2) ? DateTimeFormat.forStyle(pattern)
                                            : DateTimeFormat.forPattern(pattern)).withLocale(requestLocaleManager.getLocales().get(0));
          }
        catch (NotFoundException | IOException e)
          {
          }

        return requestLocaleManager.getDateTimeFormatter();
      }
  }
