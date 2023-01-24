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
package it.tidalwave.northernwind.frontend.impl.ui;

import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockController1;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockController2;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockController3;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockView1;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockView2;
import it.tidalwave.northernwind.frontend.impl.ui.mock.MockView3;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultViewFactoryTest
  {
    private DefaultViewFactory underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        underTest = new DefaultViewFactory();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_discover_and_properly_register_annotated_views()
      {
        // when
        underTest.setLogConfigurationEnabled(true);
        underTest.initialize();
        // then
        assertThat(underTest.viewBuilderMapByTypeUri.size(), is(3));

        final var viewBuilder1 = underTest.viewBuilderMapByTypeUri.get("type1");
        assertThat(viewBuilder1, is(not(nullValue())));
        assertThat(viewBuilder1.viewConstructor,           is(MockView1.class.getConstructors()[0]));
        assertThat(viewBuilder1.viewControllerConstructor, is(MockController1.class.getConstructors()[0]));

        final var viewBuilder2 = underTest.viewBuilderMapByTypeUri.get("type2");
        assertThat(viewBuilder2, is(not(nullValue())));
        assertThat(viewBuilder2.viewConstructor,           is(MockView2.class.getConstructors()[0]));
        assertThat(viewBuilder2.viewControllerConstructor, is(MockController2.class.getConstructors()[0]));

        final var viewBuilder3 = underTest.viewBuilderMapByTypeUri.get("type3");
        assertThat(viewBuilder3, is(not(nullValue())));
        assertThat(viewBuilder3.viewConstructor,           is(MockView3.class.getConstructors()[0]));
        assertThat(viewBuilder3.viewControllerConstructor, is(MockController3.class.getConstructors()[0]));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void createViewAndController_must_delegate_to_the_proper_ViewBuilder()
      throws Exception
      {
        // given
        final var viewBuilder = mock(ViewBuilder.class);
        final var site = createMockSite();
        final var siteNode = createMockSiteNode(site);
        final var id = new Id("theId");
        final var viewAndController = mock(ViewAndController.class);
        when(viewBuilder.createViewAndController(eq(id), same(siteNode))).thenReturn(viewAndController);
        underTest.viewBuilderMapByTypeUri.put("type1", viewBuilder);
        // when
        final var vac = underTest.createViewAndController("type1", id, siteNode);
        // then
        verify(viewBuilder).createViewAndController(eq(id), same(siteNode));
        assertThat(vac, is(sameInstance(viewAndController)));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(enabled = false, // FIXME: often, but not always, fails
          expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "\\QCannot find unregisteredType: available: [registeredType]\\E")
    public void createViewAndController_must_throw_exception_when_type_is_not_registered()
      throws Exception
      {
        // given
        final var viewBuilder = mock(ViewBuilder.class);
        final var site = createMockSite();
        final var siteNode = createMockSiteNode(site);
        final var id = new Id("theId");
        underTest.viewBuilderMapByTypeUri.put("registeredType", viewBuilder);
        // then
        final var vac = underTest.createViewAndController("unregisteredType", id, siteNode);
      }
  }
