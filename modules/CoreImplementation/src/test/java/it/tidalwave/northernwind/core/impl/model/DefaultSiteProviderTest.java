/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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

import java.util.Arrays;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Site;
import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteProviderTest
  {
    private ClassPathXmlApplicationContext context;

    private DefaultSiteProvider fixture;

    private DefaultSite site;

    private WaitingTaskExecutor executor;

    private ServletContext servletContext;

    private Site.Builder.CallBack siteBuilderCallback;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteProviderTestBeans.xml");
        executor = context.getBean(WaitingTaskExecutor.class);
        servletContext = context.getBean(ServletContext.class);
        site = mock(DefaultSite.class);

        siteBuilderCallback = mock(Site.Builder.CallBack.class);
        when(siteBuilderCallback.build(any(Site.Builder.class))).thenReturn(site);
        final ModelFactory modelFactory = context.getBean(ModelFactory.class);
        final Site.Builder builder = new Site.Builder(modelFactory, siteBuilderCallback);
        when(modelFactory.createSite()).thenReturn(builder);

        when(servletContext.getContextPath()).thenReturn("thecontextpath");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_and_initialize_Site_when_DefaultSiteProvider_is_initialized()
      throws Exception
      {
        fixture = context.getBean(DefaultSiteProvider.class);

        verify(siteBuilderCallback).build(argThat(new SiteBuilderMatcher()
                .withContextPath("thecontextpath")
                .withDocumentPath("testDocumentPath")
                .withMediaPath("testMediaPath")
                .withLibraryPath("testLibraryPath")
                .withNodePath("testNodePath")
                .withConfigurationEnabled(true)
                .withConfiguredLocales(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr")))
                .withIgnoredFolders(Arrays.asList("ignored1", "ignored2"))));

        verify(executor).execute(any(Runnable.class)); // FIXME: needed?

        assertThat(fixture.getSite(), sameInstance((Site)site));
        assertThat(fixture.isSiteAvailable(), is(false));

        executor.doExecute(); // emulate Site initialization in background

        verify(site).initialize();
        assertThat(fixture.getSite(), sameInstance((Site)site));
        assertThat(fixture.isSiteAvailable(), is(true));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_the_correct_version_string()
      {
        fixture = context.getBean(DefaultSiteProvider.class);
        assertThat(fixture.getVersionString(), is(notNullValue()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_the_correct_context_path_in_a_web_environment()
      {
        fixture = context.getBean(DefaultSiteProvider.class);
        assertThat(fixture.getContextPath(), is("thecontextpath"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_use_no_context_path_when_ServletContext_is_not_available()
      throws Exception
      {
        ((DefaultListableBeanFactory)context.getBeanFactory()).removeBeanDefinition("servletContext");

        fixture = context.getBean(DefaultSiteProvider.class);

        assertThat(fixture.getContextPath(), is("/"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_non_null_site_even_in_cause_of_initialization_failure()
      throws Exception
      {
        doThrow(new IOException("test")).when(site).initialize();

        fixture = context.getBean(DefaultSiteProvider.class);
        executor.doExecute(); // emulate Site initialization in background

        assertThat(fixture.getSite(), sameInstance((Site)site));
        assertThat(fixture.isSiteAvailable(), is(false));
      }
  }
