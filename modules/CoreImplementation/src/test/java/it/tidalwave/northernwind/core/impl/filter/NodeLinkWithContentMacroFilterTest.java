/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import lombok.Delegate;
import lombok.Getter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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
 * @version $Id$
 *
 **********************************************************************************************************************/
public class NodeLinkWithContentMacroFilterTest extends MacroFilterTestSupport
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "matchesDataProvider")
    public void must_find_the_correct_matches (final @Nonnull String text,
                                               final @Nonnull List<String> expectedMatches)
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
    public void must_perform_the_proper_substitutions (final @Nonnull String text, final @Nonnull String expected)
      {
        // given
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
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>",
              Arrays.asList("/Blog", "/Blog/Equipment/The title", null, null)
            },
            {
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='it')$\">1</a>",
              Arrays.asList("/Blog", "/Blog/Equipment/The title", ", language='it'", "it")
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
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/')$\">1</a>",
              "href=\"/LINK/URI-Blog\">1</a>"
            },
            {
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>",
              "href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title\">1</a>"
            },
            {
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='it')$\">1</a>",
              "href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title/?l=it\">1</a>"
            },
            {
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title', language='fr')$\">1</a>",
              "href=\"/LINK/URI-Blog/EXPOSED-Blog-Equipment-The-title/?l=fr\">1</a>"
            }
          };
      }
  }
