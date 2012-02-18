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
package it.tidalwave.northernwind.core.impl.model.filter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import lombok.Delegate;
import lombok.Getter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

class NodeLinkMacroExpanderFixture extends NodeLinkMacroExpander
  {
    @Delegate(types=MacroExpanderTestHelper.class) @Getter
    private final MacroExpanderTestHelper helper = new MacroExpanderTestHelper();
  }

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class NodeLinkMacroExpanderTest extends MacroExpanderTestSupport
  {
    public NodeLinkMacroExpanderTest()
      {
        super("NodeLinkMacroExpanderTestBeans.xml");
      }
    
    @Test
    public void must_find_the_correct_matches() 
      {
        final NodeLinkMacroExpanderFixture fixture = new NodeLinkMacroExpanderFixture();
        final String text = "href=\"$nodeLink(relativePath='/Blog')$\">1</a>";
        fixture.filter(text, "text/html");
        final List<List<String>> matches = fixture.getHelper().getMatches();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0), is(Arrays.asList("/Blog")));
      }
    
    @Test(dataProvider="textProvider")
    public void must_perform_the_proper_substitutions (final @Nonnull String text, final @Nonnull String expected) 
      {
        final NodeLinkMacroExpander fixture = context.getBean(NodeLinkMacroExpander.class);
        final String filtered = fixture.filter(text, "text/html");
        
        assertThat(filtered, is(expected));
      }
    
    @DataProvider(name="textProvider")
    public Object[][] textProvider()
      {
        return new Object[][]
          {
            {
              "href=\"$nodeLink(relativePath='/Blog')$\">1</a>",
              "href=\"/LINK/URI-Blog\">1</a>"
            }
          };        
      }
  }
