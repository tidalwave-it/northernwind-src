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
package it.tidalwave.northernwind.core.impl.filter;

import it.tidalwave.northernwind.util.test.NorthernWindTestSupport;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.springframework.context.support.GenericApplicationContext;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

class NodeLinkWithContentMacroFilterFixture extends NodeLinkWithContentMacroFilter
  {
    @Delegate(types = MacroFilterTestHelper.class) @Getter
    private final MacroFilterTestHelper helper = new MacroFilterTestHelper();
  }

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class NodeLinkWithContentMacroFilterTest extends NorthernWindTestSupport
  {
    private static final Consumer<GenericApplicationContext> NO_CHANGES = context -> {};

    private static final Consumer<GenericApplicationContext> REMOVE_LANGUAGE_POST_PROCESSORS = context ->
      {
        context.removeBeanDefinition("parameterLanguageOverrideRequestProcessor");
        context.removeBeanDefinition("parameterLanguageOverrideLinkPostProcessor");
      };

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "matchesDataProvider")
    public void must_find_the_correct_matches (@Nonnull final String text,
                                               @Nonnull final List<String> expectedMatches)
      {
        // given
        final NodeLinkWithContentMacroFilterFixture underTest = new NodeLinkWithContentMacroFilterFixture();
        // when
        underTest.filter(text, "text/html");
        // then
        final List<List<String>> matches = underTest.getHelper().getMatches();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0), is(expectedMatches));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "textProvider")
    public void must_perform_the_proper_substitutions (@Nonnull final String text,
                                                       @Nonnull final Consumer<GenericApplicationContext> config,
                                                       @Nonnull final String expected)
      {
        // given
        setupContext(config);
        final NodeLinkWithContentMacroFilter underTest = context.getBean(NodeLinkWithContentMacroFilter.class);
        // when
        final String filtered = underTest.filter(text, "text/html");
        // then
        assertThat(filtered, is(expected));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] matchesDataProvider()
      {
        return new Object[][]
          {
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>",
              List.of("/Blog", "/Blog/Equipment/The title", null, null)
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='it')$\">1</a>",
              List.of("/Blog", "/Blog/Equipment/The title", ", language='it'", "it")
            }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] textProvider()
      {
        return new Object[][]
          {
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/')$\">1</a>",
              NO_CHANGES,
              "<a href=\"/LINK/URI-Blog\">1</a>" // FIXME: missing leading / ?
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>",
              NO_CHANGES,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title\">1</a>" // FIXME: missing leading / ?
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='it')$\">1</a>",
              NO_CHANGES,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title/?l=it\">1</a>"
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='fr')$\">1</a>",
              NO_CHANGES,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title/?l=fr\">1</a>"
            },

            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/')$\">1</a>",
              REMOVE_LANGUAGE_POST_PROCESSORS,
              "<a href=\"/LINK/URI-Blog\">1</a>" // FIXME: missing leading / ?
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>",
              REMOVE_LANGUAGE_POST_PROCESSORS,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title\">1</a>" // FIXME: missing leading / ?
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='it')$\">1</a>",
              REMOVE_LANGUAGE_POST_PROCESSORS,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title\">1</a>" // FIXME: missing leading / ?
            },
            {
              "<a href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='fr')$\">1</a>",
              REMOVE_LANGUAGE_POST_PROCESSORS,
              "<a href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title\">1</a>" // FIXME: missing leading / ?
            }
          };
      }
  }
