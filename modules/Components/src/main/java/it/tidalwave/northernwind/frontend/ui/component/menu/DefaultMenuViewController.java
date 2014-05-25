/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.menu;

import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.SiteNode.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MenuViewController}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Scope("session") @Slf4j
public class DefaultMenuViewController implements MenuViewController
  {
    @Nonnull
    protected final MenuView view;

    @Nonnull
    protected final SiteNode siteNode;

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
            view.setTitle(siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_TITLE));
          }
        catch (NotFoundException e)
          {
          }
        catch (IOException e)
          {
          }

        try
          {
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());

            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_PATH);
            final Content template = site.find(Content.class).withRelativePath(templateRelativePath).result();
            view.setTemplate(template.getProperties().getProperty(PROPERTY_TEMPLATE));
          }
        catch (NotFoundException e)
          {
            // ok, use the default template
          }
        catch (IOException e)
          {
          }

        try
          {
            for (final String relativePath : siteNode.getPropertyGroup(view.getId()).getProperty(PROPERTY_LINKS))
              {
                try
                  {
                    final SiteNode targetSiteNode = site.find(SiteNode).withRelativePath(relativePath).result();
                    final String navigationTitle = targetSiteNode.getProperties().getProperty(PROPERTY_NAVIGATION_LABEL,
                                                                                              "no nav. label");
                    view.addLink(navigationTitle, site.createLink(targetSiteNode.getRelativeUri()));
                  }
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
                catch (NotFoundException e)
                  {
                    log.warn("Ignoring link '{}' because of {}", relativePath, e.toString());
                  }
              }
          }
        catch (NotFoundException e)
          {
            log.error("", e);
          }
        catch (IOException e)
          {
            log.error("", e);
          }
      }
  }
