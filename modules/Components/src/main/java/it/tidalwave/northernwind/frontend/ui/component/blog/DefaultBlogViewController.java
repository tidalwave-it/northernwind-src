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
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
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
    private static final List<Key<String>> DATE_KEYS = Arrays.asList(PROPERTY_PUBLISHING_DATE, PROPERTY_CREATION_DATE);
    
    @Nonnull @Inject
    private Site site;
    
    @Nonnull @Inject
    protected RequestLocaleManager requestLocaleManager;
    
    @Nonnull
    protected final BlogView view;

    private final SiteNode siteNode;
    
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
    /* package */ void initialize()
      {
        try 
          {
            for (final String relativeUri : siteNode.getProperties(view.getId()).getProperty(PROPERTY_CONTENTS))
              {  
                try
                  {
                    final Content postsFolder = site.find(Content).withRelativeUri(relativeUri).result();

                    for (final Content post : postsFolder.findChildren().results())
                      {
                        try
                          {
                            addPost(post);
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
                catch (NotFoundException e)
                  {
                    log.warn("", e.toString());
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
    protected abstract void addPost (@Nonnull Content post)
      throws IOException, NotFoundException;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected abstract void render();
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected DateTime getBlogDateTime (@Nonnull Content post)
      throws NotFoundException
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
        
        throw new NotFoundException("No available date for /" + post.getFile().getPath());
      }
  }