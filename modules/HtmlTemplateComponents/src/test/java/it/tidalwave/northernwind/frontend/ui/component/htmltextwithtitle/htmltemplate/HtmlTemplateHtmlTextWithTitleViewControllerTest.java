/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.htmltemplate;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.*;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.DefaultHtmlTextWithTitleViewController.TextWithTitle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import static java.util.stream.Collectors.toList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateHtmlTextWithTitleViewControllerTest
  {
    private final Id viewId = new Id("viewId");

    private HtmlTemplateHtmlTextWithTitleView view;

    private HtmlTemplateHtmlTextWithTitleViewController underTest;

    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        final Site site = createMockSite();

        final SiteNode siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("uri"));
        final ResourceProperties siteNodeProperties = createMockProperties();

        final ResourceProperties viewProperties = createMockProperties();

        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        final Request request = mock(Request.class);
        final RequestContext requestContext = mock(RequestContext.class);
        final RenderContext renderContext = new DefaultRenderContext(request, requestContext);

        view = new HtmlTemplateHtmlTextWithTitleView(viewId, site);

        underTest = new HtmlTemplateHtmlTextWithTitleViewController(view, siteNode);
        underTest.initialize();
        underTest.prepareRendering(renderContext);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render()
      throws Exception
      {
        // given
        final Random rnd = new Random(43);
        final List<TextWithTitle> twts = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new TextWithTitle(rnd.nextDouble() < 0.3 ? Optional.empty() : Optional.of("Title #" + i),
                                                 Optional.of("<p>text " + i + "</p>"),
                                                 i))
                .collect(toList());
        // when
        underTest.render(twts);
        // then
        fileTestHelper.assertFileContents(view.asBytes(UTF_8), "text_with_title.xhtml");
      }
  }
