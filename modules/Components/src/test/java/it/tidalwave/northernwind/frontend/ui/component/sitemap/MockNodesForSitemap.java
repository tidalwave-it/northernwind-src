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
package it.tidalwave.northernwind.frontend.ui.component.sitemap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import it.tidalwave.util.Finder;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import lombok.RequiredArgsConstructor;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.P_LATEST_MODIFICATION_DATE;
import static it.tidalwave.northernwind.frontend.ui.component.sitemap.SitemapViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class MockNodesForSitemap
  {
    static class CollectingVisitor<T> implements Composite.Visitor<T, List<T>>
      {
        private final List<T> value = new ArrayList<>();

        @Override
        public void visit (@Nonnull final T layout)
          {
            value.add(layout);
          }

        @Override @Nonnull
        public Optional<List<T>> getValue ()
          {
            return Optional.of(value);
          }
      }

    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<SiteNode> createMockNodes (final long seed,
                                           @Nonnegative final int count,
                                           @Nonnull final String uriSegment)
      throws NotFoundException, HttpStatusException
      {
        final var random = new Random(seed);
        final List<SiteNode> nodes = new ArrayList<>();

        IntStream.rangeClosed(1, count).forEach(i ->
          {
            final var node = createMockSiteNode(site);
            final var properties = createMockProperties();
            final var priority = random.nextInt(10) / 10.0f;
            when(properties.getProperty(eq(P_SITEMAP_PRIORITY))).thenReturn(Optional.of(priority));
            when(properties.getProperty(eq(P_SITEMAP_CHILDREN_PRIORITY))).thenReturn(Optional.of(priority));

            final var dateTime = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT"))
                                              .plusMinutes(random.nextInt(10 * 365 * 24 * 60));

            when(properties.getProperty(eq(P_LATEST_MODIFICATION_DATE))).thenReturn(Optional.of(dateTime));
            when(properties.getProperty(eq(P_SITEMAP_CHANGE_FREQUENCY))).thenReturn(Optional.of("daily"));
            when(node.getProperties()).thenReturn(properties);

            final var layout = mock(Layout.class);
            when(layout.getTypeUri()).thenReturn("http://northernwind.tidalwave.it/component/foobar");
            when(node.getLayout()).thenReturn(layout);

            final var childrenLayouts = IntStream.rangeClosed(1, 10).mapToObj(j ->
              {
                try
                  {
                    final var childLayout = mock(Layout.class);
                    when(layout.getTypeUri()).thenReturn("http://northernwind.tidalwave.it/component/foobar-" + j);
                    final var vac = mock(ViewFactory.ViewAndController.class);
                    final var viewController = mock(ViewController.class);
                    when(viewController.findVirtualSiteNodes()).thenCallRealMethod();
                    when(vac.getController()).thenReturn(viewController);
                    when(childLayout.createViewAndController(any(SiteNode.class))).thenReturn(vac);
                    return childLayout;
                  }
                catch (Exception e)
                  {
                    throw new RuntimeException(e);
                  }
              }).collect(toList());

            when(node.getRelativeUri()).thenReturn(ResourcePath.of(String.format(uriSegment, i)));
            when(layout.accept(any(Composite.Visitor.class))).thenAnswer(invocation ->
              {
                final Composite.Visitor<Layout, ?> visitor = invocation.getArgument(0);
                childrenLayouts.forEach(visitor::visit);
                return visitor.getValue();
              });

            nodes.add(node);
          });

        if (!uriSegment.contains("blog"))
          {
            makeBlogNode(nodes.get(8), 32, 30);
          }

        return nodes;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void makeBlogNode (@Nonnull final SiteNode blogNode,
                               final long seed,
                               @Nonnegative final int count)
      throws NotFoundException, HttpStatusException
      {
        when(blogNode.getRelativeUri()).thenReturn(ResourcePath.of("/blog"));
        final Finder<SiteNode> blogFinder = createMockSiteFinder();
        final var blogNodes = createMockNodes(seed, count, "/blog/post-%02d");
        when(blogFinder.results()).thenReturn(blogNodes);

        // Simulate more than one layout that have virtual nodes, so we test that duplicated URLs are coalesced.
        final var layouts = blogNode.getLayout().accept(new CollectingVisitor<>()).get();
        Stream.of(layouts.get(3), layouts.get(4)).forEach(layout ->
          {
            try
              {
                final var viewController = layout.createViewAndController(blogNode).getController();
                when(viewController.findVirtualSiteNodes()).thenReturn(blogFinder);
              }
            catch (Exception e)
              {
                throw new RuntimeException(e);
              }
          });
      }
  }
