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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteNodeTest
  {
    private ClassPathXmlApplicationContext context;

    private DefaultSiteNode fixture;

    private InternalSite site;

    private Resource resource;

    private ResourceFile resourceFile;

    private ModelFactory modelFactory;

    private InheritanceHelper inheritanceHelper;

    private Layout emptyPlaceHolderLayout;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteNodeTestBeans.xml");
        site = context.getBean(InternalSite.class);
        modelFactory = context.getBean(ModelFactory.class);
        inheritanceHelper = context.getBean(InheritanceHelper.class);

        resource = mock(Resource.class);
        resourceFile = MockResourceFile.folder("/structure/foo/resourceFile");
        when(resource.getFile()).thenReturn(resourceFile);
        when(modelFactory.createResource(any(ResourceFile.class))).thenReturn(resource);

        final ResourceFile nodeFolder = MockResourceFile.folder("structure");
        when(site.getNodeFolder()).thenReturn(nodeFolder);

        emptyPlaceHolderLayout = mock(Layout.class);
//        when(modelFactory.createLayout(any(Id.class), eq("emptyPlaceholder"))).thenReturn(emptyPlaceHolderLayout);
        when(modelFactory.createLayout()).thenReturn(new Layout.Builder().withCallBack(new Layout.Builder.CallBack()
          {
            @Override
            public Layout build (final @Nonnull Layout.Builder builder)
              {
                assertThat(builder.getType(), is("emptyPlaceholder"));
                return emptyPlaceHolderLayout;
              }
          }));

        fixture = new DefaultSiteNode(site, resourceFile);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_initialize_with_no_layout()
      {
        assertThat(fixture.site, sameInstance(site));
        assertThat(fixture.resource, sameInstance(resource));
        assertThat(fixture.getLayout(), sameInstance(emptyPlaceHolderLayout));
      }

//    /*******************************************************************************************************************
//     *
//     ******************************************************************************************************************/
//    @Test
//    public void must_properly_initialize_with_layout()
//      {
//        assertThat(fixture.site, sameInstance(site));
//        assertThat(fixture.resource, sameInstance(resource));
//        assertThat(fixture.getLayout(), sameInstance(emptyPlaceHolderLayout));
//      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_initialize_with_no_layout", dataProvider = "uriProvider")
    public void getRelativeUri_must_return_a_correct_value (final @CheckForNull String exposedUri,
                                                            final @Nonnull String fileName,
                                                            final @Nonnull String parentUri,
                                                            final @Nonnull String parentPath,
                                                            final @Nonnull String expectedResult)
      throws Exception
      {
        prepareMocksForGetRelativeUri(exposedUri, fileName, parentUri, parentPath);

        final ResourcePath relativeUri = fixture.getRelativeUri();

        assertThat(relativeUri.asString(), is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void getRelativeUri_must_be_called_only_once()
      throws Exception
      {
        prepareMocksForGetRelativeUri("exposedUri1", "file1", "/parentUri1", "structure/parent1");

        final int previousUriComputationCounter = fixture.uriComputationCounter;

        for (int i = 0; i < 10; i++)
          {
            fixture.getRelativeUri();
          }

        assertThat(fixture.uriComputationCounter, is(previousUriComputationCounter + 1));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void prepareMocksForGetRelativeUri (final @CheckForNull String exposedUri,
                                                final @Nonnull String fileName,
                                                final @Nonnull String parentUri,
                                                final @Nonnull String parentPath)
      throws IOException, NotFoundException
      {
        final ResourceFile parentResourceFile = MockResourceFile.folder(parentPath);
        resourceFile = MockResourceFile.folder(parentResourceFile, fileName);
        when(resource.getFile()).thenReturn(resourceFile);

        final SiteNode parentSiteNode = mock(SiteNode.class);
        when(parentSiteNode.getRelativeUri()).thenReturn(new ResourcePath(parentUri));

        final ResourceProperties properties = mock(ResourceProperties.class);

        // FIXME: consider using a concrete mock for ResourceProperties, since it's cumbersome to mock in this way
        if (exposedUri != null)
          {
            when(properties.getProperty(eq(SiteNode.PROPERTY_EXPOSED_URI), anyString())).thenReturn(exposedUri);
          }
        else
          {
            when(properties.getProperty(eq(SiteNode.PROPERTY_EXPOSED_URI), anyString())).thenAnswer(new Answer<String>()
              {
                @Override
                public String answer (final @Nonnull InvocationOnMock invocation)
                  {
                    return (String)invocation.getArguments()[1];
                  }
              });
          }

        when(resource.getProperties()).thenReturn(properties);

        final SiteFinder<SiteNode> siteNodeFinder = mock(SiteFinder.class);
        when(siteNodeFinder.withRelativePath(anyString())).thenReturn(siteNodeFinder); // FIXME: anyString()
//        when(siteNodeFinder.withRelativePath(eq(parentPath))).thenReturn(siteNodeFinder);
        when(siteNodeFinder.result()).thenReturn(parentSiteNode);
        when(site.find(eq(SiteNode.class))).thenReturn(siteNodeFinder);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "uriProvider")
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