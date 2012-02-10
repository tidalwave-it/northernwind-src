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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.util.test.FileComparisonUtils;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteProvider;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;

//@RequiredArgsConstructor
class MockResourceFinder extends FinderSupport<Resource, MockResourceFinder> implements SiteFinder<Resource> 
  {
    @Nonnull
    private final List<? extends Resource> results;

    public MockResourceFinder(List<? extends Resource> results) {
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
    public void doWithResults(Predicate predicate) 
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
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class XsltTemplateTestSupport 
  {
    private XsltMacroFilter filter;
    
    @BeforeMethod
    public void setup() 
      throws Exception
      {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/XsltTemplateTestBeans.xml");
        filter = context.getBean(XsltMacroFilter.class);
        final SiteProvider siteProvider = context.getBean(SiteProvider.class);
        final Site site = mock(Site.class);
        when(siteProvider.getSite()).thenReturn(site);
        
        final FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
        when(site.getFileSystemProvider()).thenReturn(fileSystemProvider);

        final File root = new File("src/main/resources/content/library/XsltTemplates").getAbsoluteFile();
        final FileObject fileObject = FileUtil.toFileObject(root);
        final List<Resource> resources = new ArrayList<Resource>();
        
        for (final FileObject xsltFileObject : fileObject.getChildren())
          {
            final Resource resource = mock(Resource.class);
            when(resource.getFile()).thenReturn(xsltFileObject);
            resources.add(resource);
          }
        
        when(site.find(eq(Resource.class))).thenReturn(new MockResourceFinder(resources)); 
      }
    
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
