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
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
interface FileDelegateExclusions 
  {
    public void delete();

//    public boolean hasExt(String ext);

    public ResourceFileSystem getFileSystem();

    public ResourceFile getParent();

//    public NwFileObject move(FileLock lock, NwFileObject target, String name, String ext);
//
//    public NwFileObject copy(NwFileObject target, String name, String ext);
//
//    public NwFileObject createData(String name, String ext);

    public ResourceFile createFolder(String name);
    //        public Enumeration<? extends NwFileObject> getData (boolean rec); calls getChildren()
  }
