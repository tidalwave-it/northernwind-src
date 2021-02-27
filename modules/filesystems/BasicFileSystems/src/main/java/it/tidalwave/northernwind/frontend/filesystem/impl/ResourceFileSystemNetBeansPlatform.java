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
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import it.tidalwave.util.As;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import lombok.experimental.Delegate;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class ResourceFileSystemNetBeansPlatform implements ResourceFileSystem
  {
    private final IdentityHashMap<FileObject, ResourceFile> delegateLightWeightMap = new IdentityHashMap<>();

    @Nonnull
    private final FileSystem fileSystem;

    @Delegate
    private final As asSupport = new AsSupport(this);

    @Override @Nonnull
    public ResourceFile getRoot()
      {
        return createResourceFile(fileSystem.getRoot());
      }

    @Override @Nonnull
    public ResourceFile findFileByPath (final @Nonnull String path)
      {
        return createResourceFile(fileSystem.findResource(path));
      }

    /* package */ synchronized ResourceFile createResourceFile (final @Nonnull FileObject fileObject)
      {
        if (fileObject == null)
          {
            return null;
          }

        ResourceFile decorator = delegateLightWeightMap.get(fileObject);

        if (decorator == null)
          {
            decorator = new ResourceFileNetBeansPlatform(this, fileObject);
            delegateLightWeightMap.put(fileObject, decorator);
          }

        return decorator;
      }

    // TODO: equals and hashcode
  }
