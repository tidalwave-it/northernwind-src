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
import java.util.Collections;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class EmptyFileSystemTestSupport extends FileSystemTestSupport
  {
    public EmptyFileSystemTestSupport (final @Nonnull ResourceFileSystem fileSystem)
      {
        super(fileSystem);
      }
    
    public void initialize()
      {
        final ResourceFile documentFolder = createRootMockFolder("documentPath");
        final ResourceFile mediaFolder    = createRootMockFolder("mediaPath");
        final ResourceFile libraryFolder  = createRootMockFolder("libraryPath");
        final ResourceFile nodeFolder     = createRootMockFolder("nodePath");
      }

    @Override
    public void performAssertions (final @Nonnull DefaultSite fixture) 
      {
        assertThat(fixture.documentMapByRelativePath.size(), is(1));
        assertThat(fixture.documentMapByRelativePath.get("/").toString(), is("MockContent(path=documentPath)"));
        
        assertThat(fixture.libraryMapByRelativePath.isEmpty(), is(true));
        
        assertThat(fixture.mediaMapByRelativePath.isEmpty(), is(true));
        
        assertThat(fixture.nodeMapByRelativePath.size(), is(1));
        assertThat(fixture.nodeMapByRelativePath.get("/").toString(), is("MockSiteNode(path=nodePath)"));
       
        assertThat(fixture.nodeMapByRelativeUri.size(), is(1));
        assertThat(fixture.nodeMapByRelativeUri.get("relativeUriFor(nodePath)").toString(), is("MockSiteNode(path=nodePath)"));
      }
  }
