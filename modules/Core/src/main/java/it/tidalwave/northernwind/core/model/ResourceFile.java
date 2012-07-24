/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Enumeration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ResourceFile 
  {
    @Nonnull
    public ResourceFileSystem getFileSystem();
    
    @Nonnull
    public String getNameExt(); // TODO: rename to getName()

    @Nonnull
    public String getPath();
    
    public boolean isFolder();

    public boolean isData();
    
    @Nonnull
    public String getMimeType();

    @Nonnull
    public InputStream getInputStream()
      throws FileNotFoundException;
    
    @Nonnull
    public String asText (String encoding)
      throws IOException;
    
    @Nonnull
    public byte[] asBytes()
      throws IOException;

    @Nonnull
    public Date lastModified(); // TODO: rename to getLastModifiedDateTime(), return JodaTime DateTime

    public ResourceFile getParent();

    public ResourceFile getFileObject (@Nonnull String fileName); // TODO: rename to getChild()

    public ResourceFile[] getChildren(); // TODO: return Collection<>

    public Enumeration<? extends ResourceFile> getChildren (boolean recursive); // TODO: return Collection, replace boolean with enum

    @Nonnull
    public File toFile();

    // TODO: methods below probably can be dropped, are only used in filesystem implementations
    public void delete()
      throws IOException;

    @Nonnull
    public ResourceFile createFolder (@Nonnull String name)
      throws IOException;

    public void copyTo (@Nonnull ResourceFile targetFolder)
      throws IOException;
  }
