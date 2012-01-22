/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.basic;

import it.tidalwave.northernwind.core.filesystem.FileSystemChangedEvent;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.File;
import org.joda.time.DateTime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.messagebus.MessageBus.Listener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for the {@link FileSystem} that clones a source provider into a local FileSystem for performance 
 * purposes...
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
    
    @Inject @Named("applicationMessageBus") @Nonnull
    private MessageBus messageBus;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private final Listener<FileSystemChangedEvent> sourceProviderChangeListener = new Listener<FileSystemChangedEvent>() 
      {
        @Override
        public void notify (final @Nonnull FileSystemChangedEvent event) 
          {
            if (event.getFileSystemProvider() == sourceProvider)
              {
                try 
                  {
                    log.info("Detected file change, regenerating local file system...");
                    generateLocalFileSystem();
                    messageBus.publish(new FileSystemChangedEvent(LocalCopyFileSystemProvider.this, new DateTime()));
                  }
                catch (IOException e) 
                  {
                    log.error("While resetting site: ", e);
                  }
              }
          }
      };
    
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
    /* package */ void initialize()
      throws IOException
      {
        log.info("initialize()");
        generateLocalFileSystem();
        messageBus.subscribe(FileSystemChangedEvent.class, sourceProviderChangeListener);            
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void generateLocalFileSystem()
      throws IOException
      {
        log.info("generateLocalFileSystem()");
        new File(rootPath).mkdirs(); // TODO: use FileSystem API
        // FIXME: shouldn't be needed, but otherwise after a second call to this method won't find files
        targetProvider = new LocalFileSystemProvider(); 
        targetProvider.setRootPath(rootPath);
        final FileObject targetRoot = targetProvider.getFileSystem().getRoot();
        final String path = FileUtil.toFile(targetRoot).getAbsolutePath();
        log.info(">>>> scratching {} ...", path);
        emptyFolder(targetRoot);
        log.info(">>>> copying files to {} ...", path);
        copyFolder(sourceProvider.getFileSystem().getRoot(), targetRoot);           
//                    targetProvider.getFileSystem().refresh(true);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void emptyFolder (final @Nonnull FileObject folder) 
      throws IOException
      {
        log.trace("emptyFolder({}, {}", folder);
        
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
        log.trace("copyFolder({}, {}", sourceFolder, targetFolder);
        
        for (final FileObject sourceChild : sourceFolder.getChildren())
          {
            if (!sourceChild.isFolder())
              { 
                log.trace(">>>> copying {} into {} ...", sourceChild, targetFolder);
                FileUtil.copyFile(sourceChild, targetFolder, sourceChild.getName());
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
