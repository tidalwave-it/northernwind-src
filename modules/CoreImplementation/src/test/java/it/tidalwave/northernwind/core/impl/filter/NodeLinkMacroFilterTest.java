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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.NorthernWindTestSupport;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NodeLinkMacroExpanderFixture extends NodeLinkMacroFilter
  {
    @Delegate(types = MacroFilterTestHelper.class) @Getter
    private final MacroFilterTestHelper helper = new MacroFilterTestHelper();
  }

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class NodeLinkMacroFilterTest extends NorthernWindTestSupport
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_find_the_correct_matches()
      {
        // given
        final var underTest = new NodeLinkMacroExpanderFixture();
        final var text = "href=\"$nodeLink(relativePath='/Blog')$\">1</a>";
        // when
        underTest.filter(text, "text/html");
        final var matches = underTest.getHelper().getMatches();
        // then
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0), is(List.of("/Blog")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "textProvider")
    public void must_perform_the_proper_substitutions (@Nonnull final String text, @Nonnull final String expected)
      {
        // given
        final var underTest = context.getBean(NodeLinkMacroFilter.class);
        // when
        final var filtered = underTest.filter(text, "text/html");
        // then
        assertThat(filtered, is(expected));
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
              // INPUT                                                EXPECTED
              "href=\"$nodeLink(relativePath='/Blog')$\">1</a>",      "href=\"/LINK/URI-Blog\">1</a>"
            }
          };
      }
  }
