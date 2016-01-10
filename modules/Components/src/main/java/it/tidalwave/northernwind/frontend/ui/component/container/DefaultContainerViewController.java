/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.container;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable
public class DefaultContainerViewController implements ContainerViewController
  {
    @Nonnull
    private final ContainerView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    @PostConstruct
    /* package */ void initialize()
      throws IOException
      {
        try
          {
            // First search the template in a path, which could be useful for retrieving from a library; if not
            // found, a property with the contents is searched.
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_PATH);
            final Content template = site.find(Content.class).withRelativePath(templateRelativePath).result();
            view.setTemplate(template.getProperties().getProperty(PROPERTY_TEMPLATE));
          }
        catch (NotFoundException e)
          {
            // ok, use the default template
          }

        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        view.setClassName(viewProperties.getProperty(PROPERTY_CLASS, "nw-" + view.getId()));
      }
  }
