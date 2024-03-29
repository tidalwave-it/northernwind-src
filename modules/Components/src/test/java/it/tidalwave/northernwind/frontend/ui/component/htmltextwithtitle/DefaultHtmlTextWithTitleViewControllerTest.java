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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_CONTENT_PATHS;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultHtmlTextWithTitleViewControllerTest
  {
    static class UnderTest extends DefaultHtmlTextWithTitleViewController
      {
        public List<? extends TextWithTitle> contents;

        public UnderTest(final HtmlTextWithTitleView arg0, final SiteNode arg1)
          {
            super(arg0, arg1);
          }

        @Override
        protected void render (@Nonnull final List<? extends TextWithTitle> contents)
          {
            this.contents = contents;
          }
      }

    private final Id viewId = new Id("viewId");

    private SiteNode siteNode;

    private Site site;

    private UnderTest underTest;

    private RenderContext renderContext;

    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        site = createMockSite();
        MockContentSiteFinder.registerTo(site);

        siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of("uri"));
        final var siteNodeProperties = createMockProperties();

        final var viewProperties = createMockProperties();

        when(siteNode.getProperties()).thenReturn(siteNodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        final var request = mock(Request.class);
        final var requestContext = mock(RequestContext.class);
        renderContext = new DefaultRenderContext(request, requestContext);

        final var view = mock(HtmlTextWithTitleView.class);
        when(view.getId()).thenReturn(viewId);

        underTest = new UnderTest(view, siteNode);
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
        final var paths = IntStream.range(0, 10).mapToObj(i -> "/path/content-" + i).collect(toList());
        mockViewProperty(siteNode, viewId, P_CONTENT_PATHS, Optional.of(paths));

        for (var i = 0; i < paths.size(); i++)
          {
            final var properties =
                    site.find(_Content_).withRelativePath(paths.get(i)).result().getProperties();
            when(properties.getProperty(P_TITLE)).thenReturn(Optional.of("Title #" + i));
            when(properties.getProperty(P_FULL_TEXT)).thenReturn(Optional.of(String.format("Full text #%d", i)));
          }

        // when
        underTest.renderView(renderContext);
        // then
        final var text = underTest.contents.stream()
                                           .map(twt -> String.format("%2d %-20s %-20s",
                                                                        twt.level, twt.title.orElse(""), twt.text.orElse("")))
                                           .collect(joining("\n"));
        fileTestHelper.assertFileContents(text.getBytes(UTF_8), "text_with_title.txt");
      }
  }
