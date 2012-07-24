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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.io.IOException;
import org.openide.util.actions.SystemAction;
import it.tidalwave.northernwind.core.model.NwFileObject;
import it.tidalwave.northernwind.core.model.NwFileSystem;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.FileSystemProvidersProvider;
import javax.annotation.CheckForNull;
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
public class LayeredFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    List<? extends FileSystemProvider> delegates = new ArrayList<FileSystemProvider>();
    
    @Getter @Setter
    private FileSystemProvidersProvider fileSystemProvidersProvider;
     
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private final NwFileSystem fileSystem = new NwFileSystem() 
      {
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String getDisplayName() 
          {
            return "LayeredFileSystem";
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override
        public boolean isReadOnly()
          {
            return true;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public NwFileObject getRoot() 
          {
            return findResource("");
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @CheckForNull
        public NwFileObject findResource (final @Nonnull String name) 
          {
            log.trace("findResource({})", name);
            NwFileObject result = null;
            
              // FIXME: move to init!
            if (fileSystemProvidersProvider != null)
              {
                delegates.clear();
                final List fileSystemProviders = fileSystemProvidersProvider.getFileSystemProviders();
                delegates.addAll(fileSystemProviders);
              }
            
            for (final ListIterator<? extends FileSystemProvider> i = delegates.listIterator(delegates.size()); i.hasPrevious(); )
              {
                try 
                  {
                    final NwFileSystem fileSystem = i.previous().getFileSystem();
                    final NwFileObject fileObject = fileSystem.findResource(name);

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
        
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public SystemAction[] getActions()
          {
            try 
              {
                return delegates.get(delegates.size() - 1).getFileSystem().getActions();
              } 
            catch (IOException e)
              {
                throw new RuntimeException(e);
              }
          }
      };
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public NwFileSystem getFileSystem()  
      {
        return fileSystem;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    public NwFileObject createDecoratorFileObject (final @Nonnull NwFileObject delegate)
      {
        return (delegate == null) ? null
                                  : (delegate.isData() ? new DecoratorFileObject(this, delegate) 
                                                       : new DecoratorFolderObject(this, delegate.getPath(), delegate));  
      }
  }
