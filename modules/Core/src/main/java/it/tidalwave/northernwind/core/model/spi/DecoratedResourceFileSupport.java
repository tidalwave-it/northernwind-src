/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
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

    @Nonnull
    @Override
    public ResourceFile createFolder (@Nonnull final String name)
      throws IOException
      {
//        log.trace("createFolder({})", name);
        return fileSystem.createDecoratorFile(delegate.createFolder(name));
      }

    @Override @Nonnull
    public Finder findChildren()
      {
        return ResourceFileFinderSupport.withComputeResults(f ->
              delegate.findChildren().results().stream().map(fileSystem::createDecoratorFile).collect(toList()));
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
