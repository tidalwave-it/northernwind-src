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
package it.tidalwave.northernwind.frontend.ui.component.sitemap.htmltemplate;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.DefaultSitemapViewController;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.SitemapViewController;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link SitemapViewController} based on HTML templates.</p>
 *
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
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateSitemapViewController extends DefaultSitemapViewController
  {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Nonnull
    private final HtmlTemplateSitemapView view;

    public HtmlTemplateSitemapViewController (@Nonnull final SiteNode siteNode,
                                              @Nonnull final HtmlTemplateSitemapView view)
      {
        super(siteNode, view);
        this.view = view;
      }

    @Override
    protected void render (@Nonnull final Set<? extends Entry> entries)
      {
        view.setMimeType("application/xml");
        view.render(getViewProperties().getProperty(P_SITEMAP_TEMPLATE_PATH),
                    entries.stream().map(HtmlTemplateSitemapViewController::toAggregate).collect(toAggregates("entries")));
      }

    @Nonnull
    private static Aggregate toAggregate (@Nonnull final Entry entry)
      {
        return Aggregate.of("location",         entry.getLocation())
                      .with("lastModification", entry.getLastModification().format(FORMATTER))
                      .with("changeFrequency",  entry.getChangeFrequency())
                      .with("priority",         Float.toString(entry.getPriority()));
      }
  }
