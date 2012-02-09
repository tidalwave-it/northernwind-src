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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class HtmlTemplateBlogViewControllerTest
  {
    private HtmlTemplateBlogViewController fixture;
    
    private BlogView view;
    
    private SiteNode node;
    
    private Site site;
    
    private RequestHolder requestHolder;
    
    private RequestLocaleManager requestLocaleManager;
    
    @BeforeMethod
    public void setupFixture()
      {
        view = mock(BlogView.class);   
        node = mock(SiteNode.class);
        site = mock(Site.class);
        requestHolder = mock(RequestHolder.class);
        requestLocaleManager = mock(RequestLocaleManager.class);
        fixture = new HtmlTemplateBlogViewController(view, node, site, requestHolder, requestLocaleManager);  
      }
    
    @Test(dataProvider="mainTitleTestDataProvider")
    public void must_properly_render_the_main_title (final @Nonnull String viewId,
                                                     final @Nonnull String title, 
                                                     final @Nonnull String expectedRendering) 
      throws Exception
      {
        final Id id = new Id(viewId);
        when(view.getId()).thenReturn(id);
        final ResourceProperties properties = mock(ResourceProperties.class);
        when(node.getPropertyGroup(eq(id))).thenReturn(properties);
        when(properties.getProperty(eq(PROPERTY_TITLE))).thenReturn(title);
          
        final StringBuilder builder = new StringBuilder();
        fixture.renderMainTitle(builder);
        
        assertThat(builder.toString(), is(expectedRendering));
      }
    
    @DataProvider(name="mainTitleTestDataProvider")
    private Object[][] mainTitleTestDataProvider()
      {
        return new Object[][]
          {
            { "id1", "title1",  "<h2>title1</h2>\n"},  
            { "id2", "title 2", "<h2>title 2</h2>\n"},  
            { "id3", "",        ""},  
            { "id4", "  ",      "" } 
          };
      }  
}
