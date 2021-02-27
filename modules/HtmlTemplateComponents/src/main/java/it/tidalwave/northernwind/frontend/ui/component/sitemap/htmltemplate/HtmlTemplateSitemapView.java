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
package it.tidalwave.northernwind.frontend.ui.component.sitemap.htmltemplate;

import javax.annotation.Nonnull;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.htmltemplate.HtmlTemplateHtmlFragmentView;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.sitemap.SitemapView;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link SitemapView} based on HTML templates.</p>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Sitemap/#v1.0",
              controlledBy=HtmlTemplateSitemapViewController.class)
public class HtmlTemplateSitemapView extends HtmlTemplateHtmlFragmentView implements SitemapView
  {
    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     *
     * @param       id              the id of this view
     * @param       site            the site
     *
     ******************************************************************************************************************/
    public HtmlTemplateSitemapView (final @Nonnull Id id, final @Nonnull Site site)
      {
        super(id);
        this.site= site;
      }

    /*******************************************************************************************************************
     *
     * Renders the sitemap contents. See {@link HtmlTemplateSitemapViewController} for more information.
     *
     * @see         HtmlTemplateSitemapViewController
     * @param       templatePath    the path of an optional template for the rendering
     * @param       entries         the entries to render
     *
     ******************************************************************************************************************/
    public void render (final @Nonnull Optional<ResourcePath> templatePath, final @Nonnull Aggregates entries)
      {
        final Template template = site.getTemplate(getClass(), templatePath, "Sitemap.st");
        addComponent(new HtmlHolder(template.render(entries)));
      }
  }
