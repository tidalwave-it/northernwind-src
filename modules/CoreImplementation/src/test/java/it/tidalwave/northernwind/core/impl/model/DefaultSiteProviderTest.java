/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import lombok.Setter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

// FIXME: should be useless with Mockito, but there's a section which throws NPE
class MockModelFactory implements ModelFactory
  {
    @Setter
    private DefaultSite site;
    
    @Override
    public Site createSite (String contextPath, 
                            String documentPath, 
                            String mediaPath,
                            String libraryPath,
                            String nodePath, 
                            boolean logConfigurationEnabled,
                            List<Locale> configuredLocales, 
                            List<String> ignoredFolders)
      {
        return site;
      }

    @Override
    public Resource createResource(ResourceFile file) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public Content createContent(ResourceFile folder) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public Media createMedia(ResourceFile file) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public SiteNode createSiteNode(Site site, ResourceFile folder)
      throws IOException, NotFoundException 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public Layout createLayout(Id id, String type) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public Request createRequest() 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public Request createRequestFrom(HttpServletRequest httpServletRequest) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    @Override
    public ResourceProperties createProperties(Id id) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }
  }

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteProviderTest 
  {
    private ApplicationContext context;
    
    private DefaultSiteProvider fixture;

    private MockModelFactory modelFactory;
    
    private DefaultSite site;
    
    private WaitingTaskExecutor executor;
    
    private ServletContext servletContext;
            
    @BeforeMethod
    public void setupFixture()
      {
        context = new ClassPathXmlApplicationContext("DefaultSiteProviderTestBeans.xml");
        modelFactory = context.getBean(MockModelFactory.class);
        executor = context.getBean(WaitingTaskExecutor.class);
        servletContext = context.getBean(ServletContext.class);
        site = mock(DefaultSite.class);
        modelFactory.setSite(site);
        when(servletContext.getContextPath()).thenReturn("thecontextpath");
        reset(modelFactory);
//        when(modelFactory.createSite(anyString(), FIXME: throws NPE
//                                     anyString(), 
//                                     anyString(), 
//                                     anyString(), 
//                                     anyString(), 
//                                     any(Boolean.class), 
//                                     any(List.class), 
//                                     any(List.class)))
//                         .thenReturn(site);
      }
    
    @Test
    public void must_properly_create_and_initialize_Site_when_DefaultSiteProvider_is_initialized()
      throws Exception
      {
        fixture = context.getBean(DefaultSiteProvider.class);
        verify(modelFactory).createSite(eq("thecontextpath"), 
                                        eq("testDocumentPath"), 
                                        eq("testMediaPath"), 
                                        eq("testLibraryPath"), 
                                        eq("testNodePath"),
                                        eq(true), 
                                        eq(Arrays.asList(new Locale("en"), new Locale("it"), new Locale("fr"))), 
                                        eq(Arrays.asList("ignored1", "ignored2"))); 
        verify(executor).execute(any(Runnable.class));
        
        assertThat(fixture.getSite(), sameInstance((Site)site));
        assertThat(fixture.isSiteAvailable(), is(false));
        
        executor.doExecute();
        
        verify(site).initialize();
        assertThat(fixture.getSite(), sameInstance((Site)site));
        assertThat(fixture.isSiteAvailable(), is(true));
      }
    
    @Test
    public void must_return_the_correct_version_string()
      {
        fixture = context.getBean(DefaultSiteProvider.class);
        assertThat(fixture.getVersionString(), is(notNullValue()));  
      }
    
    @Test
    public void must_return_the_correct_context_path_in_a_web_environment()
      {
        fixture = context.getBean(DefaultSiteProvider.class);
        assertThat(fixture.getContextPath(), is("thecontextpath"));
      }
  }
