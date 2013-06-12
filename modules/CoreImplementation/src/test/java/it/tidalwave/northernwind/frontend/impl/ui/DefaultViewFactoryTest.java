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
package it.tidalwave.northernwind.frontend.impl.ui;

import java.lang.reflect.Constructor;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController1;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController2;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController3;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView1;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView2;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView3;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultViewFactoryTest
  {
    private DefaultViewFactory fixture;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        fixture = new DefaultViewFactory();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_discover_and_properly_register_annotated_views()
      throws Exception
      {
        fixture.setLogConfigurationEnabled(true);
        fixture.initialize();

        assertThat(fixture.viewBuilderMapByTypeUri.size(), is(3));

        final ViewBuilder viewBuilder1 = fixture.viewBuilderMapByTypeUri.get("type1");
        assertThat(viewBuilder1, is(not(nullValue())));
        assertThat(viewBuilder1.viewConstructor,           is((Constructor)MockView1.class.getConstructors()[0]));
        assertThat(viewBuilder1.viewControllerConstructor, is((Constructor)MockController1.class.getConstructors()[0]));

        final ViewBuilder viewBuilder2 = fixture.viewBuilderMapByTypeUri.get("type2");
        assertThat(viewBuilder2, is(not(nullValue())));
        assertThat(viewBuilder2.viewConstructor,           is((Constructor)MockView2.class.getConstructors()[0]));
        assertThat(viewBuilder2.viewControllerConstructor, is((Constructor)MockController2.class.getConstructors()[0]));

        final ViewBuilder viewBuilder3 = fixture.viewBuilderMapByTypeUri.get("type3");
        assertThat(viewBuilder3, is(not(nullValue())));
        assertThat(viewBuilder3.viewConstructor,           is((Constructor)MockView3.class.getConstructors()[0]));
        assertThat(viewBuilder3.viewControllerConstructor, is((Constructor)MockController3.class.getConstructors()[0]));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void createViewAndController_must_delegate_to_the_proper_ViewBuilder()
      throws Exception
      {
        final ViewBuilder viewBuilder = mock(ViewBuilder.class);
        final SiteNode siteNode = mock(SiteNode.class);
        final Id id = new Id("theId");
        final ViewAndController viewAndController = mock(ViewAndController.class);
        when(viewBuilder.createViewAndController(eq(id), same(siteNode))).thenReturn(viewAndController);
        fixture.viewBuilderMapByTypeUri.put("type1", viewBuilder);

        final ViewAndController vac = fixture.createViewAndController("type1", id, siteNode);

        verify(viewBuilder).createViewAndController(eq(id), same(siteNode));
        assertThat(vac, is(sameInstance(viewAndController)));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "\\QCannot find type2: available: [type1]\\E")
    public void createViewAndController_must_throw_exception_when_type_is_not_registered()
      throws Exception
      {
        final ViewBuilder viewBuilder = mock(ViewBuilder.class);
        final SiteNode siteNode = mock(SiteNode.class);
        final Id id = new Id("theId");
        fixture.viewBuilderMapByTypeUri.put("type1", viewBuilder);

        final ViewAndController vac = fixture.createViewAndController("type3", id, siteNode);
      }
  }