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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * <p>A default implementation of the {@link HtmlTextWithTitleViewController} that is independent of the presentation
 * technology. This class is capable to render a sequence of texts with their titles.</p>
 *
 * <p>Supported properties of the {@link SiteNode}:</p>
 *
 * <ul>
 * <li>{@code P_CONTENT_PATHS}: a set of paths to {@link Content}s;</li>
 * <li>{@code P_CLASS}: an optional CSS class name for the wrapping {@code &lt;div&gt;}.</li>
 * </ul>
 *
 * <p>For each {@code Content} the following properties are used:</p>
 *
 * <ul>
 * <li>{@code P_TITLE}: for rendering the title;</li>
 * <li>{@code P_FULL_TEXT}: for rendering the text.</li>
 * </ul>
 *
 * <p>Concrete implementations must provide the following method:</p>
 *
 * <ul>
 * <li>{@link #render(java.util.List)}</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @RequiredArgsConstructor
    public static class TextWithTitle
      {
        public final Optional<String> title;
        public final Optional<String> text;
        public final int level;
      }

    @Nonnull
    private final HtmlTextWithTitleView view;

    @Nonnull
    private final SiteNode siteNode;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      {
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        final int titleLevel = viewProperties.getProperty(P_LEVEL).orElse(2);
        view.setClassName(viewProperties.getProperty(P_CLASS).orElse("nw-" + view.getId()));
        render(viewProperties.getProperty(P_CONTENT_PATHS).orElse(emptyList())
                .stream()
                .flatMap(path -> siteNode.getSite().find(Content).withRelativePath(path).stream())
                .map(c -> new TextWithTitle(c.getProperty(P_TITLE), c.getProperty(P_FULL_TEXT), titleLevel))
                .collect(toList()));
      }

    /*******************************************************************************************************************
     *
     * Renders the collection of texts with their titles.
     *
     * @param   contents    the contents to render
     *
     ******************************************************************************************************************/
    protected abstract void render (@Nonnull List<TextWithTitle> contents);
  }
