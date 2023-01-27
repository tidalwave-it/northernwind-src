/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.sitemap.htmltemplate;

import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.MockNodesForSitemap;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateSitemapViewControllerTest
  {
    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    private HtmlTemplateSitemapViewController underTest;

    private HtmlTemplateSitemapView view;

    private final Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        final var site = createMockSite();

        final var mockSiteNodes = new MockNodesForSitemap(site);
        final var nodes = mockSiteNodes.createMockNodes(65, 20, "/path-%02d");

        final SiteFinder<SiteNode> finder = createMockSiteFinder();
        when(finder.results()).thenReturn(nodes);
        when(site.find(eq(SiteNode.class))).thenReturn(finder);

        view = new HtmlTemplateSitemapView(viewId, site);

        final var siteNode = createMockSiteNode(site);
        final var siteNodeProperties = createMockProperties();
        final var viewProperties = createMockProperties();
        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("blog"));

        underTest = new HtmlTemplateSitemapViewController(siteNode, view);
        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_sitemap()
      throws Exception
      {
        // given
        final var context = mock(RenderContext.class);
        // when
        underTest.renderView(context);
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), "sitemap.xml");
      }
  }
