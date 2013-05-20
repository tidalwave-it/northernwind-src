/*
 * #%L
 * %%
 * %%
 * #L%
 */
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public abstract class MetadataInterpolatorSupport implements MetadataInterpolator
  {
    @Getter @Nonnull
    protected final String id;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected static String formatted (final @CheckForNull String string)
      {
        return (string != null) ? string : "";
      }
  }
