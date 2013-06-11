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
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.util.Id;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

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
        when(resourceFile.getPath()).thenReturn("/resourceFile");
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
  }