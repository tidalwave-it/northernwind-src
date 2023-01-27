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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryViewController.GalleryItem;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.MediaMetadataProvider;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A support implementation for {@link GalleryLoader}, provides a default factory method for creating gallery
 * {@link GalleryItem}s which delegates to a {@link MediaMetadataProvider}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class GalleryLoaderSupport implements GalleryLoader
  {
    private static final Key<String> P_MEDIA_METADATA_PROVIDER = Key.of("mediaMetadataProvider", String.class);

    @Nonnull
    private final BeanFactory beanFactory;

    @Nonnull
    private final MediaMetadataProvider mediaMetadataProvider;

    @Nonnull
    private final ResourceProperties properties;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected GalleryLoaderSupport (@Nonnull final BeanFactory beanFactory,
                                    @Nonnull final ResourceProperties properties)
      {
        this.beanFactory = beanFactory;
        this.properties = properties;
        mediaMetadataProvider = findMediaMetadataProvider();
      }

    /*******************************************************************************************************************
     *
     * Creates a gallery {@link GalleryItem} for the given media id.
     *
     * @param  mediaId   the media id
     * @return           the gallery {@code Item}
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public GalleryItem createItem (@Nonnull final Id mediaId)
      {
        return new GalleryItem(mediaId, mediaMetadataProvider.getMetadataString(mediaId, "$XMP.dc.title$", properties));
      }

    /*******************************************************************************************************************
     *
     * Finds the {@link MediaMetadataProvider}.
     *
     ******************************************************************************************************************/
    @Nonnull
    private MediaMetadataProvider findMediaMetadataProvider()
      {
        final var metadataProviderName = properties.getProperty(P_MEDIA_METADATA_PROVIDER).orElse("");

        try
          {
            return beanFactory.getBean(metadataProviderName, MediaMetadataProvider.class);
          }
        catch (NoSuchBeanDefinitionException e)
          {
            log.warn("Cannot find bean: {}", metadataProviderName);
            return MediaMetadataProvider.VOID;
          }
      }
  }
