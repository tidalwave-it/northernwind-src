/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.impl.ui;

import org.springframework.context.ApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.RequiredArgsConstructor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.TestHelper;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockService2;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockService1;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ViewBuilderTest
  {
    @RequiredArgsConstructor
    public static class MockView
      {
        public final Id id;
        public final SiteNode siteNode;
        public final Site site;
        public final MockService1 service1;
      }

    @RequiredArgsConstructor
    public static class MockController
      {
        public final Id id;
        public final SiteNode siteNode;
        public final MockView view;
        public final Site site;
        public final MockService2 service2;
      }

    private final TestHelper helper = new TestHelper(this);

    private ViewBuilder underTest;

    private ApplicationContext context;

    private SiteNode siteNode;

    private Id id;

    private Site site;

    private SiteProvider siteProvider;

    private MockService1 service1;

    private MockService2 service2;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        context = helper.createSpringContext();
        siteProvider = context.getBean(SiteProvider.class);
        service1 = context.getBean(MockService1.class);
        service2 = context.getBean(MockService2.class);

        siteNode = mock(SiteNode.class);
        when(siteProvider.getSite()).thenReturn(site);

        id = new Id("theId");

        underTest = new ViewBuilder(MockView.class, MockController.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_instantiate_view_and_controller()
      throws Exception
      {
        // when
        final ViewAndController viewAndController = underTest.createViewAndController(id, siteNode);
        // then
        final Object oController = viewAndController.getController();
        final Object oView = viewAndController.getView();

        assertThat(oController, is(not(nullValue())));
        assertThat(oView, is(not(nullValue())));
        assertThat(oController, is(instanceOf(MockController.class)));
        assertThat(oView, is(instanceOf(MockView.class)));

        final MockController controller = (MockController)oController;
        final MockView view = (MockView)oView;

        assertThat(controller.view, is(sameInstance(view)));
        assertThat(controller.siteNode, is(sameInstance(siteNode)));
        assertThat(controller.id, is(id));
        assertThat(controller.site, is(sameInstance(site)));
        assertThat(controller.service2, is(sameInstance(service2)));

        assertThat(view.siteNode, is(sameInstance(siteNode)));
        assertThat(view.id, is(id));
        assertThat(view.site, is(sameInstance(site)));
        assertThat(view.service1, is(sameInstance(service1)));
      }
  }