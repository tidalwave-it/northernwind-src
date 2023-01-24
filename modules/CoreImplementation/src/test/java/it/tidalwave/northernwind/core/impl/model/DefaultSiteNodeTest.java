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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import it.tidalwave.northernwind.core.impl.model.mock.MockResourceFile;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.hamcrest.CoreMatchers.is;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultSiteNodeTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ApplicationContext context;

    private DefaultSiteNode underTest;

    private InternalSite site;

    private Resource resource;

    private ResourceFile resourceFile;

    private Layout emptyPlaceHolderLayout;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        context = helper.createSpringContext();
        site = context.getBean(InternalSite.class);
        final var modelFactory = context.getBean(ModelFactory.class);
        final var inheritanceHelper = context.getBean(InheritanceHelper.class);
        final var requestLocaleManager = context.getBean(RequestLocaleManager.class);

        resource = createMockResource();
        resourceFile = MockResourceFile.folder("/structure/foo/resourceFile");
        when(resource.getFile()).thenReturn(resourceFile);
        when(modelFactory.createResource()).thenReturn(new Resource.Builder(modelFactory, builder -> resource));
        when(requestLocaleManager.getLocales()).thenReturn(List.of(Locale.ENGLISH));

        final var nodeFolder = MockResourceFile.folder("structure");
        when(site.getNodeFolder()).thenReturn(nodeFolder);

        emptyPlaceHolderLayout = mock(Layout.class);
//        when(modelFactory.createLayout(any(Id.class), eq("emptyPlaceholder"))).thenReturn(emptyPlaceHolderLayout);
        when(modelFactory.createLayout()).thenReturn(new Layout.Builder(modelFactory, builder ->
        {
          assertThat(builder.getType(), is("emptyPlaceholder"));
          return emptyPlaceHolderLayout;
        }));

        underTest = new DefaultSiteNode(modelFactory, site, resourceFile);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_initialize_with_no_layout()
      {
        // given the initialization
        // then
        assertThat(underTest.site, sameInstance(site));
        assertThat(underTest.getResource(), sameInstance(resource));
        assertThat(underTest.getLayout(), sameInstance(emptyPlaceHolderLayout));
      }

//    /*******************************************************************************************************************
//     *
//     ******************************************************************************************************************/
//    @Test
//    public void must_properly_initialize_with_layout()
//      {
//        assertThat(underTest.site, sameInstance(site));
//        assertThat(underTest.resource, sameInstance(resource));
//        assertThat(underTest.getLayout(), sameInstance(emptyPlaceHolderLayout));
//      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_initialize_with_no_layout", dataProvider = "uriProvider")
    public void getRelativeUri_must_return_a_correct_value (@CheckForNull final String exposedUri,
                                                            @Nonnull final String fileName,
                                                            @Nonnull final String parentUri,
                                                            @Nonnull final String parentPath,
                                                            @Nonnull final String expectedResult)
      throws Exception
      {
        // given
        prepareMocksForGetRelativeUri(exposedUri, fileName, parentUri, parentPath);
        // when
        final var relativeUri = underTest.getRelativeUri();
        // then
        assertThat(relativeUri.asString(), is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void getRelativeUri_must_be_called_only_once()
      throws Exception
      {
        // given
        prepareMocksForGetRelativeUri("exposedUri1", "file1", "/parentUri1", "structure/parent1");
        final var previousUriComputationCounter = underTest.uriComputationCounter;
        // when
        for (var i = 0; i < 10; i++)
          {
            underTest.getRelativeUri();
          }
        // then
        assertThat(underTest.uriComputationCounter, is(previousUriComputationCounter + 1));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void prepareMocksForGetRelativeUri (@CheckForNull final String exposedUri,
                                                @Nonnull final String fileName,
                                                @Nonnull final String parentUri,
                                                @Nonnull final String parentPath)
            throws NotFoundException
      {
        final var parentResourceFile = MockResourceFile.folder(parentPath);
        resourceFile = MockResourceFile.folder(parentResourceFile, fileName);
        when(resource.getFile()).thenReturn(resourceFile);

        final var parentSiteNode = createMockSiteNode(site);
        when(parentSiteNode.getRelativeUri()).thenReturn(ResourcePath.of(parentUri));

        final var properties = createMockProperties();
        when(properties.getProperty(eq(SiteNode.P_EXPOSED_URI))).thenReturn(Optional.ofNullable(exposedUri));
        when(resource.getProperties()).thenReturn(properties);

        final SiteFinder<SiteNode> siteNodeFinder = createMockSiteFinder();
//        when(siteNodeFinder.withRelativePath(eq(parentPath))).thenReturn(siteNodeFinder);
        when(siteNodeFinder.result()).thenReturn(parentSiteNode);
        when(site.find(eq(SiteNode.class))).thenReturn(siteNodeFinder);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] uriProvider()
      {
        return new Object[][]
          {
            //   exposedUri     fileName,       parentUri      parentPath           expectedResult
            // root node
              { null,          "structure",    "irrelevant",  "",                  "/"                        },
              { "exposedUri",  "structure",    "irrelevant",  "",                  "/"                        },

            // just below the root node
              { null,          "file1",        "/",           "structure",         "/file1"                   },
              { null,          "file2",        "/",           "structure",         "/file2"                   },
              { "exposedUri1", "file1",        "/",           "structure",         "/exposedUri1"             },
              { "exposedUri2", "file2",        "/",           "structure",         "/exposedUri2"             },

              { null,          "file1",        "/parentUri1", "structure/parent3", "/parentUri1/file1"        },
              { null,          "file2",        "/parentUri1", "structure/parent4", "/parentUri1/file2"        },
              { null,          "file1",        "/parentUri2", "structure/parent5", "/parentUri2/file1"        },
              { null,          "file2",        "/parentUri2", "structure/parent6", "/parentUri2/file2"        },
              { null,          "file+%282%29", "/parentUri2", "structure/parent6", "/parentUri2/file (2)"     },
              { "exposedUri1", "file1",        "/parentUri1", "structure/parent1", "/parentUri1/exposedUri1"  },
              { "exposedUri2", "file1",        "/parentUri2", "structure/parent2", "/parentUri2/exposedUri2"  }
          };
      }
  }
