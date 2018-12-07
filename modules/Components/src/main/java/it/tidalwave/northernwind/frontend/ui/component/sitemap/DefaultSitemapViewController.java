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
package it.tidalwave.northernwind.frontend.ui.component.sitemap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import it.tidalwave.util.Key;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_LATEST_MODIFICATION_DATE;
import static it.tidalwave.northernwind.frontend.ui.component.blog.DefaultBlogViewController.TIME0;
import static it.tidalwave.northernwind.frontend.ui.component.sitemap.SitemapViewController.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultSitemapViewController implements SitemapViewController
  {
    @Nonnull
    protected final SitemapView view;

    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        final StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        site.find(SiteNode.class).stream().forEach(siteNode ->
          {
            final Layout layout = siteNode.getLayout();

            // Prevents infinite recursion
            if (!layout.getTypeUri().startsWith("http://northernwind.tidalwave.it/component/Sitemap/"))
              {
                // FIXME: should probably skip children of sitenodes with managePathParams
                // FIXME: for instance, Calendar would benefit
                // FIXME: Would Blog benefit? It should, as it manages its own children
                // FIXME: Places and Themes should move managePathParams=true to each children
                // FIXME: Problem, the root gallery needs managePathParams=true to load images.xml
                log.debug(">>>> sitemap processing {} / layout {} ...", siteNode.getRelativeUri(), layout);

                appendUrl(builder, siteNode, null);

                layout.accept(new VisitorSupport<Layout, Void>()
                  {
                    @Override
                    public void visit (final @Nonnull Layout childLayout)
                      {
                        try
                          {
                            final Object controller = childLayout.createViewAndController(siteNode).getController();

                            if (controller instanceof CompositeSiteNodeController)
                              {
                                for (final SiteNode childSiteNode : ((CompositeSiteNodeController)controller).findVirtualSiteNodes().results())
                                  {
                                    appendUrl(builder, siteNode, childSiteNode);
                                  }
                              }
                          }
                        catch (HttpStatusException e)
                          {
                            log.warn("sitemap for {} threw {}", siteNode.getRelativeUri(), e.toString());
                          }
                        catch (Exception e)
                          {
                            log.warn("Skipped item because of {} - root cause {}", e, rootCause(e).toString());
                          }
                      }

                    @Override
                    public Void getValue()
                      {
                        return null;
                      }
                  });
              }
          });

        builder.append("</urlset>\n");
        view.setContent(builder.toString());
        view.setMimeType("application/xml");
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void appendUrl (final @Nonnull StringBuilder builder,
                            final @Nonnull SiteNode siteNode,
                            final @Nullable SiteNode childSiteNode)
      {
        final SiteNode n = (childSiteNode != null) ? childSiteNode : siteNode;
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final ResourceProperties properties = n.getProperties();
        //
        // FIXME: if you put the sitemap property straightly into the child site node, you can simplify a lot,
        // just using a single property and only peeking into a single node
        final Key<String> priorityKey = (childSiteNode == null) ? P_SITEMAP_PRIORITY : P_SITEMAP_CHILDREN_PRIORITY;
        final float sitemapPriority = Float.parseFloat(siteNode.getProperty(priorityKey).orElse("0.5"));

        if (sitemapPriority > 0)
          {
            builder.append("  <url>\n");
            builder.append(String.format("    <loc>%s</loc>%n", site.createLink(n.getRelativeUri())));
            builder.append(String.format("    <lastmod>%s</lastmod>%n",
                                         properties.getDateTimeProperty(P_LATEST_MODIFICATION_DATE).orElse(TIME0).format(dateTimeFormatter)));
            builder.append(String.format("    <changefreq>%s</changefreq>%n",
                                         properties.getProperty(P_SITEMAP_CHANGE_FREQUENCY).orElse("daily")));
            builder.append(String.format("    <priority>%s</priority>%n", Float.toString(sitemapPriority)));
            builder.append("  </url>\n");
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Throwable rootCause (final @Nonnull Throwable t)
      {
        final Throwable cause = t.getCause();
        return (cause != null) ? rootCause(cause) : t;
      }
  }
