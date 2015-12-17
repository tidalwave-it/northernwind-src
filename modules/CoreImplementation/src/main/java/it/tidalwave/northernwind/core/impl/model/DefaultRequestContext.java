/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;

/***********************************************************************************************************************
 *
 * A default implementation of {@link FilterContext}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultRequestContext implements RequestContext
  {
    @Inject
    private ModelFactory modelFactory;

    private final ThreadLocal<Content> contentHolder = new ThreadLocal<>();

    private final ThreadLocal<SiteNode> nodeHolder = new ThreadLocal<>();

    private final ThreadLocal<ResourceProperties> dynamicNodePropertiesHolder = new ThreadLocal<>();

    @Override @Nonnull
    public ResourceProperties getContentProperties()
      {
        if (contentHolder.get() == null) // FIXME: should never occur
          {
            log.warn("NO CONTENT IN CONTEXT");
//            Thread.dumpStack(); // FIXME
            return modelFactory.createProperties().build();
          }

        return contentHolder.get().getProperties();
      }

    @Override @Nonnull
    public ResourceProperties getNodeProperties()
      {
        if (contentHolder.get() == null) // FIXME: should never occur
          {
            log.warn("NO CONTENT IN CONTEXT");
//            Thread.dumpStack(); // FIXME
            return dynamicNodePropertiesHolder.get();
          }

        return nodeHolder.get().getProperties().merged(dynamicNodePropertiesHolder.get());
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
        dynamicNodePropertiesHolder.set(modelFactory.createProperties().build());
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
        dynamicNodePropertiesHolder.remove();
      }

    @Override
    public void requestReset()
      {
        clearNode();
        clearContent();
      }

    @Override
    public <Type> void setDynamicNodeProperty (final @Nonnull Key<Type> key, final @Nonnull Type value)
      {
        ResourceProperties properties = dynamicNodePropertiesHolder.get();
        dynamicNodePropertiesHolder.set(properties.withProperty(key, value));
      }

    @Override @Nonnull
    public String toString()
      {
        return String.format("RequestContext[content: %s, node: %s]", toString(contentHolder.get()), toString(nodeHolder.get()));
      }

    @Nonnull
    private static String toString (final @CheckForNull Resource resource)
      {
        return (resource == null) ? "null" : resource.getFile().getPath().asString();
      }
  }
