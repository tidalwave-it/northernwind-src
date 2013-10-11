/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.joda.time.DateTime;
import it.tidalwave.util.As;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourcePath;
import lombok.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MockResourceFile implements ResourceFile
  {
    private final boolean directory;

    @Getter @CheckForNull
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
    public static ResourceFile folder (final @Nonnull ResourceFile parent, final @Nonnull String fileName)
      {
        final ResourcePath parentPath = parent.getPath();
        return new MockResourceFile(parent, parentPath.appendedWith(fileName), true);
      }

    @Nonnull
    public static ResourceFile folder (final @Nonnull String path)
      {
        return new MockResourceFile(null, new ResourcePath(path), true);
      }

    private MockResourceFile (final @Nonnull ResourceFile parent,
                              final @Nonnull ResourcePath path,
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

    @Override
    public String getMimeType()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public InputStream getInputStream() throws FileNotFoundException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public String asText(String encoding) throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public byte[] asBytes() throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public DateTime getLatestModificationTime()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public File toFile()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void delete() throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile createFolder(String name)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void copyTo(ResourceFile targetFolder)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public Finder findChildren()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }
  }
