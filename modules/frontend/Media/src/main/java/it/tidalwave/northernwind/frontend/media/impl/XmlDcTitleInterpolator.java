/*
 * #%L
 * %%
 * %%
 * #L%
 */

package it.tidalwave.northernwind.frontend.media.impl;

import java.util.Map;
import javax.annotation.Nonnull;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class XmlDcTitleInterpolator extends MetadataInterpolatorSupport
  {
    @Override @Nonnull
    public String interpolate (final @Nonnull String string, final @Nonnull Context context)
      {
        final Map<String, String> xmpProperties = context.getMetadata().getXmp().getXmpProperties();
        
        return string.replace("$XMP.dc.title$", formatted(xmpProperties.get("dc:title[1]")));
      }
  }
