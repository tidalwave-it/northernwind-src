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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.Item;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A support implementation for {@link GalleryLoader}, provides a default factory method for creating gallery
 * {@link Item}s which delegates to a {@link MediaMetadataProvider}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j
public abstract class GalleryLoaderSupport implements GalleryLoader
  {
    private static final Key<String> PROPERTY_MEDIA_METADATA_PROVIDER = new Key<>("mediaMetadataProvider");

    @Inject
    private ApplicationContext context;

    @Nonnull
    private final MediaMetadataProvider mediaMetadataProvider;

    @Nonnull
    private final ResourceProperties properties;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected GalleryLoaderSupport (final @Nonnull ResourceProperties properties)
      {
        this.properties = properties;
        mediaMetadataProvider = findMediaMetadataProvider();
      }

    /*******************************************************************************************************************
     *
     * Creates a gallery {@link Item} for the given media id.
     *
     * @param  mediaId   the media id
     * @return           the gallery {@code Item}
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Item createItem (final @Nonnull Id mediaId)
      {
        return new Item(mediaId, mediaMetadataProvider.getMetadataString(mediaId, "$XMP.dc.title$", properties));
      }

    /*******************************************************************************************************************
     *
     * Finds the {@link MediaMetadataProvider}.
     *
     ******************************************************************************************************************/
    @Nonnull
    private MediaMetadataProvider findMediaMetadataProvider()
      {
        String metadataProviderName = "";

        try
          {
            metadataProviderName = properties.getProperty2(PROPERTY_MEDIA_METADATA_PROVIDER, "");
            return context.getBean(metadataProviderName, MediaMetadataProvider.class);
          }
        catch (IOException e)
          {
            log.warn("Cannot find bean: {}", metadataProviderName);
            return MediaMetadataProvider.VOID;
          }
        catch (NoSuchBeanDefinitionException e)
          {
            log.warn("Cannot find bean: {}", metadataProviderName);
            return MediaMetadataProvider.VOID;
          }
      }
  }
