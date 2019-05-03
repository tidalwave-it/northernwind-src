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
package it.tidalwave.northernwind.frontend.ui.component.sitemap.htmltemplate;

import java.util.List;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.*;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.MockNodesForSitemap;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
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

    private SiteNode siteNode;

    private Site site;

    private Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        site = createMockSite();

        final MockNodesForSitemap mockSiteNodes = new MockNodesForSitemap(site);
        final List<SiteNode> nodes = mockSiteNodes.createMockNodes(65, 20, "/path-%02d");

        final SiteFinder<SiteNode> finder = createMockSiteFinder();
        when(finder.results()).thenReturn((List)nodes);
        when(site.find(eq(SiteNode.class))).thenReturn(finder);

        view = new HtmlTemplateSitemapView(viewId, site);

        siteNode = createMockSiteNode(site);
        final ResourceProperties siteNodeProperties = createMockProperties();
        final ResourceProperties viewProperties = createMockProperties();
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
        final RenderContext context = mock(RenderContext.class);
        // when
        underTest.renderView(context);
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), "sitemap.xml");
      }
  }
