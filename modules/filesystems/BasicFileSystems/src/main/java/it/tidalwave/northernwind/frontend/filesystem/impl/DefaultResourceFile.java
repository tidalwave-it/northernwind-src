/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.util.As;
import it.tidalwave.util.spi.AsSupport;
import java.time.ZonedDateTime;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class DefaultResourceFile implements ResourceFile
  {
    @Getter @Nonnull
    private final ResourceFileSystem fileSystem;

    @Nonnull
    private final Path delegate;

    @Delegate
    private final As asSupport = new AsSupport(this);

    @Override @Nonnull
    public String getName()
      {
        return delegate.getFileName().toString();
      }

    @Override @Nonnull
    public ResourcePath getPath()
      {
        return new ResourcePath(delegate.toAbsolutePath().toString());
      }

    @Override
    public boolean isFolder()
      {
        return delegate.toFile().isDirectory();
      }

    @Override
    public boolean isData()
      {
        return delegate.toFile().isFile();
      }

    @Override
    public Finder findChildren()
      {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

    @Override
    public String getMimeType()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public InputStream getInputStream()
      throws FileNotFoundException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public String asText(String encoding)
      throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public byte[] asBytes()
      throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ZonedDateTime getLatestModificationTime()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile getParent()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override @Nonnull
    public File toFile()
      {
        return delegate.toFile();
      }

    @Override
    public void delete()
      throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile createFolder(String name)
      throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void copyTo(ResourceFile targetFolder)
      throws IOException
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }
  }
