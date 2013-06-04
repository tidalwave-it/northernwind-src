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
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
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
        super("File system with only ignored folders");
      }
    
    @Override
    public void setUp (final @Nonnull ResourceFileSystem fileSystem)
      {
        super.setUp(fileSystem);
        // FIXME: this is flat, create some hierarchy
        createMockFolder(fileSystem, documentFolder, "document1");
        createMockFolder(fileSystem, documentFolder, "document2");
        createMockFolder(fileSystem, documentFolder, "document3");
        createMockFolder(fileSystem, nodeFolder, "node1");
        createMockFolder(fileSystem, nodeFolder, "node2");
        createMockFolder(fileSystem, nodeFolder, "node3");
        createMockFolder(fileSystem, nodeFolder, "node4");
        createMockFile(fileSystem, mediaFolder, "media1");
        createMockFile(fileSystem, mediaFolder, "media2");
        createMockFile(fileSystem, mediaFolder, "media3");
        createMockFile(fileSystem, libraryFolder, "library1");
        createMockFile(fileSystem, libraryFolder, "library2");
      }


    @Override
    public void performAssertions (final @Nonnull DefaultSite fixture) 
      {
        assertThat(fixture.documentMapByRelativePath.size(), is(4));
        assertThat(fixture.documentMapByRelativePath.get("/").toString(),          is("MockContent(path=content/document)"));
        assertThat(fixture.documentMapByRelativePath.get("/document1").toString(), is("MockContent(path=content/document/document1)"));
        assertThat(fixture.documentMapByRelativePath.get("/document2").toString(), is("MockContent(path=content/document/document2)"));
        assertThat(fixture.documentMapByRelativePath.get("/document3").toString(), is("MockContent(path=content/document/document3)"));
        
        assertThat(fixture.libraryMapByRelativePath.size(), is(2));
        assertThat(fixture.libraryMapByRelativePath.get("/library1").toString(), is("MockResource(path=content/library/library1)"));
        assertThat(fixture.libraryMapByRelativePath.get("/library2").toString(), is("MockResource(path=content/library/library2)"));
        
        assertThat(fixture.mediaMapByRelativePath.size(), is(3));
        assertThat(fixture.mediaMapByRelativePath.get("/media1").toString(), is("MockMedia(path=content/media/media1)"));
        assertThat(fixture.mediaMapByRelativePath.get("/media2").toString(), is("MockMedia(path=content/media/media2)"));
        assertThat(fixture.mediaMapByRelativePath.get("/media3").toString(), is("MockMedia(path=content/media/media3)"));
        
        assertThat(fixture.nodeMapByRelativePath.size(), is(5));
        assertThat(fixture.nodeMapByRelativePath.get("/").toString(),      is("MockSiteNode(path=structure)"));
        assertThat(fixture.nodeMapByRelativePath.get("/node1").toString(), is("MockSiteNode(path=structure/node1)"));
        assertThat(fixture.nodeMapByRelativePath.get("/node2").toString(), is("MockSiteNode(path=structure/node2)"));
        assertThat(fixture.nodeMapByRelativePath.get("/node3").toString(), is("MockSiteNode(path=structure/node3)"));
        assertThat(fixture.nodeMapByRelativePath.get("/node4").toString(), is("MockSiteNode(path=structure/node4)"));
       
        assertThat(fixture.nodeMapByRelativeUri.size(), is(5));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(structure)").toString(),       is("MockSiteNode(path=structure)"));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(structure/node1)").toString(), is("MockSiteNode(path=structure/node1)"));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(structure/node2)").toString(), is("MockSiteNode(path=structure/node2)"));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(structure/node3)").toString(), is("MockSiteNode(path=structure/node3)"));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(structure/node4)").toString(), is("MockSiteNode(path=structure/node4)"));
      }
  }
