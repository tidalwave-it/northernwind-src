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
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import it.tidalwave.util.As;
import it.tidalwave.northernwind.core.model.MimeTypeResolver;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.spi.ResourceFileFinderSupport;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(of = "delegate")
public class ResourceFileNetBeansPlatform implements ResourceFile
  {
    interface Exclusions
      {
        public String getName();
        public ResourcePath getPath();
        public ResourceFile getParent();
        public ResourceFile getFileObject (String fileName);
        public Collection<ResourceFile> getChildren();
        public Collection<ResourceFile> getChildren (boolean b);
        public String getMIMEType();
        public File toFile();
        public ResourceFile createFolder (String name);
        public void copyTo (ResourceFile targetFolder);
        public ResourceFileSystem getFileSystem();
        public Date lastModified();
      }

    @Inject
    private Provider<MimeTypeResolver> mimeTypeResolver;

    @Getter @Nonnull
    private final ResourceFileSystemNetBeansPlatform fileSystem;

    @Getter @Delegate(excludes = Exclusions.class) @Nonnull
    private final FileObject delegate;

    @Delegate
    private final As asSupport = As.forObject(this);

    public ResourceFileNetBeansPlatform (@Nonnull final ResourceFileSystemNetBeansPlatform fileSystem,
                                         @Nonnull final FileObject delegate)
      {
        this.fileSystem = fileSystem;
        this.delegate = delegate;
      }

    @Override @Nonnull
    public ResourcePath getPath()
      {
        return ResourcePath.of(delegate.getPath());
      }

    @Override @Nonnull
    public String getName()
      {
        return delegate.getNameExt();
      }

    @Override
    public ResourceFile getParent()
      {
        return fileSystem.createResourceFile(delegate.getParent());
      }

    @Override @Nonnull
    public ResourceFile createFolder (@Nonnull final String name)
            throws IOException
      {
        return fileSystem.createResourceFile(delegate.createFolder(name));
      }

    @Override @Nonnull
    public Finder findChildren()
      {
        return ResourceFileFinderSupport.withComputeResults(getClass().getSimpleName(), f ->
          {
            final var name = f.getName();
            final var recursive = f.isRecursive();

            final List<ResourceFile> result = new ArrayList<>();

            if (name != null)
              {
                final var child = delegate.getFileObject(name);

                if (child != null)
                  {
                    result.add(fileSystem.createResourceFile(child));
                  }
              }
            else
              {
                for (final FileObject child : Collections.list(delegate.getChildren(recursive)))
                  {
                    result.add(fileSystem.createResourceFile(child));
                  }
              }

            return result;
          });
      }

    @Override @Nonnull
    public String getMimeType()
      {
        return mimeTypeResolver.get().getMimeType(delegate.getNameExt());
      }

    @Override @Nonnull
    public ZonedDateTime getLatestModificationTime()
      {
        // See NW-154
        final var file = toFile();

        final var millis = (file != null) ? file.lastModified() : delegate.lastModified().getTime();
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of("GMT"));
      }

    @Nonnull
    @Override
    public File toFile()
      {
        return FileUtil.toFile(delegate);
      }

    @Override
    public void copyTo (@Nonnull final ResourceFile targetFolder)
            throws IOException
      {
        FileUtil.copyFile(delegate, ((ResourceFileNetBeansPlatform)targetFolder).delegate, delegate.getName());
      }

    // TODO: equals and hashcode
  }
