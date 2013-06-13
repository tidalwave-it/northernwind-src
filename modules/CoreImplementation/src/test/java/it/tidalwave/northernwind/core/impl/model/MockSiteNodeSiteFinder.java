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
package it.tidalwave.northernwind.core.impl.model;

import it.tidalwave.northernwind.core.model.ModifiablePath;
import it.tidalwave.northernwind.core.model.SiteFinder.Predicate;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MockSiteNodeSiteFinder extends FinderSupport<SiteNode, DefaultSiteFinder<SiteNode>>
                                    implements SiteFinder<SiteNode>
  {
    private String relativePath;

    private String relativeUri;

    @Override @Nonnull
    public SiteFinder<SiteNode> withRelativePath (final @Nonnull String relativePath)
      {
        this.relativePath = relativePath;
        return this;
      }

    @Override @Nonnull
    public SiteFinder<SiteNode> withRelativeUri (final @Nonnull String relativeUri)
      {
        this.relativeUri = relativeUri;
        return this;
      }

    @Override @Nonnull
    protected List<? extends SiteNode> computeResults()
      {
        final SiteNode content = mock(SiteNode.class);
        when(content.getRelativeUri()).thenReturn(new ModifiablePath("URI-" + relativePath.substring(1)));
        return Arrays.asList(content);
      }

    @Override
    public void doWithResults (final @Nonnull Predicate<SiteNode> predicate)
      {
        throw new UnsupportedOperationException("Not supported.");
      }
  }
