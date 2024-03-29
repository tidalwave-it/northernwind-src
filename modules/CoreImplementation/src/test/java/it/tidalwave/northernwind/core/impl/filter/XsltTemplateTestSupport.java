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
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import it.tidalwave.util.spi.HierarchicFinderSupport;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.LocalFileSystemProvider;
import it.tidalwave.northernwind.core.impl.util.CachedURIResolver;
import org.testng.annotations.BeforeMethod;
import it.tidalwave.util.test.FileComparisonUtils;
import it.tidalwave.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;

//@RequiredArgsConstructor
class MockResourceFinder extends HierarchicFinderSupport<Resource, SiteFinder<Resource>> implements SiteFinder<Resource>
  {
    private static final long serialVersionUID = 1692141469939523431L;

    @Nonnull
    private final List<Resource> results;

    public MockResourceFinder (@Nonnull final List<? extends Resource> results)
      {
        this.results = (List<Resource>)results;
      }

    @Nonnull
    @Override
    public MockResourceFinder withRelativePath(@Nonnull final String relativePath)
      {
        return this;
      }

    @Nonnull
    @Override
    public MockResourceFinder withRelativeUri(@Nonnull final String relativeUri)
      {
        return this;
      }

    @Nonnull
    @Override
    protected List<Resource> computeResults()
      {
        return results;
      }
  }

/***********************************************************************************************************************
 *
 * FIXME: it seems this class is not used.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class XsltTemplateTestSupport
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private XsltMacroFilter filter;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        final var context = helper.createSpringContext(
                "META-INF/CommonsAutoBeans.xml",
                "XsltTemplateTest/TestBeans.xml",
                "META-INF/CachedUriResolverBeans.xml");
        filter = context.getBean(XsltMacroFilter.class);
        context.getBean(CachedURIResolver.class).setCacheFolderPath("target/CachedUriResolver");
        final var siteProvider = context.getBean(SiteProvider.class);
        final var site = mock(Site.class);
        when(siteProvider.getSite()).thenReturn(site);

        final var fileSystemProvider = mock(ResourceFileSystemProvider.class);
        when(site.getFileSystemProvider()).thenReturn(fileSystemProvider);

        final var root = new File("src/main/resources/content/library/XsltTemplates").getAbsoluteFile();
        final ResourceFileSystemProvider localFileSystemProvider = new LocalFileSystemProvider();
        final var file = localFileSystemProvider.getFileSystem().findFileByPath(root.getAbsolutePath());
        final List<Resource> resources = new ArrayList<>();

        for (final var xsltFile : file.findChildren().results())
          {
            final var resource = mock(Resource.class);
            when(resource.getFile()).thenReturn(xsltFile);
            resources.add(resource);
          }

        when(site.find(eq(Resource.class))).thenReturn(new MockResourceFinder(resources));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void test (@Nonnull final String sourceFileName)
      throws IOException
      {
        final var sourceFile = new File("src/test/resources/" + sourceFileName);
        final var text = FileUtils.readFileToString(sourceFile);
        final var filter1 = filter.filter(text, "application/xhtml+xml");
        final var actualFile = new File("target/test-artifacts/Filtered" + sourceFileName);
        final var expectedFile = new File("src/test/resources/expected-results/Filtered" + sourceFileName);
        FileUtils.writeStringToFile(actualFile, filter1);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }
  }
