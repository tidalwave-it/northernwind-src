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
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
class DecoratorFolderObject extends FileObjectDelegateSupport
  {
    @Nonnull
    private final String path;

    @Delegate(types=ResourceFile.class, excludes=FolderDelegateExclusions.class) @Nonnull
    private final ResourceFile delegate;

    private SortedMap<String, ResourceFile> childrenMap;

    public DecoratorFolderObject (final @Nonnull LayeredFileSystemProvider fileSystemProvider,
                                  final @Nonnull String path, 
                                  final @Nonnull ResourceFile delegate)
      {
        super(fileSystemProvider);
        this.path = path;
        this.delegate = delegate;
      }
    
    @Override @Nonnull
    public Collection<ResourceFile> getChildren() 
      {
        log.trace("getChildren() - {}", this);
        return getChildrenMap().values();
      }

    @Override
    public ResourceFile getChildByName (final String relativePath)
      {
        log.trace("getChildByName({})", relativePath);

        if (relativePath.contains("/"))
          {
            throw new IllegalArgumentException("relativePath: " + relativePath);  
          }

        return getChildrenMap().get(relativePath);
      }

//    @Override
//    public NwFileObject getFileObject (final String name, final String ext)
//      {
//        log.trace("getFileObject({}, {})", name, ext);
//        return getFileObject(name + "." + ext);
////            return createDecoratorFileObject(delegate.getFileObject(name, ext)); 
//      }

    @Override
    public ResourceFile getParent()
      {
        return fileSystemProvider.createDecoratorFileObject(delegate.getParent());
      }

//    @Override
//    public NwFileObject createData (final String name, final String ext)
//      throws IOException
//      {
//        log.trace("createData({}, {})", name, ext);
//        return fileSystemProvider.createDecoratorFileObject(delegate.createData(name, ext)); 
//      }

    @Override
    public ResourceFile createFolder (final String name)
      throws IOException
      {
        log.trace("createFolder({})", name);
        return fileSystemProvider.createDecoratorFileObject(delegate.createFolder(name));
      }

    @Override @Nonnull
    public String toString() 
      {
        return String.format("DecoratorFolderObject(%s)", delegate);
      }

    @Nonnull
    private synchronized Map<String, ResourceFile> getChildrenMap()
      {
        if (childrenMap == null)
          {
            childrenMap = new TreeMap<>();
            
            for (final ListIterator<? extends FileSystemProvider> i = fileSystemProvider.delegates.listIterator(fileSystemProvider.delegates.size()); i.hasPrevious(); )
              {
                try
                  {
                    final ResourceFileSystem fileSystem = i.previous().getFileSystem();
                    final ResourceFile delegateDirectory = fileSystem.findFileByPath(path);

                    if (delegateDirectory != null)
                      {
                        for (final ResourceFile fileObject : delegateDirectory.getChildren())
                          {
                            if (!childrenMap.containsKey(fileObject.getName()))
                              {
                                childrenMap.put(fileObject.getName(), fileSystemProvider.createDecoratorFileObject(fileObject));
                              }
                          }
                      }
                  } 
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }

            log.trace(">>>> childrenMap: {}", childrenMap);
          }
    
        return childrenMap;
      }
  }

