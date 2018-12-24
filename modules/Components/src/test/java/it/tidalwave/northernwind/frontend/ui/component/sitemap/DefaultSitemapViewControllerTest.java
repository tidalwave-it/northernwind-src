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
package it.tidalwave.northernwind.frontend.ui.component.sitemap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.DefaultSitemapViewController.Entry;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultSitemapViewControllerTest
  {
    class UnderTest extends DefaultSitemapViewController
      {
        @Nonnull
        private final List<Entry> entries = new ArrayList<>();

        public UnderTest (final @Nonnull SiteNode node, final @Nonnull SitemapView view)
          {
            super(node, view);
          }

        @Override
        protected void render (final @Nonnull Set<Entry> entries)
          {
            this.entries.addAll(entries);
          }
      }

    private UnderTest underTest;

    private SitemapView view;

    private SiteNode siteNode;

    private Site site;

    private RenderContext context;

    private Id viewId = new Id("viewId");

    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        site = createMockSite();

        final MockNodesForSitemap mockSiteNodes = new MockNodesForSitemap(site);
        final List<SiteNode> nodes = mockSiteNodes.createMockNodes(65, 20, "/path-%02d");

        final SiteFinder<SiteNode> finder = createMockSiteFinder();
        when(finder.results()).thenReturn((List)nodes);
        when(site.find(eq(SiteNode.class))).thenReturn(finder);

        view = mock(SitemapView.class);
        when(view.getId()).thenReturn(viewId);

        siteNode = createMockSiteNode(site);
        final ResourceProperties siteNodeProperties = createMockProperties();
        final ResourceProperties viewProperties = createMockProperties();
        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        context = mock(RenderContext.class);

        underTest = new UnderTest(siteNode, view);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_sitemap()
      throws Exception
      {
        // when
        underTest.renderView(context);
        // then
        final String s = underTest.entries.stream().map(e -> String.format("%-34s %20s %10s %6s",
                                                                           e.getLocation(),
                                                                           e.getLastModification(),
                                                                           e.getChangeFrequency(),
                                                                           e.getPriority()))
                                          .collect(joining("\n"));
        fileTestHelper.assertFileContents(s.getBytes(UTF_8), "sitemap.txt");
      }
  }
