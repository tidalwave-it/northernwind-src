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

import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import it.tidalwave.northernwind.util.test.FileTestHelper;
import static java.util.Arrays.asList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

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
      throws Exception
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
        final ResourcePath templatePath = ResourcePath.of("/the/template/path");
        final Content template = site.find(_Content_).withRelativePath(templatePath).result();
        final ResourceProperties properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("$title$ $text$ $level$"));

        final Aggregates aggregates = new Aggregates("content", asList(
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
        final ResourcePath templatePath = ResourcePath.of("/the/template/path");
        final Content template = site.find(_Content_).withRelativePath(templatePath).result();
        final ResourceProperties properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("<div class='my'>\n  $content$\n</div>"));

        final Aggregates aggregates = new Aggregates("content", asList(
            Aggregate.of("title", "title 1").with("text", "text 1").with("level", 2),
            Aggregate.of("title", "title 2").with("text", "text 2").with("level", 2),
            Aggregate.of("title", "title 3").with("text", "text 3").with("level", 2)));
        // when
        underTest.render(Optional.of(templatePath), Optional.empty(), aggregates);
        // then
        fileTestHelper.assertFileContents(underTest.asBytes(UTF_8), "with_wrapper.xhtml");
      }
  }
