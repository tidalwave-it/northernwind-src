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

import static it.tidalwave.northernwind.core.impl.model.FileSystemTestSupport.assertItem;
import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.SiteNode;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class TestFileSystemWithAFewStuff1 extends EmptyTestFileSystem
  {
    public TestFileSystemWithAFewStuff1()
      {
        super("File system with a few stuff 1");
      }
    
    @Override
    public void setUp (final @Nonnull ResourceFileSystem fileSystem,
                       final @Nonnull Map<String, String> resourceProperties)
      {
        super.setUp(fileSystem, resourceProperties);
        // FIXME: this is flat, create some hierarchy
        createMockFolder(fileSystem, documentFolder, "document1");
        createMockFolder(fileSystem, documentFolder, "document2");
        createMockFolder(fileSystem, documentFolder, "document3");
        createMockFolder(fileSystem, nodeFolder, "node1");
        createMockFolder(fileSystem, nodeFolder, "node2");
        createMockFolder(fileSystem, nodeFolder, "node3");
        resourceProperties.put("structure/node3." + SiteNode.PROPERTY_MANAGES_PATH_PARAMS.stringValue(), "true");
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
    public void performAssertions (final @Nonnull DefaultSite fixture) 
      {
        assertThat(fixture.documentMapByRelativePath.size(), is(4));
        assertItem(fixture.documentMapByRelativePath, "/document1", "Content(path=content/document/document1)");
        assertItem(fixture.documentMapByRelativePath, "/document1", "Content(path=content/document/document1)");
        assertItem(fixture.documentMapByRelativePath, "/document2", "Content(path=content/document/document2)");
        assertItem(fixture.documentMapByRelativePath, "/document3", "Content(path=content/document/document3)");
        
        assertThat(fixture.libraryMapByRelativePath.size(), is(2));
        assertItem(fixture.libraryMapByRelativePath, "/library1", "Resource(path=content/library/library1)");
        assertItem(fixture.libraryMapByRelativePath, "/library2", "Resource(path=content/library/library2)");
        
        assertThat(fixture.mediaMapByRelativePath.size(), is(3));
        assertItem(fixture.mediaMapByRelativePath, "/media1", "Media(path=content/media/media1)");
        assertItem(fixture.mediaMapByRelativePath, "/media2", "Media(path=content/media/media2)");
        assertItem(fixture.mediaMapByRelativePath, "/media3", "Media(path=content/media/media3)");
        
        assertThat(fixture.nodeMapByRelativePath.size(), is(5));
        assertItem(fixture.nodeMapByRelativePath, "/",          "Node(path=structure)");
        assertItem(fixture.nodeMapByRelativePath, "/node1",     "Node(path=structure/node1)");
        assertItem(fixture.nodeMapByRelativePath, "/node2",     "Node(path=structure/node2)");
        assertItem(fixture.nodeMapByRelativePath, "/node3",     "Node(path=structure/node3)");
       
        assertThat(fixture.nodeMapByRelativeUri.size(), is(5));
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure)",           "Node(path=structure)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node1)",     "Node(path=structure/node1)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node2)",     "Node(path=structure/node2)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node3)",     "Node(path=structure/node3)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node3)/a",   "Node(path=structure/node3)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node3)/b",   "Node(path=structure/node3)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node3)/c/d", "Node(path=structure/node3)");
        assertItem(fixture.nodeMapByRelativeUri, "relativeUriFor(structure/node4)",     "Node(path=structure/node4)");
      }
  }
