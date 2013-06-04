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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.testng.annotations.DataProvider;
//import static org.hamcrest.CoreMatchers.notNullValue;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteTest
  {
    private ClassPathXmlApplicationContext context;
    
    private ModelFactory modelFactory;
    
    private ResourceFileSystemProvider resourceFileSystemProvider;
    
    private ResourceFileSystem resourceFileSystem;
    
    private DefaultSite fixture;

    private Site.Builder siteBuilder;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteTestBeans.xml");
        modelFactory = context.getBean(ModelFactory.class);
        resourceFileSystemProvider = context.getBean(ResourceFileSystemProvider.class);
        resourceFileSystem = mock(ResourceFileSystem.class);
        when(resourceFileSystemProvider.getFileSystem()).thenReturn(resourceFileSystem);
        
        siteBuilder = new Site.Builder().withContextPath("contextpath")
                                        .withDocumentPath("content/document")
                                        .withMediaPath("content/media")
                                        .withLibraryPath("content/library")
                                        .withNodePath("structure")
                                        .withLogConfigurationEnabled(true)
                                        .withConfiguredLocales(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr")))
                                        .withIgnoredFolders(Arrays.asList("ignored1", "ignored2"));

        when(modelFactory.createResource(any(ResourceFile.class))).thenAnswer(new Answer<Resource>() 
          {
            @Override
            public Resource answer (final @Nonnull InvocationOnMock invocation) 
              {
                final Resource resource = mock(Resource.class);
                final ResourceFile file = (ResourceFile)invocation.getArguments()[0];
                final String path = file.getPath();
                
                when(resource.toString()).thenReturn(String.format("Resource(path=%s)", path));
                return resource;
              }
          });
                
        when(modelFactory.createContent(any(ResourceFile.class))).thenAnswer(new Answer<Content>() 
          {
            @Override
            public Content answer (final @Nonnull InvocationOnMock invocation) 
              {
                final Content content = mock(Content.class);
                final ResourceFile file = (ResourceFile)invocation.getArguments()[0];
                final String path = file.getPath();
                
                when(content.toString()).thenReturn(String.format("Content(path=%s)", path));
                return content;
              }
          });
                
        when(modelFactory.createMedia(any(ResourceFile.class))).thenAnswer(new Answer<Media>() 
          {
            @Override
            public Media answer (final @Nonnull InvocationOnMock invocation) 
              {
                final Media media = mock(Media.class);
                final ResourceFile file = (ResourceFile)invocation.getArguments()[0];
                final String path = file.getPath();
                
                when(media.toString()).thenReturn(String.format("Media(path=%s)", path));
                return media;
              }
          });
                
        when(modelFactory.createSiteNode(any(Site.class), any(ResourceFile.class))).thenAnswer(new Answer<SiteNode>() 
          {
            @Override
            public SiteNode answer (final @Nonnull InvocationOnMock invocation) 
              throws Exception
              {
                final ResourceFile file = (ResourceFile)invocation.getArguments()[1];
                final String relativeUri = String.format("relativeUriFor(%s)", file.getPath());
                final String path = file.getPath();
                final SiteNode siteNode = mock(SiteNode.class);
                
                final ResourceProperties properties = mock(ResourceProperties.class);
                when(properties.getProperty(eq(SiteNode.PROPERTY_MANAGES_PATH_PARAMS))).thenReturn("false");
                when(siteNode.getProperties()).thenReturn(properties);
                when(siteNode.getRelativeUri()).thenReturn(relativeUri); 
                when(siteNode.toString()).thenReturn(String.format("Node(path=%s)", path));
                
                return siteNode;
              }
          });
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_construct()
      throws Exception
      {
        final Site.Builder builder = new Site.Builder()
                .withContextPath("thecontextpath")
                .withDocumentPath("testDocumentPath")
                .withMediaPath("testMediaPath")
                .withLibraryPath("testLibraryPath")
                .withNodePath("testNodePath")
                .withLogConfigurationEnabled(true)
                .withConfiguredLocales(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr")))
                .withIgnoredFolders(Arrays.asList("ignored1", "ignored2"));
        
        fixture = new DefaultSite(builder);
        
        assertThat(fixture.getContextPath(), is("thecontextpath"));
        assertThat(fixture.documentPath, is("testDocumentPath"));
        assertThat(fixture.mediaPath, is("testMediaPath"));
        assertThat(fixture.libraryPath, is("testLibraryPath"));
        assertThat(fixture.nodePath, is("testNodePath"));
        assertThat(fixture.logConfigurationEnabled, is(true));
        assertThat(fixture.getConfiguredLocales(), is(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr"))));
        assertThat(fixture.ignoredFolders, is(Arrays.asList("ignored1", "ignored2")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "fileSystems")
    public void must_properly_initialize_with_an_empty_site (final @Nonnull FileSystemTestSupport fsTestSupport) 
      throws Exception
      {
        fsTestSupport.setUp(resourceFileSystem);
        fixture = new DefaultSite(siteBuilder);

        fixture.initialize();
        
        fsTestSupport.performAssertions(fixture);
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_a_Finder_for_Resource()
      throws Exception
      {
        final FileSystemTestSupport fsTestSupport = new EmptyTestFileSystem();
        fsTestSupport.setUp(resourceFileSystem);
        fixture = new DefaultSite(siteBuilder);
        fixture.initialize();
        
        final DefaultSiteFinder<Resource> finder = (DefaultSiteFinder<Resource>)fixture.find(Resource.class);
        
//        assertThat(finder.getName) TODO
        assertThat(finder.mapByRelativePath, is(sameInstance(fixture.libraryMapByRelativePath)));
        assertThat(finder.mapByRelativeUri,  is(nullValue()));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "fileSystems")
    public Object[][] fileSystemsDataProvider()
      {
        return new Object[][]
          {
            { new EmptyTestFileSystem() },
            { new TestFileSystemWithOnlyIgnoredFiles() },
            { new TestFileSystemWithAFewStuff1()}
                // TODO: add more filesystem configurations
          };
      }
  }
