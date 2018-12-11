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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.*;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.frontend.ui.component.Template.Aggregates;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class HtmlTemplateBlogViewTest
  {
    private HtmlTemplateBlogView underTest;

    private Site site;

    private Id viewId = new Id("viewId");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        site = mock(Site.class);
        MockContentSiteFinder.registerTo(site);
        underTest = new HtmlTemplateBlogView(site, viewId);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_posts_with_custom_template()
      throws Exception
      {
        // given
        final String templatePath = "/the/template/path";
        final Content template = site.find(Content).withRelativePath(templatePath).result();
        final ResourceProperties properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("Custom posts template"));
        // when
        underTest.renderPosts(Optional.of(templatePath), Aggregates.EMPTY, Aggregates.EMPTY, Aggregates.EMPTY);
        // then
        assertThat(underTest.asString(UTF_8), is("<div class='nw-viewId'>\nCustom posts template\n\n</div>"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_render_tag_cloud_with_custom_template()
      throws Exception
      {
        // given
        final String templatePath = "/the/template/path";
        final Content template = site.find(Content).withRelativePath(templatePath).result();
        final ResourceProperties properties = template.getProperties();
        when(properties.getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of("Custom tag cloud template"));
        // when
        underTest.renderTagCloud(Optional.of(templatePath), Aggregates.EMPTY);
        // then
        assertThat(underTest.asString(UTF_8), is("<div class='nw-viewId'>\nCustom tag cloud template\n\n</div>"));
      }
  }
