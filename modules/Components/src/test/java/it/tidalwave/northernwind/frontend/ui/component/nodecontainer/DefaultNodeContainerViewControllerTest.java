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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.stubbing.Answer;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.model.SiteNode._SiteNode_;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;
import static it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultNodeContainerViewControllerTest
  {
    private Site site;

    private NodeContainerView view;

    private DefaultNodeContainerViewController underTest;

    private ResourceProperties nodeProperties;

    private ResourceProperties viewProperties;

    private RenderContext renderContext;

    private final Answer<Void> logInvocation = invocation ->
      {
        log.info(">>>> view.addAttribute(\"{}\", \"{}\")", invocation.getArgument(0), invocation.getArgument(1));
        return null;
      };

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      throws Exception
      {
        final var viewId = new Id("id");

        site = createMockSite();
        MockSiteNodeSiteFinder.registerTo(site);
        MockContentSiteFinder.registerTo(site);

        nodeProperties = createMockProperties();
        viewProperties = createMockProperties();

        final var siteNode = createMockSiteNode(site);
        when(siteNode.getProperties()).thenReturn(nodeProperties);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        view = mock(NodeContainerView.class);
        when(view.getId()).thenReturn(viewId);
        doAnswer(logInvocation).when(view).addAttribute(any(String.class), any(String.class));

        final var requestLocaleManager = mock(RequestLocaleManager.class);
        when(requestLocaleManager.getLocales()).thenReturn(List.of(Locale.US));

        final var requestContext = mock(RequestContext.class);
        when(requestContext.getNodeProperties()).thenReturn(nodeProperties);

        renderContext = new DefaultRenderContext(mock(Request.class), requestContext);

        underTest = new DefaultNodeContainerViewController(view, siteNode, requestLocaleManager);
        underTest.initialize();
        underTest.prepareRendering(renderContext);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_template()
      throws Exception
      {
        // given
        final var templateContent = "the template content";
        final var templatePath = ResourcePath.of("/path/to/template");
        when(viewProperties.getProperty(P_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        mockProperty(_Content_, templatePath, P_TEMPLATE, templateContent);
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).setTemplate(templateContent);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_template_when_no_property_set()
      throws Exception
      {
        // given
        final var templatePath = ResourcePath.of("/path/to/template");
        when(viewProperties.getProperty(P_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        // don't set P_TEMPLATE
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).setTemplate(anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_template_when_no_Content()
      throws Exception
      {
        // given
        final var templatePath = ResourcePath.of("/path/to/nonexistent/template");
        when(viewProperties.getProperty(P_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).setTemplate(anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_language()
      throws Exception
      {
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("language", Locale.US.getLanguage());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_title_prefix()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_TITLE_PREFIX)).thenReturn(Optional.of("the title prefix"));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("titlePrefix", "the title prefix");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_title_prefix_when_unspecified()
      throws Exception
      {
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).addAttribute(eq("titlePrefix"), anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_descriptiond()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_DESCRIPTION)).thenReturn(Optional.of("the description"));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("description", "the description");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_description_when_unspecified()
      throws Exception
      {
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).addAttribute(eq("description"), anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_title()
      throws Exception
      {
        // given
        when(nodeProperties.getProperty(P_TITLE)).thenReturn(Optional.of("the title"));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("title", "the title");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_title_when_unspecified()
      throws Exception
      {
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).addAttribute(eq("title"), anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_screenCssSection()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_SCREEN_STYLE_SHEETS))
                .thenReturn(Optional.of(List.of("/css/1.css", "/css/2.css")));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("screenCssSection",
            "<link rel=\"stylesheet\" media=\"screen\" href=\"http://acme.com/css/1.css\" type=\"text/css\" />\n" +
            "<link rel=\"stylesheet\" media=\"screen\" href=\"http://acme.com/css/2.css\" type=\"text/css\" />\n");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_printCssSection()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_PRINT_STYLE_SHEETS))
                .thenReturn(Optional.of(List.of("/css/1.css", "/css/2.css")));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("printCssSection",
            "<link rel=\"stylesheet\" media=\"print\" href=\"http://acme.com/css/1.css\" type=\"text/css\" />\n" +
            "<link rel=\"stylesheet\" media=\"print\" href=\"http://acme.com/css/2.css\" type=\"text/css\" />\n");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_rssFeeds()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_RSS_FEEDS)).thenReturn(Optional.of(
                List.of("/feed1", "/feed2", "/nonexistentFeed", "/feed3")));
        mockProperty(_SiteNode_, ResourcePath.of("/feed1"), P_TITLE, "Feed 1 title");
        mockProperty(_SiteNode_, ResourcePath.of("/feed2"), P_TITLE, "Feed 2 title");
        // no property for feed3
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("rssFeeds",
            "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Feed 1 title\" href=\"http://acme.com/URI-feed1/\" />\n" +
            "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Feed 2 title\" href=\"http://acme.com/URI-feed2/\" />\n" +
            "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"http://acme.com/URI-feed3/\" />\n");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_scripts()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_SCRIPTS)).thenReturn(Optional.of(List.of("/js/1.js", "/js/2.js")));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("scripts",
            "<script type=\"text/javascript\" src=\"http://acme.com/js/1.js\"></script>\n" +
            "<script type=\"text/javascript\" src=\"http://acme.com/js/2.js\"></script>\n");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_inlined_scripts()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_INLINED_SCRIPTS)).thenReturn(Optional.of(
                List.of("/script1", "/script2", "/nonexistentScript", "/script3")));
        mockProperty(_Content_, ResourcePath.of("/script1"), P_TEMPLATE, "<script>1</script>");
        mockProperty(_Content_, ResourcePath.of("/script2"), P_TEMPLATE, "<script>2</script>");
        // no property for script3
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).addAttribute("inlinedScripts",
            "<script>1</script>" +
            "<script>2</script>"); // TODO: missing newlines
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private <T> void mockProperty (@Nonnull final Class<? extends Resource> type,
                                   @Nonnull final ResourcePath path,
                                   @Nonnull final Key<T> key,
                                   @Nonnull final T value)
      throws NotFoundException
      {
        final var properties = site.find(type).withRelativePath(path).result().getProperties();
        when(properties.getProperty(eq(key))).thenReturn(Optional.of(value));
      }
  }
