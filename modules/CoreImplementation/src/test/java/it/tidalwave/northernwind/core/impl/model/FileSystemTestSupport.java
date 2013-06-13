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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import javax.annotation.CheckForNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/***********************************************************************************************************************
 *
 * TODO: refactor with MockResourceFile,
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @ToString
public abstract class FileSystemTestSupport
  {
    @Nonnull
    private final String name;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public abstract void setUp (@Nonnull ResourceFileSystem fileSystem,
                                @Nonnull Map<String, String> resourceProperties);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public abstract void performAssertions (@Nonnull DefaultSite fixture);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createRootMockFolder (final @Nonnull ResourceFileSystem fileSystem,
                                                 final @Nonnull String name)
      {
        final ResourceFile folder = createMockFolder(name);
        when(fileSystem.findFileByPath(eq(name))).thenReturn(folder);
        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createMockFolder (final @Nonnull ResourceFileSystem fileSystem,
                                             final @Nonnull ResourceFile parentFolder,
                                             final @Nonnull String name)
      {
        final String path = parentFolder.getPath() + "/" + name;

        final ResourceFile folder = createMockFolder(name);
        when(folder.getParent()).thenReturn(parentFolder);
        when(folder.getPath()).thenReturn(path);
        when(folder.toString()).thenReturn(path);
        when(fileSystem.findFileByPath(eq(path))).thenReturn(folder);

        final Collection<ResourceFile> children = parentFolder.getChildren();
        children.add(folder);
        when(parentFolder.getChildren()).thenReturn(children);

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createMockFile (final @Nonnull ResourceFileSystem fileSystem,
                                           final @Nonnull ResourceFile parentFolder,
                                           final @Nonnull String name)
      {
        final String path = parentFolder.getPath() + "/" + name;

        final ResourceFile file = createMockFile(name);
        when(file.getParent()).thenReturn(parentFolder);
        when(file.getPath()).thenReturn(path);
        when(file.toString()).thenReturn(path);
        when(fileSystem.findFileByPath(eq(path))).thenReturn(file);

        final Collection<ResourceFile> children = parentFolder.getChildren();
        children.add(file);
        when(parentFolder.getChildren()).thenReturn(children);

        return file;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceFile createMockFolder (final @Nonnull String name)
      {
        final ResourceFile folder = mock(ResourceFile.class);
        when(folder.getName()).thenReturn(name);
        when(folder.getPath()).thenReturn(name);
        when(folder.toString()).thenReturn(name);
        when(folder.isData()).thenReturn(false);
        when(folder.isFolder()).thenReturn(true);
        when(folder.getChildren()).thenReturn(new ArrayList<ResourceFile>());

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceFile createMockFile (final @Nonnull String name)
      {
        final ResourceFile folder = mock(ResourceFile.class);
        when(folder.getName()).thenReturn(name);
        when(folder.getPath()).thenReturn(name);
        when(folder.toString()).thenReturn(name);
        when(folder.isData()).thenReturn(true);
        when(folder.isFolder()).thenReturn(false);
//        when(folder.getChildren()).thenReturn(new ArrayList<ResourceFile>());

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected static void assertItem (final @Nonnull Map<String, ?> map,
                                      final @Nonnull String key,
                                      final @CheckForNull String expectedValue)
      {
        if (expectedValue == null)
          {
            assertThat(key, map.get(key), is(nullValue()));
          }
        else
          {
            assertThat(key, map.get(key), is(notNullValue()));
            assertThat(map.get(key).toString(), is(expectedValue));
          }
      }
  }
