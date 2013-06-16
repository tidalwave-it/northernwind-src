/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.htmlfragment;

import javax.annotation.PostConstruct;
import javax.annotation.Nonnull;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
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
 * A default implementation of {@link HtmlFragmentViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultHtmlFragmentViewController implements HtmlFragmentViewController
  {
    @Nonnull
    private final HtmlFragmentView view;

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
        final StringBuilder htmlBuilder = new StringBuilder();

        try
          {
            for (final String relativePath : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_CONTENTS))
              {
                final Content content = site.find(Content).withRelativePath(relativePath).result();

                try
                  {
                    htmlBuilder.append(content.getProperties().getProperty(PROPERTY_FULL_TEXT)).append("\n");
                  }
                catch (NotFoundException e)
                  {
                    htmlBuilder.append(content.getProperties().getProperty(PROPERTY_TEMPLATE)).append("\n");
                  }
              }
          }
        catch (NotFoundException e)
          {
            htmlBuilder.append(e.toString().replaceAll("\\[.*\\]", ""));
            log.error("", e.toString());
          }
        catch (IOException e)
          {
            htmlBuilder.append(e.toString());
            log.error("", e);
          }

        view.setContent(htmlBuilder.toString());

        try
          {
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
            view.setClassName(viewProperties.getProperty(PROPERTY_CLASS, "nw-" + view.getId()));
          }
        catch (IOException e)
          {
            // ok
          }
      }
  }
