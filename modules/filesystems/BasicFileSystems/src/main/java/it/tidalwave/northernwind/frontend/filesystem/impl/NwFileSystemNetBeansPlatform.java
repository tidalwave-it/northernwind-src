/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import org.openide.filesystems.FileObject;
import it.tidalwave.northernwind.core.model.NwFileObject;
import it.tidalwave.northernwind.core.model.NwFileSystem;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class NwFileSystemNetBeansPlatform implements NwFileSystem
  {
    private final IdentityHashMap<FileObject, NwFileObject> delegateLightWeightMap = new IdentityHashMap<>();
    
    @Nonnull
    private final org.openide.filesystems.FileSystem fileSystem;

    @Override @Nonnull
    public NwFileObject getRoot() 
      {
        return createNwFileObject(fileSystem.getRoot());
      }
    
    @Override @Nonnull
    public NwFileObject findResource (final @Nonnull String name) 
      {
        return createNwFileObject(fileSystem.findResource(name));
      }
    
    /* package */ synchronized NwFileObject createNwFileObject (final @Nonnull FileObject fileObject)
      {
        if (fileObject == null)
          {
            return null;  
          }
        
        NwFileObject decorator = delegateLightWeightMap.get(fileObject);
        
        if (decorator == null)
          {
            decorator = new NwFileObjectNetBeansPlatform(this, fileObject);
            delegateLightWeightMap.put(fileObject, decorator);
          }
        
        return decorator;
      }
    
    // TODO: equals and hashcode
  }
