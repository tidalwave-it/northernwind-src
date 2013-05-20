/*
 * #%L
 * %%
 * %%
 * #L%
 */
package it.tidalwave.northernwind.frontend.media.impl;

import javax.annotation.Nonnull;
import java.util.Map;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class XmlDcTitleInterpolator extends MetadataInterpolatorSupport
  {
    public XmlDcTitleInterpolator() 
      {
        super("$XMP.dc.title$");
      }
    
    @Override @Nonnull
    public String interpolate (final @Nonnull String string, final @Nonnull Context context)
      {
        final Map<String, String> xmpProperties = context.getMetadata().getXmp().getXmpProperties();
        
        return string.replace(id, formatted(xmpProperties.get("dc:title[1]")));
      }
  }
