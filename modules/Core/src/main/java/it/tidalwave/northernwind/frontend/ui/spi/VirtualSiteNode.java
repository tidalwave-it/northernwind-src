/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import it.tidalwave.util.As;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Id;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/***********************************************************************************************************************
 *
 * A convenient implementation of {@link SiteNode} for {@link ViewController#findVirtualSiteNodes()}.
 *
 * @see ViewController#findVirtualSiteNodes() 
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class VirtualSiteNode implements SiteNode
  {
    @Nonnull
    private final SiteNode parentSiteNode;

    @Getter @Nonnull
    private final ResourcePath relativeUri;

    @Getter @Nonnull
    private final ResourceProperties properties;

    @Delegate
    private final As asSupport = new AsSupport(this);

    @Override @Nonnull
    public Site getSite()
      {
        return parentSiteNode.getSite();
      }

    // TODO: perhaps the methods below could be implemented by delegating to the first real SiteNode up in the
    // hierarchy.
    @Override
    public Layout getLayout()
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public ResourceFile getFile()
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public ResourceProperties getPropertyGroup (final @Nonnull Id id)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public boolean isPlaceHolder()
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override @Nonnull
    public Finder<SiteNode> findChildren()
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override @Nonnull
    public String toString()
      {
        return "VirtualSiteNode(" + relativeUri + ')';
      }
  }
