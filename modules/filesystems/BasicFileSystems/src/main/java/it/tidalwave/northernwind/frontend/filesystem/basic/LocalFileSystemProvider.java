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
package it.tidalwave.northernwind.frontend.filesystem.basic;

import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.NwFileObject;
import it.tidalwave.northernwind.core.model.NwFileSystem;
import org.openide.filesystems.LocalFileSystem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * A provider for a local {@link NwFileSystem}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString(of={"rootPath"})
public class LocalFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    private String rootPath = "";
    
    @CheckForNull
    private LocalFileSystem fileSystem;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized NwFileSystem getFileSystem() 
      throws IOException
      {
        if (fileSystem == null)
          {
            try
              {
                fileSystem = new LocalFileSystem();
                fileSystem.setRootDirectory(new File(rootPath));

                final NwFileObject rootFolder = fileSystem.getRoot();

                if (rootFolder == null)
                  {
                    throw new FileNotFoundException(rootPath);  
                  } 
              }
            catch (PropertyVetoException e)
              {
                throw new FileNotFoundException(e.toString());
              }
          }
              
        return fileSystem;  
      }    
  }
