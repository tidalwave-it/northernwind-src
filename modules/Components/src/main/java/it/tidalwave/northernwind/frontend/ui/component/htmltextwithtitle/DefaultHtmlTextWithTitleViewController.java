/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
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
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlTextWithTitleViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
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

            for (final String relativePath : viewProperties.getProperty(PROPERTY_CONTENTS))
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
            view.setClassName(viewProperties.getProperty(PROPERTY_CLASS, "nw-" + view.getId()));
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
      throws IOException
      {
        try
          {
            final String title = contentProperties.getProperty(PROPERTY_TITLE);
            htmlBuilder.append(String.format("<%s>%s</%s>\n", titleMarkup, title, titleMarkup));
          }
        catch (NotFoundException e)
          {
            // ok, no title
          }
      }

    /*******************************************************************************************************************
     *
     * Appends the text.
     *
     ******************************************************************************************************************/
    private void appendText (final @Nonnull ResourceProperties contentProperties,
                             final @Nonnull StringBuilder htmlBuilder)
      throws IOException
      {
        try
          {
            htmlBuilder.append(contentProperties.getProperty(PROPERTY_FULL_TEXT)).append("\n");
          }
        catch (NotFoundException e)
          {
            log.warn("", e);
            htmlBuilder.append(e.toString());
          }
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
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_WRAPPER_TEMPLATE_RESOURCE);
            final Content content = site.find(Content).withRelativePath(templateRelativePath).result();
            final ResourceProperties templateProperties = content.getProperties();
            return templateProperties.getProperty(PROPERTY_TEMPLATE, "$content$");
          }
        catch (NotFoundException e)
          {
            return "$content$";
          }
      }
  }
