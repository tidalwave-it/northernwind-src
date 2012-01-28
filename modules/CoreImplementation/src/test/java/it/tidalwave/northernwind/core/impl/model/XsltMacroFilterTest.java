/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.util.test.FileComparisonUtils;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/***********************************************************************************************************************
 *
 * @author fritz
 *
 **********************************************************************************************************************/
public class XsltMacroFilterTest 
  {
    private XsltMacroFilter fixture;
    
    @BeforeMethod
    public void setupFixture()
      {
        fixture = new XsltMacroFilter();  
      }
    
    @Test
    public void must_not_filter_resources_that_are_not_XHTML() 
      {
        final String text = "foo bar";
        
        final String result = fixture.filter(text, "text/html");
        
        assertThat(result, is(text));
      }
    
    @Test
    public void must_filter_XHTML_resources()
      throws IOException
      {
        final String text = IOUtils.toString(getClass().getResourceAsStream("/it/tidalwave/northernwind/core/impl/model/file1.xhtml"));
        
        final String result = fixture.filter(text, "application/xhtml+xml");
        
        final File expectedFile = new File("src/test/resources/expected-results/file1-filtered.xhtml");
        final File actualFile = new File("target/test-artifacts/file1-filtered.xhtml");
        actualFile.getParentFile().mkdir();
        FileUtils.write(actualFile, result);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }
}
