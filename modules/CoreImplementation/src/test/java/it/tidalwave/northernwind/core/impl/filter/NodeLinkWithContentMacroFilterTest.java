/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
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
    @Delegate(types=MacroFilterTestHelper.class) @Getter
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
    public NodeLinkWithContentMacroFilterTest()
      {
        super("NodeLinkWithContentMacroFilterTestBeans.xml");
      }
    
    @Test
    public void must_find_the_correct_matches() 
      {
        final NodeLinkWithContentMacroFilterFixture fixture = new NodeLinkWithContentMacroFilterFixture();
        final String text = "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>";
        fixture.filter(text, "text/html");
        final List<List<String>> matches = fixture.getHelper().getMatches();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0), is(Arrays.asList("/Blog", "/Blog/Equipment/The title", null, null)));
      }
    
    @Test(dataProvider="textProvider")
    public void must_perform_the_proper_substitutions (final @Nonnull String text, final @Nonnull String expected) 
      {
        final NodeLinkWithContentMacroFilter fixture = context.getBean(NodeLinkWithContentMacroFilter.class);
        final String filtered = fixture.filter(text, "text/html");
        
        assertThat(filtered, is(expected));
      }
    
    @DataProvider(name="textProvider")
    public Object[][] textProvider()
      {
        return new Object[][]
          {
            {
              "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/')$\">1</a>",
              "href=\"/LINK/URI-Blog/\">1</a>"
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