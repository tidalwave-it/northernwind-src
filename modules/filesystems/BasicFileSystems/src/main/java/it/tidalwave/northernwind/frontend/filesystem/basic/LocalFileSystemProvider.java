/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.frontend.filesystem.impl.ResourceFileSystemNetBeansPlatform;
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
@ToString(of = { "rootPath" })
public class LocalFileSystemProvider implements ResourceFileSystemProvider
  {
    @Getter @Setter @Nonnull
    private String rootPath = "";

    @CheckForNull
    private ResourceFileSystem fileSystem;

    @CheckForNull
    private LocalFileSystem fileSystemDelegate;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized ResourceFileSystem getFileSystem()
      throws IOException
      {
        if (fileSystem == null)
          {
            try
              {
                fileSystemDelegate = new LocalFileSystem();
                fileSystemDelegate.setRootDirectory(new File(rootPath));

                final FileObject rootFolder = fileSystemDelegate.getRoot();

                if (rootFolder == null)
                  {
                    throw new FileNotFoundException(rootPath);
                  }

                fileSystem = new ResourceFileSystemNetBeansPlatform(fileSystemDelegate);
              }
            catch (PropertyVetoException e)
              {
                throw new FileNotFoundException(e.toString());
              }
          }

        return fileSystem;
      }
  }
