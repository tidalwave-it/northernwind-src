/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.io.IOException;
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
import static org.imajine.image.op.ReadOp.Type.METADATA;
import static it.tidalwave.northernwind.core.model.Media.Media;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MetadataLoader}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
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
    public ResourceFile findMediaResourceFile (final @Nonnull ResourceProperties siteNodeProperties, 
                                               final @Nonnull Id mediaId) 
      throws NotFoundException, IOException 
      {
        final ResourceProperties properties = siteNodeProperties.getGroup(PROPERTY_GROUP_ID);
        return findMedia(mediaId, properties).getFile();
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     * 
     ******************************************************************************************************************/
    @Override @Nonnull
    public Metadata loadMetadata (final @Nonnull ResourceFile file) 
      throws IOException 
      {
        log.info("loadMetadata({})", file.getPath());
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
     * @throws NotFoundException  if no {@code Media} is found
     *
     ******************************************************************************************************************/
    @Nonnull
    private Media findMedia (final @Nonnull Id mediaId, final @Nonnull ResourceProperties properties)
      throws NotFoundException, IOException
      {
        final Site site = siteProvider.get().getSite();

        for (final Iterator<String> i = properties.getProperty(PROPERTY_MEDIA_PATHS).iterator(); i.hasNext(); )
          {
            final String mediaPath = i.next();
            final String resourceRelativePath = String.format(mediaPath, mediaId.stringValue());

            try
              {
                return site.find(Media).withRelativePath(resourceRelativePath).result();
              }
            catch (NotFoundException e)
              {
                if (!i.hasNext())
                  {
                    throw e;
                  }
              }
          }

        throw new RuntimeException("Shouldn't get here");
      }
  }
