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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.menu;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import it.tidalwave.util.Key;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.core.model.SiteNode.PROPERTY_NAVIGATION_LABEL;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.menu.MenuViewController.PROPERTY_LINKS;

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

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      {
        final Id viewId = new Id("id");

        site = mock(Site.class);
        MockSiteNodeSiteFinder.registerTo(site);
        MockContentSiteFinder.registerTo(site);

        when(site.createLink(any(ResourcePath.class))).then(invocation ->
          {
            final ResourcePath path = invocation.getArgument(0);
            return String.format("http://acme.com%s", path.asString());
          });

        viewProperties = createMockProperties();

        final SiteNode siteNode = mock(SiteNode.class);
        when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(viewProperties);

        view = mock(MenuView.class);
        when(view.getId()).thenReturn(viewId);

        underTest = new DefaultMenuViewController(view, siteNode, site);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_set_the_template()
      throws NotFoundException
      {
        // given
        final String templateContent = "the template content";
        final String templatePath = "/path/to/template";
        when(viewProperties.getProperty(PROPERTY_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        stubProperty(Content, templatePath, PROPERTY_TEMPLATE, templateContent);
        // when
        underTest.initialize();
        // then
        verify(view).setTemplate(templateContent);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_template_when_no_property_set()
      throws NotFoundException
      {
        // given
        final String templatePath = "/path/to/template";
        when(viewProperties.getProperty(PROPERTY_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        // don't set PROPERTY_TEMPLATE
        // when
        underTest.initialize();
        // then
        verify(view, never()).setTemplate(anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_not_set_the_template_when_no_Content()
      throws NotFoundException
      {
        // given
        final String templatePath = "/path/to/inexistent/template";
        when(viewProperties.getProperty(PROPERTY_TEMPLATE_PATH)).thenReturn(Optional.of(templatePath));
        // when
        underTest.initialize();
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
        when(viewProperties.getProperty(PROPERTY_TITLE)).thenReturn(Optional.of("the title"));
        // when
        underTest.initialize();
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
        underTest.initialize();
        // then
        verify(view, never()).setTitle(anyString());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_add_the_links()
      throws NotFoundException
      {
        // given
        when(viewProperties.getProperty(PROPERTY_LINKS)).thenReturn(Optional.of(
                Arrays.asList("/node1", "/node2", "/inexistentNode", "/node3")));
        stubProperty(SiteNode, "/node1", PROPERTY_NAVIGATION_LABEL, "Node 1 title");
        stubProperty(SiteNode, "/node2", PROPERTY_NAVIGATION_LABEL, "Node 2 title");
        // no property for node3
        // when
        underTest.initialize();
        // then
        final InOrder inOrder = inOrder(view);
        inOrder.verify(view).addLink("Node 1 title", "http://acme.com/URI-node1");
        inOrder.verify(view).addLink("Node 2 title", "http://acme.com/URI-node2");
        inOrder.verify(view).addLink("no nav. label", "http://acme.com/URI-node3");
        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private <T> void stubProperty (final @Nonnull Class<? extends Resource> type,
                                   final @Nonnull String relativePath,
                                   final @Nonnull Key<T> key,
                                   final @Nonnull T value)
      throws NotFoundException
      {
        final ResourceProperties properties = site.find(type).withRelativePath(relativePath).result().getProperties();
        when(properties.getProperty(eq(key))).thenReturn(Optional.of(value));
      }
  }
