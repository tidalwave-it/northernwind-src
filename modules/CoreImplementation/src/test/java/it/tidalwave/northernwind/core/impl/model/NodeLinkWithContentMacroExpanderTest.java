/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import it.tidalwave.northernwind.core.model.Site;
import lombok.Delegate;
import lombok.Getter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

class MacroExpanderTestHelper
  {
    @Getter
    private List<List<String>> matches = new ArrayList<List<String>>();
    
    @Nonnull
    public String filter (final @Nonnull Matcher matcher) 
      {
        final List<String> match = new ArrayList<String>();
        
        for (int i = 1; i <= matcher.groupCount(); i++)
          {
            match.add(matcher.group(i));
          }
        
        matches.add(match);
        
        return "";
      }
  }

class NodeLinkWithContentMacroExpanderFixture extends NodeLinkWithContentMacroExpander
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
public class NodeLinkWithContentMacroExpanderTest 
  {
    private Site site;
    
    @BeforeClass
    public void setUp() 
      {
        site = mock(Site.class);
      }
    
    @Test
    public void must_find_the_correct_matches() 
      {
        final NodeLinkWithContentMacroExpanderFixture fixture = new NodeLinkWithContentMacroExpanderFixture();
        final String text = "href=\"$nodeLink(relativePath='/Blog', contentRelativePath='/Blog/Equipment/The title')$\">1</a>";
        fixture.filter(text);
        final List<List<String>> matches = fixture.getHelper().getMatches();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0), is(Arrays.asList("/Blog", "/Blog/Equipment/The title")));
      }
  }
