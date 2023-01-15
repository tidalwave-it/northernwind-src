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

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.SiteNode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class MockFileSystemWithAFewStuff1 extends EmptyMockFileSystem
  {
    public MockFileSystemWithAFewStuff1()
      {
        super("File system with a few stuff 1");
      }

    @Override
    public void setUp (@Nonnull final ResourceFileSystem fileSystem,
                       @Nonnull final MockModelFactory.PropertySetter propertySetter)
      {
        super.setUp(fileSystem, propertySetter);
        // FIXME: this is flat, create some hierarchy
        createMockFolder(fileSystem, documentFolder, "document1");
        createMockFolder(fileSystem, documentFolder, "document2");
        createMockFolder(fileSystem, documentFolder, "document3");
        createMockFolder(fileSystem, nodeFolder, "node1");
        createMockFolder(fileSystem, nodeFolder, "node2");
        createMockFolder(fileSystem, nodeFolder, "node3");
        propertySetter.setProperty("/structure/node3", SiteNode.P_MANAGES_PATH_PARAMS, true);
        createMockFolder(fileSystem, nodeFolder, "node4");
        createMockFile(fileSystem, mediaFolder, "media1");
        createMockFile(fileSystem, mediaFolder, "media2");
        createMockFile(fileSystem, mediaFolder, "media3");
        createMockFile(fileSystem, mediaFolder, "ignored1");
        createMockFile(fileSystem, libraryFolder, "library1");
        createMockFile(fileSystem, libraryFolder, "library2");
        createMockFile(fileSystem, libraryFolder, "ignored2");
      }

    @Override
    public void performAssertions (@Nonnull final DefaultSite underTest)
      {
        assertThat(underTest.documentMapByRelativePath.size(), is(4));
        assertItem(underTest.documentMapByRelativePath, "/",          "Content(path=/content/document)");
        assertItem(underTest.documentMapByRelativePath, "/document1", "Content(path=/content/document/document1)");
        assertItem(underTest.documentMapByRelativePath, "/document2", "Content(path=/content/document/document2)");
        assertItem(underTest.documentMapByRelativePath, "/document3", "Content(path=/content/document/document3)");

        assertThat(underTest.libraryMapByRelativePath.size(), is(2));
        assertItem(underTest.libraryMapByRelativePath, "/library1", "Resource(path=/content/library/library1)");
        assertItem(underTest.libraryMapByRelativePath, "/library2", "Resource(path=/content/library/library2)");

        assertThat(underTest.mediaMapByRelativePath.size(), is(3));
        assertItem(underTest.mediaMapByRelativePath, "/media1", "Media(path=/content/media/media1)");
        assertItem(underTest.mediaMapByRelativePath, "/media2", "Media(path=/content/media/media2)");
        assertItem(underTest.mediaMapByRelativePath, "/media3", "Media(path=/content/media/media3)");

        assertThat(underTest.nodeMapByRelativePath.size(), is(5));
        assertItem(underTest.nodeMapByRelativePath, "/",          "Node(path=/structure)");
        assertItem(underTest.nodeMapByRelativePath, "/node1",     "Node(path=/structure/node1)");
        assertItem(underTest.nodeMapByRelativePath, "/node2",     "Node(path=/structure/node2)");
        assertItem(underTest.nodeMapByRelativePath, "/node3",     "Node(path=/structure/node3)");

        assertThat(underTest.nodeMapByRelativeUri.size(), is(5));
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure",           "Node(path=/structure)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node1",     "Node(path=/structure/node1)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node1/a",   null);
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node2",     "Node(path=/structure/node2)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node2/a",   null);
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node3",     "Node(path=/structure/node3)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node3/a",   "Node(path=/structure/node3)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node3/b",   "Node(path=/structure/node3)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node3/c/d", "Node(path=/structure/node3)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node4",     "Node(path=/structure/node4)");
        assertItem(underTest.nodeMapByRelativeUri, "/relativeUriFor:/structure/node4/a",   null);
      }
  }
