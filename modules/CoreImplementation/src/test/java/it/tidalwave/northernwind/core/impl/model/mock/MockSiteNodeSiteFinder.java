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
package it.tidalwave.northernwind.core.impl.model.mock;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import it.tidalwave.util.Finder8Support;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
import static org.mockito.Mockito.*;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;

/***********************************************************************************************************************
 *
 * A mock implementation of {@SiteFinder<SiteNode>} which must be called with
 * {@link #withRelativePath(java.lang.String)}. The typical initialisation is:
 *
 * <pre>
        Site site = mock(Site.class);
        MockSiteNodeSiteFinder.registerTo(site);
 * </pre>
 *
 * It will return mock {@link SiteNode} instances for any relative path that doesn't contain {@code "inexistent"}. It's
 * guaranteed that the same mocked instance is always returned for any path, thus they can be stubbed which code like
 * this:
 *
 * <pre>
 *      // given
 *      SiteNode siteNode = site.find(SiteNode).withRelativePath("/test/path").result();
 *      when(siteNode.someMethod()).thenReturn(...); // stub the content
 *      ...
 *      // when
 *      //   run some method that retrieves a SiteNode mapped to /test/path
 * </pre>
 *
 * Mocked instance of {@code SiteNode} are also bound to mocked {@code ResourceProperties} instances, so the following
 * code is valid too:
 *
 * <pre>
 *      // given
 *      SiteNode siteNode = site.find(SiteNode).withRelativePath("/test/path").result();
 *      when(siteNode.getProperties().getProperty(eq(...))).thenReturn("some value");
 * </pre>
 *
 * Note: this mock has got an implementation that is too complex. But this project is used on didactic purposes too, and
 * it makes sense to have some imperfect stuff from the real world too.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class MockSiteNodeSiteFinder extends Finder8Support<SiteNode, SiteFinder<SiteNode>>
                                    implements SiteFinder<SiteNode>
  {
    private final static long serialVersionUID = 1L;

    private final Site site;

    private final String relativePath;

    private final String relativeUri;

    // This makes it sure that different runs with different mocked Sites use different data
    private final static Map<Site, Map<String, SiteNode>> SITE_CACHE = new IdentityHashMap<>();

    public static void registerTo (final @Nonnull Site site)
      {
        when(site.find(eq(SiteNode))).thenReturn(new MockSiteNodeSiteFinder(site));
      }

    private MockSiteNodeSiteFinder (final @Nonnull Site site)
      {
        this.site         = site;
        this.relativePath = null;
        this.relativeUri  = null;
      }

    public MockSiteNodeSiteFinder (final @Nonnull MockSiteNodeSiteFinder other, final @Nonnull Object override)
      {
        super(other, override);
        final MockSiteNodeSiteFinder source = getSource(MockSiteNodeSiteFinder.class, other, override);
        this.site         = source.site;
        this.relativePath = source.relativePath;
        this.relativeUri  = source.relativeUri;
      }

    @Override @Nonnull
    public SiteFinder<SiteNode> withRelativePath (final @Nonnull String relativePath)
      {
        return clone(new MockSiteNodeSiteFinder(site, relativePath, relativeUri));
      }

    @Override @Nonnull
    public SiteFinder<SiteNode> withRelativeUri (final @Nonnull String relativeUri)
      {
        return clone(new MockSiteNodeSiteFinder(site, relativePath, relativeUri));
      }

    @Override @Nonnull
    protected List<? extends SiteNode> computeResults()
      {
        assert relativePath != null : "relativePath is null";

        if (relativePath.contains("inexistent"))
          {
            return Collections.emptyList();
          }

        final Map<String, SiteNode> nodeMapByRelativePath = SITE_CACHE.computeIfAbsent(site, __ -> new HashMap<>());
        return Arrays.asList(nodeMapByRelativePath.computeIfAbsent(relativePath, this::createMockSiteNodeWithPath));
      }

    @Nonnull
    private SiteNode createMockSiteNodeWithPath (final @Nonnull String relativePath)
      {
        final SiteNode siteNode = createMockSiteNode();
        final ResourceProperties properties = createMockProperties();
        when(siteNode.getRelativeUri()).thenReturn(new ResourcePath("URI-" + relativePath.substring(1)));
        when(siteNode.getProperties()).thenReturn(properties);

        return siteNode;
      }
  }
