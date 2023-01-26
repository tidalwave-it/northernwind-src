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
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.model.DefaultSiteFinder;
import it.tidalwave.northernwind.core.impl.util.RegexTreeMap;
import it.tidalwave.northernwind.core.model.ResourcePath;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class XsltMacroFilterTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ApplicationContext context;

    private XsltMacroFilter underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = helper.createSpringContext();
        final var siteProvider = context.getBean(SiteProvider.class);
        final var site = context.getBean(Site.class);
        when(siteProvider.getSite()).thenReturn(site);

        final var xslt = helper.readStringFromResource("Photo.xslt");
        createMockResource(site, createMockTextFileOfPathAndContent("/XsltTemplates/Photo.xlst", xslt));

        underTest = context.getBean(XsltMacroFilter.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_filter_resources_that_are_not_XHTML()
      {
        // given
        final var text = "foo bar";
        // when
        final var result = underTest.filter(text, "text/html");
        // then
        assertThat(result, is(text));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "fileNames")
    public void must_filter_XHTML_resources (@Nonnull final String fileName)
      throws IOException
      {
        // given
        final var tr = helper.testResourceFor(fileName);
        final var text = tr.readStringFromResource();
        // when
        final var filteredText = underTest.filter(text, "application/xhtml+xml");
        // then
        tr.writeToActualFile(filteredText);
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] fileNames()
      {
        return new Object[][]
          {
            { "file1.xhtml" },
            { "file2.xhtml" },
            { "issue-NW-96-a-NW-106-a.xhtml" },
            { "issue-NW-96-b.xhtml" },
            { "issue-NW-97-a.xhtml" },
            { "issue-NW-100.xhtml" },
            { "issue-NW-102-a.xhtml" },
            { "issue-NW-104-a.xhtml" },
            { "issue-NW-114-a.xhtml" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceFile createMockTextFileOfPathAndContent (@Nonnull final String path, @Nonnull final String content)
      throws IOException
      {
        final var file = mock(ResourceFile.class);
        when(file.getPath()).thenReturn(ResourcePath.of(path));
        when(file.asText(anyString())).thenReturn(content);
        return file;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void createMockResource (@Nonnull final Site site, @Nonnull final ResourceFile file)
      {
        final var resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(file);

        final Map<String, Resource> map = new HashMap<>();
        map.put(file.getPath().asString(), resource);
        when(site.find(eq(Resource.class))).thenReturn(new DefaultSiteFinder<>("mockFinder", map, new RegexTreeMap<>()));
      }
  }
