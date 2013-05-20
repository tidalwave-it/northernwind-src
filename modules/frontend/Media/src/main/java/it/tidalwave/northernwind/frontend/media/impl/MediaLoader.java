/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import org.imajine.image.EditableImage;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MediaLoader 
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFile findMediaResourceFile (@Nonnull ResourceProperties siteNodeProperties, 
                                               @Nonnull Id mediaId) 
      throws NotFoundException, IOException;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public EditableImage loadImage (@Nonnull ResourceFile file) 
      throws IOException;
  }
