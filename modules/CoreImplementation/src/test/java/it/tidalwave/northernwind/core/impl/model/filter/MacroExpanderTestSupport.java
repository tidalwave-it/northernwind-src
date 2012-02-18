/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model.filter;

import javax.annotation.Nonnull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.model.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.MockSiteNodeSiteFinder;
import org.testng.annotations.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import lombok.RequiredArgsConstructor;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class MacroExpanderTestSupport 
  {
    @Nonnull
    private final String contextName;
    
    protected ApplicationContext context;
    
    protected SiteProvider siteProvider;
    
    protected Site site;
    
    @BeforeClass // FIXME: should be BeforeMethod?
    public void setUp() 
      {
        context = new ClassPathXmlApplicationContext(contextName);
        siteProvider = context.getBean(SiteProvider.class);
        site = context.getBean(Site.class);
        when(siteProvider.getSite()).thenReturn(site);
        
        when(site.find(eq(Content.class))).thenReturn(new MockContentSiteFinder());
        when(site.find(eq(SiteNode.class))).thenReturn(new MockSiteNodeSiteFinder());
        when(site.createLink(anyString())).thenAnswer(new Answer<String>()
          {
            @Override @Nonnull
            public String answer (final @Nonnull InvocationOnMock invocation) 
              {
                return "/LINK/" + invocation.getArguments()[0];
              }
          });
      }
  }
