/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.rssfeed;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.joda.time.DateTime;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Content;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.Properties;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultRssFeedViewController extends DefaultBlogViewController implements RssFeedViewController
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;

    @Nonnull
    private final RssFeedView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    private final List<Item> items = new ArrayList<>();

    private final String linkBase;

    private final Channel feed;

    private final ResourceProperties properties;

    public DefaultRssFeedViewController (final @Nonnull RssFeedView view,
                                         final @Nonnull SiteNode siteNode,
                                         final @Nonnull Site site,
                                         final @Nonnull RequestLocaleManager requestLocaleManager,
                                         final @Nonnull RequestHolder requestHolder,
                                         final @Nonnull RequestContext requestContext)
      throws NotFoundException, IOException
      {
        super(view, siteNode, site, requestHolder, requestContext);
        this.view = view;
        this.siteNode = siteNode;
        this.site = site;
        feed = new Channel("rss_2.0");
        properties = siteNode.getPropertyGroup(view.getId());
        linkBase = properties.getProperty(PROPERTY_LINK, "");
        feed.setTitle(properties.getProperty(PROPERTY_TITLE, ""));
        feed.setDescription(properties.getProperty(PROPERTY_DESCRIPTION, ""));
        feed.setLink(linkBase); // FIXME: why not site.createLink()?
        feed.setCopyright(properties.getProperty(PROPERTY_CREATOR, ""));
      }

    @Override
    protected void addFullPost (final @Nonnull it.tidalwave.northernwind.core.model.Content post)
      throws IOException, NotFoundException
      {
        final DateTime blogDateTime = getBlogDateTime(post);

        if (feed.getLastBuildDate() == null)
          {
            feed.setLastBuildDate(blogDateTime.toDate());
          }

        final ResourceProperties postProperties = post.getProperties();
        final Item item = new Item();
        final Content content = new Content();
        content.setType("text/html"); // FIXME: should use post.getResourceFile().getMimeType()?
        content.setValue(postProperties.getProperty(Properties.PROPERTY_FULL_TEXT));
        item.setTitle(postProperties.getProperty(PROPERTY_TITLE, ""));
//        item.setAuthor("author " + i); TODO
        item.setPubDate(blogDateTime.toDate());
        item.setContent(content);

        try
          {
            final String link = site.createLink(post.getExposedUri());
//            final String link = linkBase + getExposedUri(post).asString() + "/";
            final Guid guid = new Guid();
            guid.setPermaLink(true);
            guid.setValue(link);
            item.setGuid(guid);
            item.setLink(link);
          }
        catch (NotFoundException e)
          {
            // ok. no link
          }

        items.add(item);
      }


    @Override
    protected void addLeadInPost (final @Nonnull it.tidalwave.northernwind.core.model.Content post)
      {
      }

    @Override
    protected void addReference (final @Nonnull it.tidalwave.northernwind.core.model.Content post)
      {
      }

    @Override
    protected void render()
      throws IllegalArgumentException, FeedException, NotFoundException, IOException
      {
        feed.setGenerator("NorthernWind v" + siteProvider.get().getVersionString());
        feed.setItems(items);

//        if (!StringUtils.hasText(feed.getEncoding()))
//          {
//            feed.setEncoding("UTF-8");
//          }

        final WireFeedOutput feedOutput = new WireFeedOutput();
        view.setContent(feedOutput.outputString(feed));
      }
  }
