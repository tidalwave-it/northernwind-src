/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.io.IOException;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Site;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.test.SiteBuilderMatcher;
import it.tidalwave.northernwind.core.impl.test.WaitingTaskExecutor;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteProviderTest
  {
    private ClassPathXmlApplicationContext context;

    private DefaultSiteProvider underTest;

    private DefaultSite site;

    private WaitingTaskExecutor executor;

    private ServletContext servletContext;

    private Site.Builder.CallBack siteBuilderCallback;

    private static final String SERVLET_CONTEXT_PATH = "thecontextpath";

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteProviderTest/TestBeans.xml");
        executor = context.getBean(WaitingTaskExecutor.class);
        servletContext = context.getBean(ServletContext.class);
        site = mock(DefaultSite.class);

        siteBuilderCallback = mock(Site.Builder.CallBack.class);
        when(siteBuilderCallback.build(any(Site.Builder.class))).thenReturn(site);
        final ModelFactory modelFactory = context.getBean(ModelFactory.class);
        final Site.Builder builder = new Site.Builder(modelFactory, siteBuilderCallback);
        when(modelFactory.createSite()).thenReturn(builder);

        when(servletContext.getContextPath()).thenReturn(SERVLET_CONTEXT_PATH);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_the_Site()
      throws Exception
      {
        // given
        underTest = context.getBean(DefaultSiteProvider.class);
        // then
        verify(siteBuilderCallback).build(argThat(new SiteBuilderMatcher()
                .withContextPath(SERVLET_CONTEXT_PATH)
                .withDocumentPath("testDocumentPath")
                .withMediaPath("testMediaPath")
                .withLibraryPath("testLibraryPath")
                .withNodePath("testNodePath")
                .withConfigurationEnabled(true)
                .withConfiguredLocales(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr")))
                .withIgnoredFolders(Arrays.asList("ignored1", "ignored2"))));
        // the executor that initializes Site hasn't started here
        assertThat(underTest.getSite(), sameInstance(site));
        assertThat(underTest.isSiteAvailable(), is(false));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_and_initialize_the_Site()
      throws Exception
      {
        // given
        underTest = context.getBean(DefaultSiteProvider.class);
        // when
        executor.doExecute(); // initializes Site
        // then
        verify(site).initialize();
        assertThat(underTest.getSite(), sameInstance(site));
        assertThat(underTest.isSiteAvailable(), is(true));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_the_correct_version_string()
      {
        // given
        underTest = context.getBean(DefaultSiteProvider.class);
        // then
        assertThat(underTest.getVersionString(), is(notNullValue()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_the_correct_context_path_in_a_web_environment()
      {
        // given
        underTest = context.getBean(DefaultSiteProvider.class);
        // then
        assertThat(underTest.getContextPath(), is(SERVLET_CONTEXT_PATH));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_use_no_context_path_when_ServletContext_is_not_available()
      throws Exception
      {
        // given
        ((DefaultListableBeanFactory)context.getBeanFactory()).removeBeanDefinition("servletContext");
        underTest = context.getBean(DefaultSiteProvider.class);
        // then
        assertThat(underTest.getContextPath(), is("/"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_non_null_site_even_in_cause_of_initialization_failure()
      throws Exception
      {
        // given
        underTest = context.getBean(DefaultSiteProvider.class);
        doThrow(new IOException("Simulated error in initialization")).when(site).initialize();
        // when
        executor.doExecute(); // emulate Site initialization in background
        // then
        assertThat(underTest.getSite(), sameInstance(site));
        assertThat(underTest.isSiteAvailable(), is(false));
      }
  }
