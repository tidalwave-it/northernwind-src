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
 * $Id: a1591e56997937c633fba05105d521612d15b528 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.htmlfragment;

import javax.annotation.PostConstruct;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Configurable;
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
 * A default implementation of {@link HtmlFragmentViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id: a1591e56997937c633fba05105d521612d15b528 $
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
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());

        final StringBuilder htmlBuilder = new StringBuilder();

        viewProperties.getProperty(PROPERTY_CONTENTS).orElse(emptyList()).stream().forEach(relativePath ->
          {
            try
              {
                final Content content = site.find(Content).withRelativePath(relativePath).result();
                final ResourceProperties contentProperties = content.getProperties();
                htmlBuilder.append(contentProperties.getProperty(PROPERTY_FULL_TEXT)
                                                    .orElse(contentProperties.getProperty(PROPERTY_TEMPLATE)
                                                                             .orElse("")));
                htmlBuilder.append("\n");
              }
            catch (NotFoundException e)
              {
                htmlBuilder.append(e.toString().replaceAll("\\[.*\\]", ""));
                log.error("NotFoundException", e.toString());
              }
          });

        view.setContent(htmlBuilder.toString());
        view.setClassName(viewProperties.getProperty(PROPERTY_CLASS).orElse("nw-" + view.getId()));
      }
  }
