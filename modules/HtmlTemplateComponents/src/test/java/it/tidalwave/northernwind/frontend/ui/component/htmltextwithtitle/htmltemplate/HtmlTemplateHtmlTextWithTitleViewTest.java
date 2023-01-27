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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.htmltemplate;

import java.util.List;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.createMockSite;
import static it.tidalwave.northernwind.core.model.Content.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateHtmlTextWithTitleViewTest
  {
    private final FileTestHelper fileTestHelper = new FileTestHelper(getClass().getSimpleName());

    private HtmlTemplateHtmlTextWithTitleView underTest;

    private Site site;

    private final Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        site = createMockSite();
        MockContentSiteFinder.registerTo(site);
        underTest = new HtmlTemplateHtmlTextWithTitleView(viewId, site);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_posts_with_custom_template()
      throws Exception
      {
        // given
        final var templatePath = ResourcePath.of("/the/template/path");
        final var template = site.find(_Content_).withRelativePath(templatePath).result();
        final var properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("$title$ $text$ $level$"));

        final var aggregates = new Aggregates("content", List.of(
            Aggregate.of("title", "title").with("text", "text").with("level", 2)));

        // when
        underTest.render(Optional.empty(), Optional.of(templatePath), aggregates);
        // then
        assertThat(underTest.asString(UTF_8), is("<div class='nw-viewId'>\ntitle text 2\n\n</div>"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_posts_with_custom_wrapper_template()
      throws Exception
      {
        // given
        final var templatePath = ResourcePath.of("/the/template/path");
        final var template = site.find(_Content_).withRelativePath(templatePath).result();
        final var properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("<div class='my'>\n  $content$\n</div>"));

        final var aggregates = new Aggregates("content", List.of(
            Aggregate.of("title", "title 1").with("text", "text 1").with("level", 2),
            Aggregate.of("title", "title 2").with("text", "text 2").with("level", 2),
            Aggregate.of("title", "title 3").with("text", "text 3").with("level", 2)));
        // when
        underTest.render(Optional.of(templatePath), Optional.empty(), aggregates);
        // then
        fileTestHelper.assertFileContents(underTest.asBytes(UTF_8), "with_wrapper.xhtml");
      }
  }
