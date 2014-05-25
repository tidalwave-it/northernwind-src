/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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

import static it.tidalwave.northernwind.core.impl.model.FileSystemTestSupport.assertItem;
import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class EmptyTestFileSystem extends FileSystemTestSupport
  {
    // FIXME: these values are not thread-safe, the same instance is reused...
    protected ResourceFile contentFolder;
    protected ResourceFile documentFolder;
    protected ResourceFile mediaFolder;
    protected ResourceFile libraryFolder;
    protected ResourceFile nodeFolder;

    public EmptyTestFileSystem()
      {
        super("Empty file system");
      }

    protected EmptyTestFileSystem (final @Nonnull String name)
      {
        super(name);
      }

    @Override
    public void setUp (final @Nonnull ResourceFileSystem fileSystem,
                       final @Nonnull Map<String, String> resourceProperties)
      {
        contentFolder  = createRootMockFolder(fileSystem, "/content");
        documentFolder = createMockFolder(fileSystem, contentFolder, "/document");
        mediaFolder    = createMockFolder(fileSystem, contentFolder, "/media");
        libraryFolder  = createMockFolder(fileSystem, contentFolder, "/library");
        nodeFolder     = createRootMockFolder(fileSystem, "/structure");
      }

    @Override
    public void performAssertions (final @Nonnull DefaultSite fixture)
      {
        assertThat(fixture.documentMapByRelativePath.size(), is(1));
        assertItem(fixture.documentMapByRelativePath, "/", "Content(path=/content/document)");

        assertThat(fixture.libraryMapByRelativePath.isEmpty(), is(true));

        assertThat(fixture.mediaMapByRelativePath.isEmpty(), is(true));

        assertThat(fixture.nodeMapByRelativePath.size(), is(1));
        assertItem(fixture.nodeMapByRelativePath, "/", "Node(path=/structure)");

        assertThat(fixture.nodeMapByRelativeUri.size(), is(1));
        assertItem(fixture.nodeMapByRelativeUri, "/relativeUriFor:/structure", "Node(path=/structure)");
      }
  }
