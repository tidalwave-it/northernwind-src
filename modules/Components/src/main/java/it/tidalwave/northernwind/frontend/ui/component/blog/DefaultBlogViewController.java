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
import java.util.List;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.Content;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public abstract class DefaultBlogViewController implements BlogViewController
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
        
        try 
          {
            setPosts(siteNode.getProperties(viewId).getProperty(PROPERTY_CONTENTS));
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
    
    private void setPosts (final @Nonnull List<String> relativeUris) 
      {
        log.debug("setPosts({})", relativeUris);
        
        for (final String relativeUri : relativeUris)
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

    protected abstract void addPost (@Nonnull Content post)
      throws IOException, NotFoundException;

    protected abstract void render();
  }
