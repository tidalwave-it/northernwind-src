/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import com.google.common.base.Predicate;
import it.tidalwave.northernwind.core.model.ResourceFile;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.util.test.FileComparisonUtils;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.impl.util.CachedURIResolver;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.LocalFileSystemProvider;
import org.testng.annotations.BeforeMethod;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;

//@RequiredArgsConstructor
class MockResourceFinder extends FinderSupport<Resource, MockResourceFinder> implements SiteFinder<Resource>
  {
    private static final long serialVersionUID = 1692141469939523431L;

    @Nonnull
    private final List<? extends Resource> results;

    public MockResourceFinder(List<? extends Resource> results)
      {
        this.results = results;
      }

    @Override
    public MockResourceFinder withRelativePath(String relativePath)
      {
        return this;
      }

    @Override
    public MockResourceFinder withRelativeUri(String relativeUri)
      {
        return this;
      }

    @Override
    public void doWithResults(Predicate<Resource> predicate)
      {
      }

    @Override
    protected List<? extends Resource> computeResults()
      {
        return results;
      }
  }

/***********************************************************************************************************************
 *
 * FIXME: it seems this class is not used.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
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
        final ApplicationContext context = helper.createSpringContext(
                "META-INF/CommonsAutoBeans.xml",
                "XsltTemplateTest/TestBeans.xml",
                "META-INF/CachedUriResolverBeans.xml");
        filter = context.getBean(XsltMacroFilter.class);
        context.getBean(CachedURIResolver.class).setCacheFolderPath("target/CachedUriResolver");
        final SiteProvider siteProvider = context.getBean(SiteProvider.class);
        final Site site = mock(Site.class);
        when(siteProvider.getSite()).thenReturn(site);

        final ResourceFileSystemProvider fileSystemProvider = mock(ResourceFileSystemProvider.class);
        when(site.getFileSystemProvider()).thenReturn(fileSystemProvider);

        final File root = new File("src/main/resources/content/library/XsltTemplates").getAbsoluteFile();
        final ResourceFileSystemProvider localFileSystemProvider = new LocalFileSystemProvider();
        final ResourceFile file = localFileSystemProvider.getFileSystem().findFileByPath(root.getAbsolutePath());
        final List<Resource> resources = new ArrayList<>();

        for (final ResourceFile xsltFile : file.findChildren().results())
          {
            final Resource resource = mock(Resource.class);
            when(resource.getFile()).thenReturn(xsltFile);
            resources.add(resource);
          }

        when(site.find(eq(Resource.class))).thenReturn(new MockResourceFinder(resources));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void test (final @Nonnull String sourceFileName)
      throws IOException
      {
        final File sourceFile = new File("src/test/resources/" + sourceFileName);
        final String text = FileUtils.readFileToString(sourceFile);
        final String filter1 = filter.filter(text, "application/xhtml+xml");
        final File actualFile = new File("target/test-artifacts/Filtered" + sourceFileName);
        final File expectedFile = new File("src/test/resources/expected-results/Filtered" + sourceFileName);
        FileUtils.writeStringToFile(actualFile, filter1);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }
  }
