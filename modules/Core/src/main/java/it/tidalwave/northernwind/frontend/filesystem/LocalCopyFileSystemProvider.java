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
package it.tidalwave.northernwind.frontend.filesystem;

import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for the {@link FileSystem} that clones a source provider into a local FileSystem for performance 
 * purposes...
 * 
 * FIXME: doesn't work, triggering a strange exception
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LocalCopyFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    private FileSystemProvider sourceProvider;
    
    @Getter @Setter @Nonnull
    private String rootPath = "";
    
    private LocalFileSystemProvider targetProvider = new LocalFileSystemProvider();
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized FileSystem getFileSystem() 
      throws IOException
      {
        return targetProvider.getFileSystem();      
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void initialize()
      throws IOException
      {
        new File(rootPath).mkdirs(); // TODO: use FileSystem API
        targetProvider.setRootPath(rootPath);
        final FileObject targetRoot = targetProvider.getFileSystem().getRoot();
        emptyFolder(targetRoot);
        copyFolder(sourceProvider.getFileSystem().getRoot(), targetRoot);           
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void emptyFolder (final @Nonnull FileObject folder) 
      throws IOException
      {
        log.info("emptyFolder({}, {}", folder);
        
        for (final FileObject child : folder.getChildren())
          {
            child.delete();
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void copyFolder (final @Nonnull FileObject sourceFolder, final @Nonnull FileObject targetFolder)
      throws IOException
      {
        log.info("copyFolder({}, {}", sourceFolder, targetFolder);
        
        for (final FileObject sourceChild : sourceFolder.getChildren())
          {
            if (!sourceChild.isFolder())
              { 
                log.info(">>>> copying {} into {} ...", sourceChild, targetFolder);
                FileUtil.copyFile(sourceChild, targetFolder, sourceChild.getNameExt());
              }
          }
        
        for (final FileObject sourceChild : sourceFolder.getChildren())
          {
            if (sourceChild.isFolder())
              { 
                copyFolder(sourceChild, targetFolder.createFolder(sourceChild.getNameExt()));
              }
          }
      }
  }
