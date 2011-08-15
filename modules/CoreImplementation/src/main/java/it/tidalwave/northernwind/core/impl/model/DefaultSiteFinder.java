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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.SiteFinder;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString(callSuper=true, exclude="mapByRelativePath")
/* package */ class DefaultSiteFinder<Type> extends FinderSupport<Type, DefaultSiteFinder<Type>> implements SiteFinder<Type>    
  {
    @Nonnull
    private final Map<String, Type> mapByRelativePath;
    
    @Nonnull
    private final RegexTreeMap<Type> mapByRelativeUri;
    
    @CheckForNull
    private String relativePath;

    @CheckForNull
    private String relativeUri;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultSiteFinder (final @Nonnull String name, 
                              final @Nonnull Map<String, Type> mapByRelativePath, 
                              final @Nonnull RegexTreeMap<Type> mapByRelativeUri) 
      {
        super(name);
        this.mapByRelativePath = mapByRelativePath;
        this.mapByRelativeUri = mapByRelativeUri;
      }    
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteFinder<Type> withRelativePath (final @Nonnull String relativePath) 
      {
        final DefaultSiteFinder<Type> clone = (DefaultSiteFinder<Type>)clone();
        clone.relativePath = relativePath;
        return clone;
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
            throw new NotFoundException(relativePath + ": " + mapByRelativePath.keySet());
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

        if (relativePath != null)
          {
            if (mapByRelativePath == null)
              {
                throw new IllegalArgumentException("Illegal type");  
              }
        
            final Type result = mapByRelativePath.get(relativePath);
            
            if (result != null)
              {
                results.add(result);  
              }
          }
        
        else if (relativeUri != null)
          {
            if (mapByRelativeUri == null)
              {
                throw new IllegalArgumentException("Illegal type");  
              }
        
            final Type result = mapByRelativeUri.get(relativeUri);
            
            if (result != null)
              {
                results.add(result);  
              }
          }
        else
          {
            results.addAll(mapByRelativePath.values());  
          }
        
        return results;
      }
  }
