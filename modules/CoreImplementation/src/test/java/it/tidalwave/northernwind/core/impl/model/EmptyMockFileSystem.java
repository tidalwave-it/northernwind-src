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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class EmptyMockFileSystem extends MockFileSystemSupport
  {
    // FIXME: these values are not thread-safe, the same instance is reused...
    protected ResourceFile contentFolder;
    protected ResourceFile documentFolder;
    protected ResourceFile mediaFolder;
    protected ResourceFile libraryFolder;
    protected ResourceFile nodeFolder;

    public EmptyMockFileSystem()
      {
        super("Empty file system");
      }

    protected EmptyMockFileSystem (final @Nonnull String name)
      {
        super(name);
      }

    @Override
    public void setUp (final @Nonnull ResourceFileSystem fileSystem,
                       final @Nonnull MockModelFactory.PropertySetter propertySetter)
      {
        contentFolder  = createRootMockFolder(fileSystem, "/content");
        documentFolder = createMockFolder(fileSystem, contentFolder, "/document");
        mediaFolder    = createMockFolder(fileSystem, contentFolder, "/media");
        libraryFolder  = createMockFolder(fileSystem, contentFolder, "/library");
        nodeFolder     = createRootMockFolder(fileSystem, "/structure");
      }

    @Override
    public void performAssertions (final @Nonnull DefaultSite underTest)
      {
        assertThat(underTest.documentMapByRelativePath.size(), is(1));
        assertItem(underTest.documentMapByRelativePath, "/", "Content(path=/content/document)");

        assertThat(underTest.libraryMapByRelativePath.isEmpty(), is(true));

        assertThat(underTest.mediaMapByRelativePath.isEmpty(), is(true));

        assertThat(underTest.nodeMapByRelativePath.size(), is(1));
        assertItem(underTest.nodeMapByRelativePath, "/", "Node(path=/structure)");

        assertThat(underTest.nodeMapByRelativeUri.size(), is(1));
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure", "Node(path=/structure)");
      }
  }
