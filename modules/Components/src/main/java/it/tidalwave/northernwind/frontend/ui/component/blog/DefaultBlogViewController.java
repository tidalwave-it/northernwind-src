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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView.BlogPost;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class DefaultBlogViewController implements BlogViewController
  {
    @Nonnull @Inject
    private Site site;
    
    @Nonnull
    protected final BlogView view;

    /*******************************************************************************************************************
     *
     * @param  view              the related view
     * @param  viewId            the id of the view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultBlogViewController (final @Nonnull BlogView view, 
                                      final @Nonnull Id viewId, 
                                      final @Nonnull SiteNode siteNode) 
    {
        this.view = view;
        final String uris = "/content/document/Blog"; // FIXME siteNode.getProperty(PROP_CONTENT);
        setPosts(Arrays.asList(uris.split(",")), viewId);
      }
    
    private void setPosts (final @Nonnull List<String> relativeUris, final @Nonnull Id viewId) 
      {
        log.debug("setPosts({})", relativeUris);
        
        for (final String relativeUri : relativeUris)
          {  
            try
              {
                // FIXME: should be fixed in the Infoglue importer
                final String fixedUri = "/" + relativeUri.trim().replaceAll("/content/document/Mobile", "").replaceAll("/content/document/", "");
                final Content postRoot = site.find(Content).withRelativeUri(fixedUri).result();
                
                for (final Content post : postRoot.findChildren().results())
                  {
                    try
                      {
                        final ResourceProperties properties = post.getProperties(viewId);
                        view.addPost(new BlogPost(properties.getProperty(PROPERTY_TITLE),     
                                                  properties.getProperty(PROPERTY_FULL_TEXT)));                
                      }
                    catch (NotFoundException e)
                      {
                        log.warn("", e);
                      }
                    catch (IOException e)
                      {
                        log.warn("", e);
                      }
                  }            
              }
            catch (NotFoundException e)
              {
                log.warn("", e);
              }
          }
      }
  }
