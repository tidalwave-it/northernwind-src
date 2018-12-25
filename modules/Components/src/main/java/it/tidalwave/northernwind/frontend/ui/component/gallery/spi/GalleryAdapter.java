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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface GalleryAdapter
  {
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getExtraViewProperties (@Nonnull Id viewId);

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getInlinedScript();

    /*******************************************************************************************************************
     *
     * Renders a catalog of media items to be consumed by the gallery client software.
     *
     * @param  items        the list of gallery items
     *
     ******************************************************************************************************************/
    public void renderCatalog (@Nonnull List<GalleryItem> items)
      throws HttpStatusException;

    /*******************************************************************************************************************
     *
     * Renders the gallery page.
     *
     * @param  items        the list of gallery items
     *
     ******************************************************************************************************************/
    public void renderGallery (@Nonnull List<GalleryItem> items);

    /*******************************************************************************************************************
     *
     * Renders a single page of a given gallery item.
     *
     * @param  item         the gallery item
     * @param  items        the list of gallery items
     *
     ******************************************************************************************************************/
    public void renderItem (@Nonnull GalleryItem item, @Nonnull List<GalleryItem> items);

    /*******************************************************************************************************************
     *
     * Renders the lightbox.
     *
     * @param  items        the list of gallery items
     *
     ******************************************************************************************************************/
    public void renderLightbox (@Nonnull List<GalleryItem> items);
  }
