/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.model.SiteNode._SiteNode_;
import static it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController.TIME0;
import static it.tidalwave.northernwind.frontend.ui.component.sitemap.SitemapViewController.*;

/***********************************************************************************************************************
 *
 * <p>A default implementation of the {@link SitemapViewController} that is independent of the presentation technology.
 * This class is capable to render the sitemap of a {@link Site}.</p>
 *
 * <p>Supported properties of any {@link SiteNode} in the site:</p>
 *
 * <ul>
 * <li>{@code P_SITEMAP_PRIORITY}: the priority of the {@code SiteNode} - if zero, the node is ignored;</li>
 * <li>{@code P_SITEMAP_CHILDREN_PRIORITY}: same as {@code P_SITEMAP_PRIORITY}, but for child nodes;</li>
 * <li>{@code P_LATEST_MODIFICATION_DATE}: the date-time of the latest modification;</li>
 * <li>{@code P_SITEMAP_CHANGE_FREQUENCY}: the supposed change frequency of the {@code SiteNode}.</li>
 * </ul>
 *
 * <p>Concrete implementations must provide one method for rendering the calendar:</p>
 *
 * <ul>
 * <li>{@link #render(java.util.List)}</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class DefaultSitemapViewController implements SitemapViewController
  {
    @RequiredArgsConstructor @ToString @Getter @EqualsAndHashCode
    protected static class Entry implements Comparable<Entry>
      {
        @Nonnull
        private final String location;

        @Nonnull
        private final ZonedDateTime lastModification;

        @Nonnull
        private final String changeFrequency;

        private final float priority;

        @Override
        public int compareTo (@Nonnull final Entry other)
          {
            return this.equals(other) ? 0 : this.location.compareTo(other.location);
          }
      }

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final SitemapView view;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (@Nonnull final RenderContext context)
      {
        final SortedSet<Entry> entries = new TreeSet<>();

        siteNode.getSite().find(_SiteNode_).stream().forEach(node ->
          {
            final Layout layout = node.getLayout();

            // Prevents infinite recursion
            if (!layout.getTypeUri().startsWith("http://northernwind.tidalwave.it/component/Sitemap/"))
              {
                // FIXME: should probably skip children of sitenodes with managePathParams
                // FIXME: for instance, Calendar would benefit
                // FIXME: Would Blog benefit? It should, as it manages its own children
                // FIXME: Places and Themes should move managePathParams=true to each children
                // FIXME: Problem, the root gallery needs managePathParams=true to load images.xml
                log.debug(">>>> sitemap processing {} / layout {} ...", node.getRelativeUri(), layout);

                newEntry(node, null).ifPresent(entries::add);

                layout.accept(new VisitorSupport<Layout, Void>()
                  {
                    @Override
                    public void visit (@Nonnull final Layout childLayout)
                      {
                        try
                          {
                            entries.addAll(childLayout.createViewAndController(node).getController()
                                .findVirtualSiteNodes()
                                .results()
                                .stream()
                                .peek(e -> log.debug(">>>>>>>> added virtual node: {}", e.getRelativeUri()))
                                .flatMap(childNode -> newEntry(node, childNode).stream())
                                .collect(toList()));
                          }
                        catch (HttpStatusException e)
                          {
                            log.warn("sitemap for {} threw {}", node.getRelativeUri(), e.toString());
                          }
                        catch (Exception e)
                          {
                            log.warn("Skipped item because of {} - root cause {}", e, rootCause(e).toString());
                          }
                      }

                    @Nonnull
                    @Override @SuppressWarnings("findbugs:NP_NONNULL_RETURN_VIOLATION")
                    public Void getValue()
                      {
                        return null;
                      }
                  });
              }
          });

        render(entries);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    protected abstract void render (@Nonnull Set<Entry> entries);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final ResourceProperties getViewProperties()
      {
        return siteNode.getPropertyGroup(view.getId());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<Entry> newEntry (@Nonnull final SiteNode siteNode, @CheckForNull final SiteNode childSiteNode)
      {
        final SiteNode node = (childSiteNode != null) ? childSiteNode : siteNode;
        final ResourceProperties properties = node.getProperties();
        //
        // FIXME: if you put the sitemap property straightly into the child site node, you can simplify a lot,
        // just using a single property and only peeking into a single node
        final Key<Float> priorityKey = (childSiteNode == null) ? P_SITEMAP_PRIORITY : P_SITEMAP_CHILDREN_PRIORITY;
        final float sitemapPriority = siteNode.getProperty(priorityKey).orElse(0.5f);

        return (sitemapPriority == 0)
                ? Optional.empty()
                : Optional.of(new Entry(siteNode.getSite().createLink(node.getRelativeUri()),
                                  properties.getProperty(P_LATEST_MODIFICATION_DATE).orElse(TIME0),
                                  properties.getProperty(P_SITEMAP_CHANGE_FREQUENCY).orElse("daily"),
                                  sitemapPriority));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Throwable rootCause (@Nonnull final Throwable t)
      {
        final Throwable cause = t.getCause();
        return (cause != null) ? rootCause(cause) : t;
      }
  }
