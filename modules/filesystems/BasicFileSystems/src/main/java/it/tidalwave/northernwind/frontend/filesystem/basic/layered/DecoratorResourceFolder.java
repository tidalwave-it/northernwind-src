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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.spi.DecoratedResourceFileSupport;
import it.tidalwave.northernwind.core.model.spi.ResourceFileFinderSupport;
import lombok.Delegate;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString(of="delegate")
class DecoratorResourceFolder extends DecoratedResourceFileSupport
  {
    @Nonnull
    private final ResourcePath path;

    @Nonnull
    private final List<? extends ResourceFileSystemProvider> delegates;

    @Delegate(types=ResourceFile.class, excludes=FolderDelegateExclusions.class) @Nonnull
    private final ResourceFile delegate;

    private SortedMap<String, ResourceFile> childrenMap;

    public DecoratorResourceFolder (final @Nonnull LayeredResourceFileSystem fileSystem,
                                    final @Nonnull List<? extends ResourceFileSystemProvider> delegates,
                                    final @Nonnull ResourcePath path,
                                    final @Nonnull ResourceFile delegate)
      {
        super(fileSystem, delegate);
        this.path = path;
        this.delegate = delegate;
        this.delegates = delegates;
      }

    @Override @Nonnull
    public Finder findChildren()
      {
        return new ResourceFileFinderSupport()
          {
            @Override @Nonnull
            protected List<? extends ResourceFile> computeResults()
              {
                if (name != null)
                  {
                    if (name.contains("/"))
                      {
                        throw new IllegalArgumentException("relativePath: " + name);
                      }

                    final ResourceFile child = getChildrenMap().get(name);

                    return (child != null) ? Collections.singletonList(child) : Collections.<ResourceFile>emptyList();
                  }
                //
                // FIXME: this reproduces the behaviour before the refactoring for NW-192 - but it's (and was) wrong
                // since it doesn't consider delegates. It should be rewritten by relying on getChildrenMap() and
                // making it eventually support recursion.
                //
                else if (recursive)
                  {
                    return delegate.findChildren().withRecursion(recursive).results();
                  }
                else
                  {
                    return new ArrayList<>(getChildrenMap().values());
                  }
              }
          };
      }

    @Nonnull
    private synchronized Map<String, ResourceFile> getChildrenMap()
      {
        if (childrenMap == null)
          {
            childrenMap = new TreeMap<>();

            for (final ListIterator<? extends ResourceFileSystemProvider> i = delegates.listIterator(delegates.size()); i.hasPrevious(); )
              {
                try
                  {
                    final ResourceFileSystem fileSystem = i.previous().getFileSystem();
                    final ResourceFile delegateDirectory = fileSystem.findFileByPath(path.asString());

                    if (delegateDirectory != null)
                      {
                        for (final ResourceFile fileObject : delegateDirectory.findChildren().results())
                          {
                            if (!childrenMap.containsKey(fileObject.getName()))
                              {
                                childrenMap.put(fileObject.getName(), getFileSystem().createDecoratorFile(fileObject));
                              }
                          }
                      }
                  }
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }

            log.trace(">>>> childrenMap: {}", childrenMap);
          }

        return childrenMap;
      }
  }

