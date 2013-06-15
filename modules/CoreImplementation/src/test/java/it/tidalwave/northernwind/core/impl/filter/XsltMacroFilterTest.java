/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import it.tidalwave.northernwind.core.model.ResourceFile;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.model.DefaultSiteFinder;
import it.tidalwave.northernwind.core.impl.util.RegexTreeMap;
import it.tidalwave.util.test.FileComparisonUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author fritz
 *
 **********************************************************************************************************************/
public class XsltMacroFilterTest
  {
    private XsltMacroFilter fixture;

    private SiteProvider siteProvider;

    private Site site;

    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/CommonsAutoBeans.xml",
                                                                              "XsltMacroFilterTestBeans.xml");
        siteProvider = context.getBean(SiteProvider.class);
        site = context.getBean(Site.class);
        when(siteProvider.getSite()).thenReturn(site);

        final ResourceFile file = mock(ResourceFile.class);
        final String resourceName = "/it/tidalwave/northernwind/core/impl/model/Photo.xslt";
        final String xslt = IOUtils.toString(getClass().getResourceAsStream(resourceName));
        when(file.asText(anyString())).thenReturn(xslt);

        final Resource resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(file);

        final Map<String, Resource> map = new HashMap<String, Resource>();
        map.put("/XsltTemplates/Photo.xlst", resource);
        when(site.find(eq(Resource.class))).thenReturn(
                new DefaultSiteFinder<Resource>("name", map, new RegexTreeMap<Resource>()));

        fixture = context.getBean(XsltMacroFilter.class);
      }

    @Test
    public void must_not_filter_resources_that_are_not_XHTML()
      {
        final String text = "foo bar";

        final String result = fixture.filter(text, "text/html");

        assertThat(result, is(text));
      }

    @Test(dataProvider = "fileNames")
    public void must_filter_XHTML_resources (final @Nonnull String fileName)
      throws IOException
      {
        final String resourceName = String.format("/it/tidalwave/northernwind/core/impl/model/%s.xhtml", fileName);
        final String text = IOUtils.toString(getClass().getResourceAsStream(resourceName));
        final String result = fixture.filter(text, "application/xhtml+xml");

        final File expectedFile = new File(String.format("src/test/resources/expected-results/%s-filtered.xhtml",
                                                         fileName));
        final File actualFile = new File(String.format("target/test-artifacts/%s-filtered.xhtml", fileName));
        actualFile.getParentFile().mkdirs();
        FileUtils.write(actualFile, result);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }

    @DataProvider(name = "fileNames")
    public Object[][] fileNamesProvider()
      {
        return new Object[][]
          {
            { "file1" }, { "file2" }, { "issue-NW-96-a-NW-106-a" }, { "issue-NW-96-b" }, { "issue-NW-97-a" }, { "issue-NW-100" },
            { "issue-NW-102-a" }, { "issue-NW-104-a" }, { "issue-NW-114-a" }
          };
      }
  }
