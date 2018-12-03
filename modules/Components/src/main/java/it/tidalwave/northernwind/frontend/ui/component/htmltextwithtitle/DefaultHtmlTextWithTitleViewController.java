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
import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Configurable;
import org.stringtemplate.v4.ST;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.TemplateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlTextWithTitleViewController}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @Nonnull
    private final HtmlTextWithTitleView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull @Getter
    private final Site site;

    private final TemplateHelper templateHelper = new TemplateHelper(this, this::getSite);

    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        final AtomicInteger titleLevel = new AtomicInteger(2); // TODO: read override from properties
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        final String template = viewProperties.getProperty(PROPERTY_WRAPPER_TEMPLATE_RESOURCE)
                                              .flatMap(templateHelper::getTemplate)
                                              .orElse("$content$");
        log.debug(">>>> template: {}", template);

        final String html = viewProperties.getProperty(PROPERTY_CONTENTS).orElse(emptyList())
                .stream()
                .flatMap(path -> site.find(Content).withRelativePath(path).stream())
                .map(content -> content.getProperties())
                .map(properties -> appendTitle(properties, "h" + titleLevel.getAndIncrement()) + appendText(properties))
                .collect(joining());

        final ST t = new ST(template, '$', '$').add("content", html);
        view.setText(t.render());
        view.setClassName(viewProperties.getProperty(PROPERTY_CLASS).orElse("nw-" + view.getId()));
      }

    /*******************************************************************************************************************
     *
     * Appends the title.
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String appendTitle (final @Nonnull ResourceProperties properties, final @Nonnull String titleMarkup)
      {
        return properties.getProperty(PROPERTY_TITLE).map(title -> String.format("<%s>%s</%s>%n", titleMarkup, title, titleMarkup)).orElse("");
      }

    /*******************************************************************************************************************
     *
     * Appends the text.
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String appendText (final @Nonnull ResourceProperties properties)
      {
        return properties.getProperty(PROPERTY_FULL_TEXT).map(text -> text + "\n").orElse("");
      }
  }
