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
package it.tidalwave.northernwind.frontend.ui.component.blog;

import javax.annotation.PostConstruct;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import org.joda.time.DateTime;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
  {
    public static final List<Key<String>> DATE_KEYS = Arrays.asList(PROPERTY_PUBLISHING_DATE, PROPERTY_CREATION_DATE);

    public static final DateTime TIME0 = new DateTime(0);

    private final Comparator<Content> REVERSE_DATE_COMPARATOR = new Comparator<Content>()
      {
        @Override
        public int compare (final @Nonnull Content post1, final @Nonnull Content post2)
          {
            final DateTime dateTime1 = post1.getProperties().getDateTimeProperty(DATE_KEYS, TIME0);
            final DateTime dateTime2 = post2.getProperties().getDateTimeProperty(DATE_KEYS, TIME0);
            return dateTime2.compareTo(dateTime1);
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
                final List<SiteNode> results = new ArrayList<>();

                try
                  {
                    final ResourceProperties componentProperties = siteNode.getPropertyGroup(view.getId());

                    for (final Content post : findAllPosts(componentProperties))
                      {
                        try
                          {
                            final ResourcePath relativeUri = siteNode.getRelativeUri().appendedWith(post.getExposedUri());
                            results.add(new ChildSiteNode(siteNode, relativeUri, post.getProperties()));
                          }
                        catch (NotFoundException | IOException e)
                          {
                            log.warn("", e);
                          }
                      }
                  }
                catch (NotFoundException | IOException e)
                  {
                    log.warn("", e);
                  }

                log.info(">>>> returning: {}", results);

                return results;
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
        if (isCalledBySitemapController())
          {
            return;
          }

        try
          {
            final ResourceProperties componentProperties = siteNode.getPropertyGroup(view.getId());
            final int maxFullItems = componentProperties.getIntProperty(PROPERTY_MAX_FULL_ITEMS, 99);
            final int maxLeadinItems = componentProperties.getIntProperty(PROPERTY_MAX_LEADIN_ITEMS, 99);
            final int maxItems = componentProperties.getIntProperty(PROPERTY_MAX_ITEMS, 99);

            log.debug(">>>> initializing controller for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {}",
                      view.getId(), maxFullItems, maxLeadinItems, maxItems);

            int currentItem = 0;

            for (final Content post : findPostsInReverseDateOrder(componentProperties))
              {
                try
                  {
                    log.debug(">>>>>>> processing blog item #{}: {}", currentItem, post);
                    post.getProperties().getProperty(PROPERTY_TITLE); // Skip folders used for categories

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
                catch (NotFoundException e)
                  {
                    log.warn("{}", e.toString());
                  }
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }

            render();
          }
        catch (NotFoundException e)
          {
            log.warn("{}", e.toString());
          }
        catch (IOException e)
          {
            log.warn("", e);
          }
      }

    /*******************************************************************************************************************
     *
     * Finds all the posts.
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<Content> findAllPosts (final @Nonnull ResourceProperties siteNodeProperties)
      throws NotFoundException, IOException
      {
        final List<Content> allPosts = new ArrayList<>();

        for (final String relativePath : siteNodeProperties.getProperty(PROPERTY_CONTENTS))
          {
            final Content postsFolder = site.find(Content).withRelativePath(relativePath).result();
            allPosts.addAll(postsFolder.findChildren().results());
          }

        log.debug(">>>> all posts: {}", allPosts.size());

        return allPosts;
      }

    /*******************************************************************************************************************
     *
     * FIXME: works, but it's really cumbersome
     *
     ******************************************************************************************************************/
    // TODO: embed the sort by reverse date in the finder
    @Nonnull
    private List<Content> findPostsInReverseDateOrder (final @Nonnull ResourceProperties siteNodeProperties)
      throws IOException, NotFoundException, HttpStatusException
      {
        String pathParams = requestHolder.get().getPathParams(siteNode);
        pathParams = pathParams.replace("/", "");
        log.debug(">>>> pathParams: {}", pathParams);

        final boolean index = siteNodeProperties.getBooleanProperty(PROPERTY_INDEX, false);
        final List<Content> allPosts = findAllPosts(siteNodeProperties);
        final List<Content> posts = new ArrayList<>();

        try
          {
            if ("".equals(pathParams))
              {
                throw new NotFoundException();
              }

            posts.add(findPostByExposedUri(allPosts, new ResourcePath(pathParams)));
            log.debug(">>>> found a single post matching exposed Uri");

            if (index) // pathParams matches an exposedUri; thus it's not a category, so an index wants all
              {
                log.debug(">>>> we're an index, adding all");
                posts.clear();
                posts.addAll(allPosts);
                throw new NotFoundException();
              }
          }
        catch (NotFoundException e) // pathParams doesn't match an exposedUri; it will eventually match a category
          {
            log.debug(">>>> now filtering by category...");

            for (final Content post : allPosts)
              {
                try
                  {
                    if (pathParams.equals("")
                        || pathParams.equals(post.getProperties().getProperty(PROPERTY_CATEGORY, "---")))
                      {
                        posts.add(post);
                      }
                  }
                catch (IOException e2)
                  {
                    log.warn("", e2);
                  }
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
     *
     ******************************************************************************************************************/
    @Nonnull
    private Content findPostByExposedUri (final List<Content> allPosts, final @Nonnull ResourcePath exposedUri)
      throws NotFoundException, IOException
      {
        for (final Content post : allPosts)
          {
            try
              {
                if (exposedUri.equals(post.getExposedUri()))
                  {
                    return post;
                  }
              }
            catch (NotFoundException e)
              {
                log.warn("{}", e.toString());
              }
            catch (IOException e)
              {
                log.warn("", e);
              }
          }

        throw new NotFoundException("Blog post with exposedUri=" + exposedUri.asString());
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
    protected abstract void render()
      throws Exception;

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
