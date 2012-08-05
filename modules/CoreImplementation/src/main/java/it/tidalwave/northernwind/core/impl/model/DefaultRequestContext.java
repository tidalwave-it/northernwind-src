/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import it.tidalwave.northernwind.core.impl.model.DefaultResourceProperties.PropertyResolver;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A default implementation of {@link FilterContext}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultRequestContext implements RequestContext
  {
    private final ThreadLocal<Content> contentHolder = new ThreadLocal<>();
    
    private final ThreadLocal<SiteNode> nodeHolder = new ThreadLocal<>();

    @Override @Nonnull
    public ResourceProperties getContentProperties() 
      {
        if (contentHolder.get() == null)
          {
            log.info("NO CONTENT IN CONTEXT");
            Thread.dumpStack(); // FIXME
            return DefaultResourceProperties.DEFAULT;
          }

        return contentHolder.get().getProperties();
      }

    @Override @Nonnull
    public ResourceProperties getNodeProperties() 
      {
        return nodeHolder.get().getProperties();
      }

    @Override
    public void setContent (final @Nonnull Content content) 
      {
        contentHolder.set(content);
      }

    @Override
    public void setNode (final @Nonnull SiteNode node) 
      {
        nodeHolder.set(node);
      }

    @Override
    public void clearContent()
      {
        contentHolder.remove();
      }

    @Override
    public void clearNode() 
      {
        nodeHolder.remove();
      }
    
    @Override
    public void requestReset() 
      {
        clearNode();
        clearContent();
      }
    
    @Override @Nonnull
    public String toString()
      {
        return String.format("RequestContext[content: %s, node: %s]", toString(contentHolder.get()), toString(nodeHolder.get()));  
      }
    
    @Nonnull
    private static String toString (final @CheckForNull Resource resource)
      {
        return (resource == null) ? "null" : resource.getFile().getPath();
      }
  }