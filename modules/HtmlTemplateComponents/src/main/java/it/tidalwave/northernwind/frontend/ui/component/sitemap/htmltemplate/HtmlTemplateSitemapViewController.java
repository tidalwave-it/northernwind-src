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
package it.tidalwave.northernwind.frontend.ui.component.sitemap.htmltemplate;

import javax.annotation.Nonnull;
import java.util.List;
import java.time.format.DateTimeFormatter;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.DefaultSitemapViewController;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;
<<<<<<< HEAD
import static it.tidalwave.northernwind.frontend.ui.component.Properties.P_TEMPLATE_PATH;
=======
>>>>>>> fix-for-NW-259

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link SitemapViewController} based on HTML templates.</p>
 *
<<<<<<< HEAD
 * <p>The template for rendering the page can be specified by means of the property {@code P_TEMPLATE_PATH}.</p>
=======
 * <p>The template for rendering the page can be specified by means of the property {@code P_SITEMAP_TEMPLATE_PATH}.</p>
 *
 * <p>This controller calls render methods to the view by passing {@link Aggregates} to be used with templates.</p>
 * <p>In case of post rendering, the following aggregates are defined:</p>
 *
 * <ul>
 * <li>{@code entries}: the entries to be rendered.</li>
 * </ul>
 *
 * <p>Each item is an {@link Aggregate} of the following fields:</p>
 *
 * <ul>
 * <li>{@code location}: the URL of the page;</li>
 * <li>{@code lastModification}: the last modification date of the page;</li>
 * <li>{@code changeFrequency}: the change frequency of the page;</li>
 * <li>{@code priority}: the priority of the page.</li>
 * </ul>
>>>>>>> fix-for-NW-259
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateSitemapViewController extends DefaultSitemapViewController
  {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Nonnull
    private final HtmlTemplateSitemapView view;

    public HtmlTemplateSitemapViewController (final @Nonnull SiteNode siteNode,
                                              final @Nonnull HtmlTemplateSitemapView view)
      {
        super(siteNode, view);
        this.view = view;
      }

    @Override
    protected void render (final @Nonnull List<Entry> entries)
      {
        view.setMimeType("application/xml");
<<<<<<< HEAD
        view.render(getViewProperties().getProperty(P_TEMPLATE_PATH),
=======
        view.render(getViewProperties().getProperty(P_SITEMAP_TEMPLATE_PATH),
>>>>>>> fix-for-NW-259
                    entries.stream().map(this::toAggregate).collect(toAggregates("entries")));
      }

    @Nonnull
    private Aggregate toAggregate (final @Nonnull Entry entry)
      {
        return Aggregate.of("location",         entry.getLocation())
                      .with("lastModification", entry.getLastModification().format(FORMATTER))
                      .with("changeFrequency",  entry.getChangeFrequency())
                      .with("priority",         Float.toString(entry.getPriority()));
      }
  }
