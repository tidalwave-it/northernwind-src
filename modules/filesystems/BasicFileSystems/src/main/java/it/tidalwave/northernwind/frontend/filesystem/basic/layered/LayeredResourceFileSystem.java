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
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.io.IOException;
import it.tidalwave.util.As;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.spi.DecoratedResourceFileSystem;
import it.tidalwave.northernwind.frontend.filesystem.basic.FileSystemProvidersProvider;
import lombok.experimental.Delegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class LayeredResourceFileSystem implements DecoratedResourceFileSystem
  {
    @Nonnull
    private final List<ResourceFileSystemProvider> delegates;

    private final FileSystemProvidersProvider fileSystemProvidersProvider;

    private final IdentityHashMap<ResourceFile, ResourceFile> delegateLightWeightMap = new IdentityHashMap<>();

    @Delegate
    private final As asSupport = As.forObject(this);

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceFile getRoot()
      {
        return findFileByPath("");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @CheckForNull
    public ResourceFile findFileByPath (@Nonnull final String name)
      {
        log.trace("findResource({})", name);
        ResourceFile result = null;

        // FIXME: move to init!
        if (fileSystemProvidersProvider != null)
          {
            delegates.clear();
            final var fileSystemProviders = fileSystemProvidersProvider.getFileSystemProviders();
            delegates.addAll(fileSystemProviders);
          }

        for (final ListIterator<? extends ResourceFileSystemProvider> i = delegates.listIterator(delegates.size());
             i.hasPrevious(); )
          {
            try
              {
                final var fileSystem = i.previous().getFileSystem();
                final var fileObject = fileSystem.findFileByPath(name);

                if (fileObject != null)
                  {
                    log.trace(">>>> fileSystem: {}, fileObject: {}", fileSystem, fileObject.getPath());
                    result = createDecoratorFile(fileObject);
                    break;
                  }
              }
            catch (IOException e)
              {
                log.warn("", e);
              }
          }

        log.trace(">>>> returning {}", result);

        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized ResourceFile createDecoratorFile (@Nonnull final ResourceFile delegateFile)
      {
        if (delegateFile == null)
          {
            return null;
          }

        var decorator = delegateLightWeightMap.get(delegateFile);

        if (decorator == null)
          {
            decorator = (delegateFile.isData() ? new DecoratorResourceFile(this, delegateFile)
                                               : new DecoratorResourceFolder(this,
                                                                             delegates,
                                                                             delegateFile.getPath(),
                                                                             delegateFile));
            delegateLightWeightMap.put(delegateFile, decorator);
          }

        return decorator;
      }
  }
