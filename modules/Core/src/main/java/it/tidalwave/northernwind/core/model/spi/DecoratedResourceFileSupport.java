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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public abstract class DecoratedResourceFileSupport implements ResourceFile
  {
    @Getter @Nonnull
    protected final DecoratedResourceFileSystem fileSystem;

    @Nonnull
    private final ResourceFile delegate;

    @Override
    public void delete()
      throws IOException
      {
        delegate.delete();
      }

    @Override
    public ResourceFile getParent()
      {
        return fileSystem.createDecoratorFile(delegate.getParent());
      }

    @Override
    public ResourceFile createFolder (final String name)
      throws IOException
      {
//        log.trace("createFolder({})", name);
        return fileSystem.createDecoratorFile(delegate.createFolder(name));
      }

    @Override
    public boolean equals (final Object object)
      {
        if ((object == null) || (getClass() != object.getClass()))
          {
            return false;
          }

        final ResourceFile other = (ResourceFile)object;
        return (this.getFileSystem() == other.getFileSystem()) && this.getPath().equals(other.getPath());
      }

    @Override
    public int hashCode()
      {
        return getFileSystem().hashCode() ^ getPath().hashCode();
      }
  }
