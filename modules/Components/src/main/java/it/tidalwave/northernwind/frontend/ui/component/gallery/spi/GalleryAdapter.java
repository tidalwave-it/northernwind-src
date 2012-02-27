/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.Item;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface GalleryAdapter 
  {
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public void initialize (@Nonnull GalleryAdapterContext context);

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
     * Creates a catalog of media items to be consumed by the gallery software.
     * 
     * @param  view   the view component
     * @param  items  the gallery items
     *
     ******************************************************************************************************************/
    public void createItemCatalog (@Nonnull GalleryView view, @Nonnull List<Item> items)
      throws HttpStatusException;
    
    /*******************************************************************************************************************
     *
     * Creates a fallback page for a single gallery item for when JavaScript is not available.
     *
     * @param  view   the view component
     * @param  id     the image id
     * @param  item   the gallery item
     *
     ******************************************************************************************************************/
    public void createFallback (@Nonnull GalleryView view, @Nonnull Id id, @Nonnull Item item);
  }
