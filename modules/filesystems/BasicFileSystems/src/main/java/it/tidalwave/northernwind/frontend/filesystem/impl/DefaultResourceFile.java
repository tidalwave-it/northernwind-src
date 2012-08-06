/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.joda.time.DateTime;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class DefaultResourceFile implements ResourceFile
  {
    @Getter @Nonnull
    private final ResourceFileSystem fileSystem;
    
    @Nonnull
    private final Path delegate;
    
    @Override @Nonnull
    public String getName() 
      {
        return delegate.getFileName().toString();
      }

    @Override @Nonnull
    public String getPath() 
      {
        return delegate.toAbsolutePath().toString();
      } 

    @Override
    public boolean isFolder() 
      {
        return delegate.toFile().isDirectory();
      }

    @Override
    public boolean isData()
      {
        return delegate.toFile().isFile();
      }

    @Override
    public String getMimeType() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public InputStream getInputStream()
      throws FileNotFoundException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public String asText(String encoding)
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public byte[] asBytes()
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public DateTime getLatestModificationTime() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile getParent() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile getChildByName(String fileName) 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }  

    @Override
    public Collection<ResourceFile> getChildren() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public Collection<ResourceFile> getChildren(boolean recursive)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override @Nonnull
    public File toFile() 
      {
        return delegate.toFile();
      }

    @Override
    public void delete() 
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public ResourceFile createFolder(String name) 
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public void copyTo(ResourceFile targetFolder) 
      throws IOException 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }
  }
