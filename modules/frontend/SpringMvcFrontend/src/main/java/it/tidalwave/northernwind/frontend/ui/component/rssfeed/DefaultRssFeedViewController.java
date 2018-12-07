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
package it.tidalwave.northernwind.frontend.ui.component.rssfeed;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.time.ZonedDateTime;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Content;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultRssFeedViewController extends DefaultBlogViewController implements RssFeedViewController
  {
    @Nonnull
    private final SiteProvider siteProvider;

    @Nonnull
    private final RssFeedView view;

    private final List<Item> items = new ArrayList<>();

    private final String linkBase;

    private final Channel feed;

    private final ResourceProperties properties;

    public DefaultRssFeedViewController (final @Nonnull RssFeedView view,
                                         final @Nonnull SiteNode siteNode,
                                         final @Nonnull Site site,
                                         final @Nonnull SiteProvider siteProvider)
      {
        super(view, siteNode, site);
        this.siteProvider = siteProvider;
        this.view = view;
        feed = new Channel("rss_2.0");
        properties = siteNode.getPropertyGroup(view.getId());
        linkBase = properties.getProperty(P_LINK).orElse("");
        feed.setTitle(properties.getProperty(P_TITLE).orElse(""));
        feed.setDescription(properties.getProperty(P_DESCRIPTION).orElse(""));
        feed.setLink(linkBase); // FIXME: why not site.createLink()?
        feed.setCopyright(properties.getProperty(P_CREATOR).orElse(""));
      }

    @Override
    protected void addFullPost (final @Nonnull it.tidalwave.northernwind.core.model.Content post)
      {
        final ZonedDateTime blogDateTime = post.getProperties().getDateTimeProperty(DATE_KEYS).orElse(TIME0);
        // FIXME: compute the latest date, which is not necessarily the first
        if (feed.getLastBuildDate() == null)
          {
            feed.setLastBuildDate(Date.from(blogDateTime.toInstant()));
          }

        final ResourceProperties postProperties = post.getProperties();
        final Item item = new Item();
        final Content content = new Content();
        // FIXME: text/xhtml?
        content.setType("text/html"); // FIXME: should use post.getResourceFile().getMimeType()?
        postProperties.getProperty(P_FULL_TEXT).ifPresent(content::setValue);
        item.setTitle(postProperties.getProperty(P_TITLE).orElse(""));
//        item.setAuthor("author " + i); TODO
        item.setPubDate(Date.from(blogDateTime.toInstant()));
        item.setContent(content);

        try
          {
            // FIXME: manipulate through site.createLink()
            final String link = linkBase.replaceAll("/$", "") + post.getExposedUri().orElseThrow(NotFoundException::new).asString() + "/";
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
    protected void addLinkToPost (final @Nonnull it.tidalwave.northernwind.core.model.Content post)
      {
      }

    @Override
    protected void addTagCloud (final Collection<TagAndCount> tagsAndCount)
      {
      }

    @Override
    protected void render()
      throws FeedException
      {
        feed.setGenerator("NorthernWind v" + siteProvider.getVersionString());
        feed.setItems(items);

//        if (!StringUtils.hasText(feed.getEncoding()))
//          {
//            feed.setEncoding("UTF-8");
//          }

        final WireFeedOutput feedOutput = new WireFeedOutput();
        view.setContent(feedOutput.outputString(feed));
      }
  }
