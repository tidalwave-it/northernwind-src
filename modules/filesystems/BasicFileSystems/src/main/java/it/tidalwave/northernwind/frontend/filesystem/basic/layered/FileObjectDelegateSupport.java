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
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
abstract class FileObjectDelegateSupport implements ResourceFile 
  {
    @Nonnull
    protected final LayeredFileSystemProvider fileSystemProvider;

    @Override @Nonnull
    public ResourceFileSystem getFileSystem()
      {
        return fileSystemProvider.getFileSystem();  
      }

    @Override
    public void delete()
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public boolean equals (final Object object)
      {
        if ((object == null) || (getClass() != object.getClass()))
          {
            return false;
          }

        final FileObjectDelegateSupport other = (FileObjectDelegateSupport)object;
        return (this.getFileSystem() == other.getFileSystem()) && this.getPath().equals(other.getPath());
      }

    @Override
    public int hashCode()
      {
        return getFileSystem().hashCode() ^ getPath().hashCode();
      }
  }
