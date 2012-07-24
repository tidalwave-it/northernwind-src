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
public interface NwFileObject 
  {
    @Nonnull
    public String getNameExt(); // TODO: rename to getName()

    @Nonnull
    public String getPath();
    
    public boolean isFolder();

    public boolean isData();
    
    @Nonnull
    public String getMIMEType(); // TODO: rename to getMimeType()

    @Nonnull
    public InputStream getInputStream()
      throws FileNotFoundException;
    
    @Nonnull
    public String asText()
      throws IOException;
    
    public String asText (String encoding)
      throws IOException;
    
    @Nonnull
    public byte[] asBytes()
      throws IOException;

    @Nonnull
    public Date lastModified(); // TODO: rename to getLastModifiedDateTime(), return JodaTime DateTime

    public NwFileObject getParent();

    public NwFileObject getFileObject (@Nonnull String fileName); // TODO: rename to getChild()

    public NwFileObject[] getChildren(); // TODO: return Collection<>

    public Enumeration<? extends NwFileObject> getChildren (boolean recursive); // TODO: return Collection, replace boolean with enum

    @Nonnull
    public File toFile();

    public void delete()
      throws IOException;

    @Nonnull
    public NwFileObject createFolder (@Nonnull String name)
      throws IOException;

    public void copyTo (@Nonnull NwFileObject targetFolder)
      throws IOException;
    
    @Nonnull
    public NwFileSystem getFileSystem();
  }
