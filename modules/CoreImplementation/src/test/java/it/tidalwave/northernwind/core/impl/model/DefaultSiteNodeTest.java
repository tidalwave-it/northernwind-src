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

import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;

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

    private Site site;

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
        site = context.getBean(Site.class);
        modelFactory = context.getBean(ModelFactory.class);
        inheritanceHelper = context.getBean(InheritanceHelper.class);
        resourceFile = mock(ResourceFile.class);

        resource = mock(Resource.class);
        resourceFile = mock(ResourceFile.class);
        when(resourceFile.getPath()).thenReturn("/structure/foo/resourceFile");
        when(resourceFile.getName()).thenReturn("resourceFile");
        when(resource.getFile()).thenReturn(resourceFile);
        when(modelFactory.createResource(any(ResourceFile.class))).thenReturn(resource);

        emptyPlaceHolderLayout = mock(Layout.class);
        when(modelFactory.createLayout(any(Id.class), eq("emptyPlaceholder"))).thenReturn(emptyPlaceHolderLayout);

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
//        TODO: requires ModelFactory to create a default DefaultLayout -> with a Builder
//        assertThat(fixture.site, sameInstance(site));
//        assertThat(fixture.resource, sameInstance(resource));
//        assertThat(fixture.getLayout(), sameInstance(emptyPlaceHolderLayout));
//      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void getRelativeUri_must_return_a_correct_value()
      throws Exception
      {
        final String exposedUri = "exposedUri";
        final String parentUri = "/parentUri";
        final String parentPath = "/parent";

        final ResourceFile parentResourceFile = mock(ResourceFile.class);
        final SiteNode parentSiteNode = mock(SiteNode.class);
        when(parentSiteNode.getRelativeUri()).thenReturn(parentUri);
        when(parentResourceFile.getPath()).thenReturn(parentPath);
        when(resourceFile.getParent()).thenReturn(parentResourceFile);

        final ResourceProperties properties = mock(ResourceProperties.class);
        when(properties.getProperty(eq(SiteNode.PROPERTY_EXPOSED_URI), anyString())).thenReturn(exposedUri);
        when(resource.getProperties()).thenReturn(properties);

        final SiteFinder<SiteNode> siteNodeFinder = mock(SiteFinder.class);
        when(siteNodeFinder.withRelativePath(eq(parentPath))).thenReturn(siteNodeFinder); // FIXME: anyString()
        when(siteNodeFinder.result()).thenReturn(parentSiteNode);
        when(site.find(eq(SiteNode.class))).thenReturn(siteNodeFinder);

        final String relativeUri = fixture.getRelativeUri();

        assertThat(relativeUri, is(parentUri + "/" + exposedUri));
      }

    // TODO:
//    /*******************************************************************************************************************
//     *
//     ******************************************************************************************************************/
//    @Test
//    public void getRelativeUri_must_be_called_only_once()
//    {
//
//    }
//
  }