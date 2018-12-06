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
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController;

/***********************************************************************************************************************
 *
 * Implementations of this interface are capable to load the list of items of the gallery from different formats.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface GalleryLoader
  {
    /*******************************************************************************************************************
     *
     * Loads the items in the gallery.
     *
     * @param  siteNode   the {@link SiteNode} of the gallery
     * @return            the list of gallery items
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<GalleryViewController.GalleryItem> loadGallery (@Nonnull SiteNode siteNode);
  }
