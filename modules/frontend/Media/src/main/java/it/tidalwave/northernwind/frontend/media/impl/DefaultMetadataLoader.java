/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.Resource;
import org.imajine.image.EditableImage;
import org.imajine.image.op.ReadOp;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static org.imajine.image.op.ReadOp.Type.METADATA;
import static it.tidalwave.northernwind.core.model.Media._Media_;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MetadataLoader}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMetadataLoader implements MetadataLoader
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceFile findMediaResourceFile (@Nonnull final ResourceProperties siteNodeProperties,
                                               @Nonnull final Id mediaId)
      throws NotFoundException
      {
        final ResourceProperties properties = siteNodeProperties.getGroup(P_GROUP_ID);
        return findMedia(mediaId, properties).map(Resource::getFile).orElseThrow(NotFoundException::new); // FIXME
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Metadata loadMetadata (@Nonnull final ResourceFile file)
      throws IOException
      {
        log.debug("loadMetadata({})", file.getPath());
        final EditableImage image = EditableImage.create(new ReadOp(file.toFile(), METADATA));
        return new DefaultMetadata(file.getName(), image);
      }

    /*******************************************************************************************************************
     *
     * Finds a {@link Media} item for the given id.
     *
     * @param  mediaId            the media id
     * @param  properties         the configuration properties
     * @return                    the {@code Media}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<Media> findMedia (@Nonnull final Id mediaId, @Nonnull final ResourceProperties properties)
      {
        final Site site = siteProvider.get().getSite();

        return properties.getProperty(P_MEDIA_PATHS).orElse(emptyList())
                .stream()
                .map(pathTemplate -> String.format(pathTemplate, mediaId.stringValue()))
                .flatMap(path -> site.find(_Media_).withRelativePath(path).stream())
                .findFirst();
      }
  }
