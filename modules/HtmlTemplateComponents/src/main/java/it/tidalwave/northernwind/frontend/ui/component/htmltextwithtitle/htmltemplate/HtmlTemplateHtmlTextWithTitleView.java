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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.htmltemplate;

import javax.annotation.Nonnull;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.HtmlTextWithTitleView;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import lombok.Getter;
import static java.util.stream.Collectors.joining;

/***********************************************************************************************************************
 *
 * An implementation of {@link HtmlTextWithTitleView} based on HTML templates.
 *
 * @see     HtmlTemplateHtmlTextWithTitleViewController
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/HtmlTextWithTitle/#v1.0",
              controlledBy=HtmlTemplateHtmlTextWithTitleViewController.class)
public class HtmlTemplateHtmlTextWithTitleView extends HtmlHolder implements HtmlTextWithTitleView
  {
    @Getter @Nonnull
    private final Id id;

    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param       id      the id of the view
     * @param       site    the {@link Site}
     *
     ******************************************************************************************************************/
    public HtmlTemplateHtmlTextWithTitleView (final @Nonnull Id id, final @Nonnull Site site)
      {
        super(id);
        this.id = id;
        this.site = site;
      }

    /*******************************************************************************************************************
     *
     * Renders this view. See {@link HtmlTemplateHtmlTextWithTitleViewController} for more information.
     *
     * @see         HtmlTemplateHtmlTextWithTitleViewController
     * @param       wrapperTemplatePath     the optional template to wrap around each item of contents
     * @param       templatePath            the template for rendering
     * @param       contents                the contents to render
     *
     ******************************************************************************************************************/
    public void render (final @Nonnull Optional<ResourcePath> wrapperTemplatePath,
                        final @Nonnull Optional<ResourcePath> templatePath,
                        final @Nonnull Aggregates contents)
      {
        addComponent(new HtmlHolder(contents.stream().map(a ->
          {
            final Template textTemplate = site.getTemplate(getClass(), templatePath, "Text.st");
            final Template wrapperTemplate = site.getTemplate(getClass(), wrapperTemplatePath, "Wrapper.st");

            a.get("title").ifPresent(t -> textTemplate.addAttribute("title", t.toString()));
            a.get("text").ifPresent(t -> textTemplate.addAttribute("text", t.toString()));;
            a.get("level").ifPresent(l -> textTemplate.addAttribute("level", l.toString()));;

            return wrapperTemplate.addAttribute("content", textTemplate.render()).render();
          }).collect(joining("\n"))));
      }
  }
