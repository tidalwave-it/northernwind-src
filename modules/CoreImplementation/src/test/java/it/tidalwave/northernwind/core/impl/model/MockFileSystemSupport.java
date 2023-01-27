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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFile.Finder;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.spi.ResourceFileFinderSupport;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * TODO: refactor with MockResourceFile,
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @ToString
public abstract class MockFileSystemSupport
  {
    private static Finder listFinder (@Nonnull final Collection<? extends ResourceFile> results)
      {
        return ResourceFileFinderSupport.withComputeResults(input -> new ArrayList<>(results));
      }

    @Nonnull
    private final String name;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public abstract void setUp (@Nonnull ResourceFileSystem fileSystem,
                                @Nonnull MockModelFactory.PropertySetter propertySetter);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public abstract void performAssertions (@Nonnull DefaultSite underTest);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createRootMockFolder (@Nonnull final ResourceFileSystem fileSystem,
                                                 @Nonnull final String name)
      {
        final var folder = createMockFolder(name);
        when(fileSystem.findFileByPath(eq(name))).thenReturn(folder);
        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createMockFolder (@Nonnull final ResourceFileSystem fileSystem,
                                             @Nonnull final ResourceFile parentFolder,
                                             @Nonnull final String name)
      {
        final var path = parentFolder.getPath().appendedWith(name);

        final var folder = createMockFolder(name);
        when(folder.getParent()).thenReturn(parentFolder);
        when(folder.getPath()).thenReturn(path);
        when(folder.toString()).thenReturn(path.asString());
        when(fileSystem.findFileByPath(eq(path.asString()))).thenReturn(folder);

        final Collection<ResourceFile> children = new ArrayList<>(parentFolder.findChildren().results());
        children.add(folder);
        when(parentFolder.findChildren()).thenReturn(listFinder(children));

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceFile createMockFile (@Nonnull final ResourceFileSystem fileSystem,
                                           @Nonnull final ResourceFile parentFolder,
                                           @Nonnull final String name)
      {
        final var path = parentFolder.getPath().appendedWith(name);

        final var file = createMockFile(name);
        when(file.getParent()).thenReturn(parentFolder);
        when(file.getPath()).thenReturn(path);
        when(file.toString()).thenReturn(path.asString());
        when(fileSystem.findFileByPath(eq(path.asString()))).thenReturn(file);

        final Collection<ResourceFile> children = new ArrayList<>(parentFolder.findChildren().results());
        children.add(file);
        when(parentFolder.findChildren()).thenReturn(listFinder(children));

        return file;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceFile createMockFolder (@Nonnull final String name)
      {
        final var folder = mock(ResourceFile.class);
        when(folder.getName()).thenReturn(name);
        when(folder.getPath()).thenReturn(ResourcePath.of(name));
        when(folder.isData()).thenReturn(false);
        when(folder.isFolder()).thenReturn(true);
        when(folder.findChildren()).thenReturn(listFinder(new ArrayList<>()));
        when(folder.toString()).thenReturn(name);

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceFile createMockFile (@Nonnull final String name)
      {
        final var folder = mock(ResourceFile.class);
        when(folder.getName()).thenReturn(name);
        when(folder.getPath()).thenReturn(ResourcePath.of(name));
        when(folder.toString()).thenReturn(name);
        when(folder.isData()).thenReturn(true);
        when(folder.isFolder()).thenReturn(false);
        when(folder.findChildren()).thenReturn(listFinder(new ArrayList<>()));

        return folder;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected static void assertItem (@Nonnull final Map<String, ?> map,
                                      @Nonnull final String key,
                                      @CheckForNull final String expectedValue)
      {
        if (expectedValue == null)
          {
            assertThat(key, map.get(key), is(nullValue()));
          }
        else
          {
            assertThat("Missing item: " + key + " in " + map.keySet(), map.get(key), is(notNullValue()));
            assertThat(map.get(key).toString(), is(expectedValue));
          }
      }
  }
