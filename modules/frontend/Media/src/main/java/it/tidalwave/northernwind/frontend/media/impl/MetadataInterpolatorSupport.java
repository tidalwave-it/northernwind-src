/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class MetadataInterpolatorSupport implements MetadataInterpolator
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected static String formatted (final @CheckForNull String string)
      {
        return (string != null) ? string : "";
      }
  }
