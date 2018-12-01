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
 * $Id: c6768f9715938836f8896136836afaa4f6986612 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.container;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.TemplateHelper;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: c6768f9715938836f8896136836afaa4f6986612 $
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

    private final TemplateHelper templateHelper = new TemplateHelper(this);

    @PostConstruct
    /* package */ void initialize()
      {
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        templateHelper.getTemplate4(viewProperties).ifPresent(view::setTemplate);
        view.setClassName(viewProperties.getProperty(PROPERTY_CLASS).orElse("nw-" + view.getId()));
      }
  }
