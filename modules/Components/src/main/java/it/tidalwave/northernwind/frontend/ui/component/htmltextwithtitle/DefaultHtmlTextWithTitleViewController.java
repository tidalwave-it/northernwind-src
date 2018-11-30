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
 * $Id: 9f70fc03a70bc0808c2acaa559ef2a1018bff687 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.stringtemplate.v4.ST;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlTextWithTitleViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id: 9f70fc03a70bc0808c2acaa559ef2a1018bff687 $
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @Nonnull
    private final HtmlTextWithTitleView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        try
          {
            int titleLevel = 2; // TODO: read override from properties
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
            final StringBuilder htmlBuilder = new StringBuilder();
            final String template = getTemplate(viewProperties);

            log.debug(">>>> template: {}", template);

            for (final String relativePath : viewProperties.getProperty(PROPERTY_CONTENTS).orElse(emptyList()))
              {
                final StringBuilder htmlFragmentBuilder = new StringBuilder();
                final Content content = site.find(Content).withRelativePath(relativePath).result();
                final ResourceProperties contentProperties = content.getProperties();
                appendTitle(contentProperties, htmlFragmentBuilder, "h" + titleLevel++);
                appendText(contentProperties, htmlFragmentBuilder);
                final ST t = new ST(template, '$', '$').add("content", htmlFragmentBuilder.toString());
                htmlBuilder.append(t.render());
              }

            view.setText(htmlBuilder.toString());
            view.setClassName(viewProperties.getProperty(PROPERTY_CLASS).orElse("nw-" + view.getId()));
          }
        catch (NotFoundException e)
          {
            view.setText(e.toString());
            log.error("", e.toString());
          }
        catch (IOException e)
          {
            view.setText(e.toString());
            log.error("", e);
          }
      }

    /*******************************************************************************************************************
     *
     * Appends the title.
     *
     ******************************************************************************************************************/
    private void appendTitle (final @Nonnull ResourceProperties contentProperties,
                              final @Nonnull StringBuilder htmlBuilder,
                              final @Nonnull String titleMarkup)
      {
        contentProperties.getProperty(PROPERTY_TITLE).ifPresent(title -> htmlBuilder.append(String.format("<%s>%s</%s>%n", titleMarkup, title, titleMarkup)));
      }

    /*******************************************************************************************************************
     *
     * Appends the text.
     *
     ******************************************************************************************************************/
    private void appendText (final @Nonnull ResourceProperties contentProperties,
                             final @Nonnull StringBuilder htmlBuilder)
      {
        contentProperties.getProperty(PROPERTY_FULL_TEXT).ifPresent(text -> htmlBuilder.append(text).append("\n"));
      }

    /*******************************************************************************************************************
     *
     * Returns the template.
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getTemplate (final @Nonnull ResourceProperties viewProperties)
      throws IOException
      {
        try
          {
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_WRAPPER_TEMPLATE_RESOURCE).orElseThrow(NotFoundException::new); // FIXME
            final Content content = site.find(Content).withRelativePath(templateRelativePath).result();
            final ResourceProperties templateProperties = content.getProperties();
            return templateProperties.getProperty(PROPERTY_TEMPLATE).orElse("$content$");
          }
        catch (NotFoundException e)
          {
            return "$content$";
          }
      }
  }
