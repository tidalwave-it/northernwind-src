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

import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController1;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController2;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockController3;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView1;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView2;
import it.tidalwave.northernwind.frontend.impl.ui.mocks.MockView3;
import java.lang.reflect.Constructor;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
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

    private ClassPathXmlApplicationContext context;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("ViewBuilderTestBeans.xml");
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
  }