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
 * $Id: f2b2bdea8326db3219e8cd6cfef167bdb1cd35a8 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.TemplateHelper;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: f2b2bdea8326db3219e8cd6cfef167bdb1cd35a8 $
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor
public abstract class GalleryAdapterSupport implements GalleryAdapter
  {
    @Inject
    protected ModelFactory modelFactory;

    @Nonnull @Getter
    private final Site site;

    private final TemplateHelper templateHelper = new TemplateHelper(this, this::getSite);

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getExtraViewProperties (final @Nonnull Id viewId)
      {
        return modelFactory.createProperties().withId(viewId).build();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getInlinedScript()
      {
        return "";
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    protected final String loadTemplate (final @Nonnull GalleryAdapterContext context,
                                         final @Nonnull Key<String> templateName,
                                         final @Nonnull String fallbackTemplate)
      {
        final GalleryView view = context.getView();
        final SiteNode siteNode = context.getSiteNode();
        final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
        return viewProperties.getProperty(templateName).flatMap(templateHelper::getTemplate)
                             .orElseGet(() -> templateHelper.getEmbeddedTemplate(fallbackTemplate));
      }
  }
