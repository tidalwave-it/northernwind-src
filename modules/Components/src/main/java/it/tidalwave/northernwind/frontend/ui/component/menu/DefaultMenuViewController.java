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
 * $Id: 1feaeccd397bbe355d5cdc6012a48118a2f38e28 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.menu;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.TemplateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static it.tidalwave.northernwind.core.model.SiteNode.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MenuViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id: 1feaeccd397bbe355d5cdc6012a48118a2f38e28 $
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Scope("session") @Slf4j
public class DefaultMenuViewController implements MenuViewController
  {
    @Nonnull
    protected final MenuView view;

    @Nonnull
    protected final SiteNode siteNode;

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
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        viewProperties.getProperty(PROPERTY_TITLE).ifPresent(view::setTitle);
        viewProperties.getProperty(PROPERTY_TEMPLATE_PATH).flatMap(templateHelper::getTemplate)
                                                          .ifPresent(view::setTemplate);

        viewProperties.getProperty(PROPERTY_LINKS).orElse(emptyList())
                .stream()
                .flatMap(path -> site.find(SiteNode).withRelativePath(path).stream())
                .forEach(node -> view.addLink(node.getProperty(PROPERTY_NAVIGATION_LABEL).orElse("no nav. label"), // FIXME
                                              site.createLink(node.getRelativeUri())));
      }
  }
