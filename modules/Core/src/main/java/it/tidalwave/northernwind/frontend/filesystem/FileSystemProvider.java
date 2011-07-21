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

import javax.annotation.Nonnull;
import java.io.IOException;
import org.openide.filesystems.FileSystem;

/***********************************************************************************************************************
 *
 * A provider for the {@link FileSystem}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface FileSystemProvider
  {
    /*******************************************************************************************************************
     *
     * Returns the {@link FileSystem}.
     * 
     * @return  the {@code FileSystem}
     *
     ******************************************************************************************************************/
    @Nonnull
    public FileSystem getFileSystem()
      throws IOException;
  }
