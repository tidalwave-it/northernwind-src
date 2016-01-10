/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultSiteTest
  {
    private ClassPathXmlApplicationContext context;

    private MockModelFactory modelFactory;

    private RequestHolder requestHolder;

    private ResourceFileSystemProvider resourceFileSystemProvider;

    private ResourceFileSystem resourceFileSystem;

    private DefaultSite underTest;

    private Site.Builder siteBuilder;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteTest/TestBeans.xml");
        modelFactory = context.getBean(MockModelFactory.class);

        final Request request = mock(Request.class);
        when(request.getBaseUrl()).thenReturn("/baseUrl");
        requestHolder = context.getBean(RequestHolder.class);
        when(requestHolder.get()).thenReturn(request);

        resourceFileSystemProvider = context.getBean(ResourceFileSystemProvider.class);
        resourceFileSystem = mock(ResourceFileSystem.class);
        when(resourceFileSystemProvider.getFileSystem()).thenReturn(resourceFileSystem);

        final Site.Builder.CallBack callback = mock(Site.Builder.CallBack.class);
        siteBuilder = new Site.Builder(modelFactory, callback)
                                        .withContextPath("/contextpath") // TODO: should also test ""
                                        .withDocumentPath("/content/document")
                                        .withMediaPath("/content/media")
                                        .withLibraryPath("/content/library")
                                        .withNodePath("/structure")
                                        .withLogConfigurationEnabled(true)
                                        .withConfiguredLocales(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr")))
                                        .withIgnoredFolders(Arrays.asList("ignored1", "ignored2"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_construct()
      throws Exception
      {
        // when
        underTest = new DefaultSite(siteBuilder);
        // then
        assertThat(underTest.getContextPath(), is("/contextpath"));
        assertThat(underTest.documentPath, is("/content/document"));
        assertThat(underTest.mediaPath, is("/content/media"));
        assertThat(underTest.libraryPath, is("/content/library"));
        assertThat(underTest.nodePath, is("/structure"));
        assertThat(underTest.logConfigurationEnabled, is(true));
        assertThat(underTest.getConfiguredLocales(), is(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr"))));
        assertThat(underTest.ignoredFolders, is(Arrays.asList("ignored1", "ignored2")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "fileSystems")
    public void must_properly_initialize (final @Nonnull MockFileSystemSupport fsTestSupport)
      throws Exception
      {
        // given
        fsTestSupport.setUp(resourceFileSystem, modelFactory.getResourceProperties());
        underTest = new DefaultSite(siteBuilder);
        // when
        underTest.initialize();
        // then
        fsTestSupport.performAssertions(underTest);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_a_Finder_for_Content()
      throws Exception
      {
        // given
        final MockFileSystemSupport fsTestSupport = new EmptyMockFileSystem();
        fsTestSupport.setUp(resourceFileSystem, modelFactory.getResourceProperties());
        underTest = new DefaultSite(siteBuilder);
        underTest.initialize();
        // when
        final DefaultSiteFinder<Content> finder = (DefaultSiteFinder<Content>)underTest.find(Content.class);
        // then
//        assertThat(finder.getName) TODO
        assertThat(finder.mapByRelativePath, is(sameInstance(underTest.documentMapByRelativePath)));
        assertThat(finder.mapByRelativeUri,  is(nullValue()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_a_Finder_for_Resource()
      throws Exception
      {
        // given
        final MockFileSystemSupport fsTestSupport = new EmptyMockFileSystem();
        fsTestSupport.setUp(resourceFileSystem, modelFactory.getResourceProperties());
        underTest = new DefaultSite(siteBuilder);
        underTest.initialize();
        // when
        final DefaultSiteFinder<Resource> finder = (DefaultSiteFinder<Resource>)underTest.find(Resource.class);
        // then
//        assertThat(finder.getName) TODO
        assertThat(finder.mapByRelativePath, is(sameInstance(underTest.libraryMapByRelativePath)));
        assertThat(finder.mapByRelativeUri,  is(nullValue()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_a_Finder_for_Media()
      throws Exception
      {
        // given
        final MockFileSystemSupport fsTestSupport = new EmptyMockFileSystem();
        fsTestSupport.setUp(resourceFileSystem, modelFactory.getResourceProperties());
        underTest = new DefaultSite(siteBuilder);
        underTest.initialize();
        // when
        final DefaultSiteFinder<Media> finder = (DefaultSiteFinder<Media>)underTest.find(Media.class);
        // then
//        assertThat(finder.getName) TODO
        assertThat(finder.mapByRelativePath, is(sameInstance(underTest.mediaMapByRelativePath)));
        assertThat(finder.mapByRelativeUri,  is(nullValue()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_a_Finder_for_SiteNode()
      throws Exception
      {
        // given
        final MockFileSystemSupport fsTestSupport = new EmptyMockFileSystem();
        fsTestSupport.setUp(resourceFileSystem, modelFactory.getResourceProperties());
        underTest = new DefaultSite(siteBuilder);
        underTest.initialize();
        // when
        final DefaultSiteFinder<SiteNode> finder = (DefaultSiteFinder<SiteNode>)underTest.find(SiteNode.class);
        // then
//        assertThat(finder.getName) TODO
        assertThat(finder.mapByRelativePath, is(sameInstance(underTest.nodeMapByRelativePath)));
        assertThat(finder.mapByRelativeUri,  is(sameInstance(underTest.nodeMapByRelativeUri)));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_create_correct_links()
      throws Exception
      {
        // given
        underTest = new DefaultSite(siteBuilder);
        // when
        final String result = underTest.createLink(new ResourcePath("link"));
        // given
        assertThat(result, is("lpp3-lpp2-lpp1-/baseUrl/contextpath/link"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] fileSystems()
      {
        return new Object[][]
          {
            { new EmptyMockFileSystem()                },
            { new MockFileSystemWithOnlyIgnoredFiles() },
            { new MockFileSystemWithAFewStuff1()       }
                // TODO: add more filesystem configurations
          };
      }
  }
