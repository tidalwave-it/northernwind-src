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
import java.io.IOException;
import it.tidalwave.northernwind.core.model.NwFileObject;
import org.openide.filesystems.FileUtil;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.FileSystemProvidersProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.ZipFileSystemProvider;
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
public class DelegateZipFileSystemsDiscoverer implements FileSystemProvidersProvider
  {
    @Getter @Setter
    private String fileSystemsFolder = "filesystems";
    
    @Getter @Setter
    private FileSystemProvider localFileSystemProvider;
    
    @Override @Nonnull
    public List<? extends FileSystemProvider> getFileSystemProviders() 
      {
        final List<FileSystemProvider> fileSystemProviders = new ArrayList<FileSystemProvider>();
        
        try
          { 
            final NwFileObject zipFolder = localFileSystemProvider.getFileSystem().findResource(fileSystemsFolder);

            if (zipFolder != null)
              {
                for (final NwFileObject fo : zipFolder.getChildren())
                  {
                    if (fo.hasExt("zip") || fo.hasExt("jar"))
                      {
                        final ZipFileSystemProvider zipFileSystemProvider = new ZipFileSystemProvider();
                        zipFileSystemProvider.setZipFilePath(FileUtil.toFile(fo).getAbsolutePath());
                        fileSystemProviders.add(zipFileSystemProvider);
                      }
                  }
              }
          }
        catch (IOException e)
          { 
            log.error("", e);
          }
        
        fileSystemProviders.add(localFileSystemProvider);
        log.info("delegate filesystems: {}", fileSystemProviders);
        
        return fileSystemProviders;
      }
  }
