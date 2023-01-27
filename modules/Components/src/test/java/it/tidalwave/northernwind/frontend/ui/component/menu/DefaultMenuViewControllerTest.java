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
package it.tidalwave.northernwind.frontend.ui.component.menu;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.model.SiteNode.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;
import static it.tidalwave.northernwind.frontend.ui.component.menu.MenuViewController.P_LINKS;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultMenuViewControllerTest
  {
    private Site site;

    private MenuView view;

    private DefaultMenuViewController underTest;

    private ResourceProperties viewProperties;

    private RenderContext renderContext;

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

        when(site.createLink(any(ResourcePath.class))).then(invocation ->
          {
            final ResourcePath path = invocation.getArgument(0);
            return String.format("http://acme.com%s", path.asString());
          });

        viewProperties = createMockProperties();
        final var siteNode = createMockSiteNode(site);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        view = mock(MenuView.class);
        when(view.getId()).thenReturn(viewId);

        renderContext = new DefaultRenderContext(mock(Request.class), mock(RequestContext.class));

        underTest = new DefaultMenuViewController(view, siteNode);
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
    public void must_properly_set_the_title()
      {
        // given
        when(viewProperties.getProperty(P_TITLE)).thenReturn(Optional.of("the title"));
        // when
        underTest.renderView(renderContext);
        // then
        verify(view).setTitle("the title");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_title_when_unspecified()
      {
        // when
        underTest.renderView(renderContext);
        // then
        verify(view, never()).setTitle(anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_add_the_links()
      throws Exception
      {
        // given
        when(viewProperties.getProperty(P_LINKS)).thenReturn(Optional.of(
                List.of("/node1", "/node2", "/nonexistentNode", "/node3")));
        mockProperty(_SiteNode_, ResourcePath.of("/node1"), P_NAVIGATION_LABEL, "Node 1 title");
        mockProperty(_SiteNode_, ResourcePath.of("/node2"), P_NAVIGATION_LABEL, "Node 2 title");
        // no property for node3
        // when
        underTest.renderView(renderContext);
        // then
        final var inOrder = inOrder(view);
        inOrder.verify(view).addLink("Node 1 title", "http://acme.com/URI-node1");
        inOrder.verify(view).addLink("Node 2 title", "http://acme.com/URI-node2");
        inOrder.verify(view).addLink("no nav. label", "http://acme.com/URI-node3");
        inOrder.verifyNoMoreInteractions();
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
