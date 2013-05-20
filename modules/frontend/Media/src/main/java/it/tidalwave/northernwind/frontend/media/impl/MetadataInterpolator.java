/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MetadataInterpolator 
  {
    @RequiredArgsConstructor @Getter @ToString
    public class Context
      {
        private final MetadataBag metadata;
        
        private final Map<String, String> lensMap;
      }
    
    @Nonnull
    public String getId();
    
    @Nonnull
    public String interpolate (@Nonnull String string, @Nonnull Context context);
  }
