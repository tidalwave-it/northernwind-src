/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.blog;

import javax.annotation.PostConstruct;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
  {
    private final Comparator<Content> REVERSE_DATE_COMPARATOR = new Comparator<Content>() 
      {
        @Override
        public int compare (final @Nonnull Content post1, final @Nonnull Content post2) 
          {
            final DateTime dateTime1 = getBlogDateTime(post1);
            final DateTime dateTime2 = getBlogDateTime(post2);
            return -dateTime1.compareTo(dateTime2);
          }
      };
    
    private static final List<Key<String>> DATE_KEYS = Arrays.asList(PROPERTY_PUBLISHING_DATE, PROPERTY_CREATION_DATE);
    
    @Nonnull
    protected final BlogView view;

    @Nonnull
    private final SiteNode siteNode;
    
    @Inject @Nonnull
    private Site site;
    
    @Inject @Nonnull
    protected RequestLocaleManager requestLocaleManager;
    
    @Inject @Nonnull
    private RequestHolder requestHolder;

    /*******************************************************************************************************************
     *
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultBlogViewController (final @Nonnull BlogView view, final @Nonnull SiteNode siteNode) 
      {
        this.view = view;
        this.siteNode = siteNode;
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
        try 
          {
            final ResourceProperties componentProperties = siteNode.getPropertyGroup(view.getId());
            final int maxFullItems = Integer.parseInt(componentProperties.getProperty(PROPERTY_MAX_FULL_ITEMS, "99"));
            final int maxLeadinItems = Integer.parseInt(componentProperties.getProperty(PROPERTY_MAX_LEADIN_ITEMS, "99"));
            final int maxItems = Integer.parseInt(componentProperties.getProperty(PROPERTY_MAX_ITEMS, "99"));
            
            log.debug(">>>> initializing controller for {}: maxFullItems: {}, maxLeadinItems: {}, maxItems: {}",
                          new Object[]{ view.getId(), maxFullItems, maxLeadinItems, maxItems });
    
            int currentItem = 0;
            
            for (final Content post : findPostsInReverseDateOrder(componentProperties))
              {
                try
                  {
                    log.debug(">>>>>>> processing blog item #{}: {}", currentItem, post);
                    post.getProperties().getProperty(PROPERTY_FULL_TEXT); // Probe existence of fullText

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
        final boolean filterByPathParams = Boolean.parseBoolean(siteNodeProperties.getProperty(PROPERTY_FILTER_BY_PATH_PARAMS, "false"));
        final boolean filtering = filterByPathParams && !"".equals(pathParams);
        
        final List<Content> posts = new ArrayList<Content>();
        
        for (final String relativePath : siteNodeProperties.getProperty(PROPERTY_CONTENTS))
          {  
            final Content postsFolder = site.find(Content).withRelativePath(relativePath).result();

            for (final Content post : postsFolder.findChildren().results())
              {
                try
                  {
                    if (!filtering
                        || pathParams.equals(getExposedUri(post))
                        || pathParams.equals(post.getProperties().getProperty(PROPERTY_CATEGORY, "---")))
                      {
                        posts.add(post);   
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
          }
        
        if (filtering && posts.isEmpty())
          {
            throw new HttpStatusException(404);
          }
      
        Collections.sort(posts, REVERSE_DATE_COMPARATOR);
        
        return posts;
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
    @Nonnull
    protected String getExposedUri (final @Nonnull Content post) 
      throws IOException, NotFoundException 
      {
        try 
          {
            return post.getProperties().getProperty(SiteNode.PROPERTY_EXPOSED_URI);
          }
        catch (NotFoundException e) 
          {
            return post.getExposedUri();
          } 
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected DateTime getBlogDateTime (final @Nonnull Content post)
      {
        final ResourceProperties properties = post.getProperties();
        final DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
        
        for (final Key<String> dateTimeKey : DATE_KEYS)
          {
            try
              {
                return isoFormatter.parseDateTime(properties.getProperty(dateTimeKey));   
              }
            catch (NotFoundException e)
              {
              }
            catch (IOException e)
              {
                log.warn("", e);
              }
          }
   
        return new DateTime(0);
      }
  }
