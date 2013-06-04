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
import lombok.RequiredArgsConstructor;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public abstract class FileSystemTestSupport 
  {
    @Nonnull
    protected final ResourceFileSystem resourceFileSystem;
    
    public abstract void initialize();
    
    public abstract void performAssertions (@Nonnull DefaultSite fixture);
    
    @Nonnull
    protected ResourceFile createRootMockFolder (final @Nonnull String name)
      {
        final ResourceFile folder = createMockFolder(name);
        when(resourceFileSystem.findFileByPath(eq(name))).thenReturn(folder);
        return folder;
      }
    
    @Nonnull
    protected ResourceFile createMockFolder (final @Nonnull String name)
      {
        final ResourceFile folder = mock(ResourceFile.class);
        when(folder.getName()).thenReturn(name);
        when(folder.getPath()).thenReturn(name); // FIXME: parent
        when(folder.isData()).thenReturn(false);
        when(folder.isFolder()).thenReturn(true);
        when(folder.getChildren()).thenReturn(Collections.<ResourceFile>emptyList());
        when(folder.toString()).thenReturn(name); // FIXME: parent
        
        return folder;
      }
  }
