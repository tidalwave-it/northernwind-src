/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.frontend.model.SiteFinder;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString(callSuper=true, exclude="mapByRelativeUri")
/* package */ class DefaultSiteFinder<Type> extends FinderSupport<Type, DefaultSiteFinder<Type>> implements SiteFinder<Type>    
  {
    @Nonnull
    private final Map<String, Type> mapByRelativeUri;
    
    @CheckForNull
    private String relativeUri;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultSiteFinder (final @Nonnull String name, final @Nonnull Map<String, Type> mapByRelativeUri) 
      {
        super(name);
        this.mapByRelativeUri = mapByRelativeUri;
      }    
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteFinder<Type> withRelativeUri (final @Nonnull String relativeUri) 
      {
        final DefaultSiteFinder<Type> clone = (DefaultSiteFinder<Type>)clone();
        clone.relativeUri = relativeUri;
        return clone;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Type result() 
      throws NotFoundException 
      {
        try
          {
            return super.result();
          }
        catch (NotFoundException e)
          {
            throw new NotFoundException(relativeUri + ": " + mapByRelativeUri.keySet());
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected List<? extends Type> computeResults()
      {
        final List<Type> results = new ArrayList<Type>();

        if (relativeUri != null)
          {
            final Type result = mapByRelativeUri.get(relativeUri);
            
            if (result != null)
              {
                results.add(result);  
              }
          }
        else
          {
            results.addAll(mapByRelativeUri.values());  
          }
        
        return results;
      }
  }
