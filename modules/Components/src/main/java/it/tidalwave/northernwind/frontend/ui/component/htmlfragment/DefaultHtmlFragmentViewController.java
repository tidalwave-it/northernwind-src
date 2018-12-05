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
package it.tidalwave.northernwind.frontend.ui.component.htmlfragment;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlFragmentViewController}.
 *
 * @author  Fabrizio Giudici
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
    @Override
    public void initialize()
      {
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        view.setContent(viewProperties.getProperty(PROPERTY_CONTENTS).orElse(emptyList())
                                      .stream()
                                      .flatMap(path -> site.find(Content).withRelativePath(path).stream())
                                      .map(content -> content.getProperties())
                                       // FIXME: use a method searching through a sequence of keys, such as getDateTimeProperty()
                                      .map(properties -> properties.getProperty(PROPERTY_FULL_TEXT)
                                                                   .orElse(properties.getProperty(PROPERTY_TEMPLATE)
                                                                                     .orElse("")))
                                      .collect(joining("\n")));
        view.setClassName(viewProperties.getProperty(PROPERTY_CLASS).orElse("nw-" + view.getId()));
      }
  }
