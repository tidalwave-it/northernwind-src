/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.menu;

import javax.annotation.Nonnull;
import org.springframework.context.annotation.Scope;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static it.tidalwave.northernwind.core.model.Content.P_TITLE;
import static it.tidalwave.northernwind.core.model.SiteNode.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MenuViewController}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Scope("session") @Slf4j
public class DefaultMenuViewController implements MenuViewController
  {
    @Nonnull
    protected final MenuView view;

    @Nonnull
    protected final SiteNode siteNode;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (@Nonnull final RenderContext context)
     {
        final Site site = siteNode.getSite();
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        viewProperties.getProperty(P_TITLE).ifPresent(view::setTitle);
        viewProperties.getProperty(P_TEMPLATE_PATH).flatMap(p -> site.getTemplate(getClass(), p))
                                                   .ifPresent(view::setTemplate);

        viewProperties.getProperty(P_LINKS).orElse(emptyList())
                .stream()
                .flatMap(path -> site.find(_SiteNode_).withRelativePath(path).stream())
                .forEach(node -> view.addLink(node.getProperty(P_NAVIGATION_LABEL).orElse("no nav. label"), // FIXME
                                              site.createLink(node.getRelativeUri())));
      }
  }
