/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import org.imajine.image.EditableImage;
import org.imajine.image.op.ReadOp;
import static org.imajine.image.op.ReadOp.Type.METADATA;
import static it.tidalwave.northernwind.core.model.Media.Media;
import static it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultMediaLoader implements MediaLoader
  {
    @Inject @Nonnull
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
    public EditableImage loadImage (final @Nonnull ResourceFile file) 
      throws IOException 
      {
        return EditableImage.create(new ReadOp(file.toFile(), METADATA));
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
