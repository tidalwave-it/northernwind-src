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
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
interface FileDelegateExclusions 
  {
    public void delete();

    public boolean hasExt(String ext);

    public OutputStream getOutputStream();

    public boolean canRevert();

    public void revert();

    public URL getURL();

    public URL toURL();

    public URI toURI();

    public FileSystem getFileSystem();

    public FileObject getParent();

    public FileObject move(FileLock lock, FileObject target, String name, String ext);

    public FileObject copy(FileObject target, String name, String ext);

    public FileObject createData(String name, String ext);

    public FileObject createFolder(String name);
    //        public Enumeration<? extends FileObject> getData (boolean rec); calls getChildren()
  }
