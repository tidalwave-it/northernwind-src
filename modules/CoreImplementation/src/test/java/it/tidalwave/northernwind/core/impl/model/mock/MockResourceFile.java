/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model.mock;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.time.ZonedDateTime;
import java.io.File;
import java.io.InputStream;
import it.tidalwave.util.As;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourcePath;
import lombok.experimental.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public final class MockResourceFile implements ResourceFile
  {
    private final boolean directory;

    @Nonnull @Getter
    private final ResourceFile parent;

    @Getter @Nonnull
    private final String name;

    @Getter @Nonnull
    private final ResourcePath path;

    @Delegate
    private final As asSupport = new AsSupport(this);

//    @Nonnull
//    public static ResourceFile file (final @Nonnull String path)
//      {
//        return new MockResourceFile(path, false);
//      }

    @Nonnull
    public static ResourceFile folder (@Nonnull final ResourceFile parent, @Nonnull final String fileName)
      {
        final ResourcePath parentPath = parent.getPath();
        return new MockResourceFile(parent, parentPath.appendedWith(fileName), true);
      }

    @Nonnull
    public static ResourceFile folder (@Nonnull final String path)
      {
        return new MockResourceFile(null, ResourcePath.of(path), true);
      }

    private MockResourceFile (@Nonnull final ResourceFile parent,
                              @Nonnull final ResourcePath path,
                              final boolean directory)
      {
        this.parent = parent;
        this.directory = directory;
        this.path = path;
        this.name = (path.getSegmentCount() == 0) ? "" : path.getTrailing();
      }

    @Override
    public boolean equals (final Object object)
      {
        if ((object == null) || (getClass() != object.getClass()))
          {
            return false;
          }

        final MockResourceFile other = (MockResourceFile) object;

        return (this.directory == other.directory) && Objects.equals(this.path, other.path);
      }

    @Override
    public int hashCode()
      {
        int hash = 3;
        hash = 89 * hash + (this.directory ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.path);
        return hash;
      }

    @Nonnull
    @Override
    public ResourceFileSystem getFileSystem()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public boolean isFolder()
      {
        return directory;
      }

    @Override
    public boolean isData()
      {
        return !directory;
      }

    @Nonnull
    @Override
    public String getMimeType()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public InputStream getInputStream()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public String asText (@Nonnull final String encoding)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public byte[] asBytes()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public ZonedDateTime getLatestModificationTime()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public File toFile()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void delete()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public ResourceFile createFolder (@Nonnull final String name)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void copyTo (@Nonnull final ResourceFile targetFolder)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    @Override
    public Finder findChildren()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }
  }
