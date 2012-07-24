/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.FileSystemProvidersProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LayeredFileSystemProvider implements ResourceFileSystemProvider
  {
    @Getter @Setter @Nonnull
    List<? extends ResourceFileSystemProvider> delegates = new ArrayList<ResourceFileSystemProvider>();
    
    @Getter @Setter
    private FileSystemProvidersProvider fileSystemProvidersProvider;
     
    private final IdentityHashMap<ResourceFile, ResourceFile> delegateLightWeightMap = new IdentityHashMap<>();
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private final ResourceFileSystem fileSystem = new ResourceFileSystem() 
      {
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public ResourceFile getRoot() 
          {
            return findFileByPath("");
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @CheckForNull
        public ResourceFile findFileByPath (final @Nonnull String name) 
          {
            log.trace("findResource({})", name);
            ResourceFile result = null;
            
              // FIXME: move to init!
            if (fileSystemProvidersProvider != null)
              {
                delegates.clear();
                final List fileSystemProviders = fileSystemProvidersProvider.getFileSystemProviders();
                delegates.addAll(fileSystemProviders);
              }
            
            for (final ListIterator<? extends ResourceFileSystemProvider> i = delegates.listIterator(delegates.size()); i.hasPrevious(); )
              {
                try 
                  {
                    final ResourceFileSystem fileSystem = i.previous().getFileSystem();
                    final ResourceFile fileObject = fileSystem.findFileByPath(name);

                    if (fileObject != null)
                      {
                        log.trace(">>>> fileSystem: {}, fileObject: {}", fileSystem, fileObject.getPath());
                        result = createDecoratorFileObject(fileObject);
                        break;
                      }
                  } 
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }

            log.trace(">>>> returning {}", result);
            
            return result;
          }
      };
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceFileSystem getFileSystem()  
      {
        return fileSystem;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFile createDecoratorFileObject (final @Nonnull ResourceFile delegate)
      {
        if (delegate == null) 
          {
            return null;  
          }
        
        ResourceFile decorator = delegateLightWeightMap.get(delegate);
        
        if (decorator == null)
          {
            decorator = (delegate.isData() ? new DecoratorFileObject(this, delegate) 
                                           : new DecoratorFolderObject(this, delegate.getPath(), delegate));  
            delegateLightWeightMap.put(delegate, decorator);
          }
                                  
        return decorator;
      }
  }
