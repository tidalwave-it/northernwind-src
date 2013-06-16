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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * A file backing a {@link Resource}. There can be various implementations of this interface: plain files on the local
 * disk, items in a zip file, elements of a repository such as Mercurial or Git, objects stored within a database,
 * etc...
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ResourceFile
  {
    /*******************************************************************************************************************
     *
     * Returns the {@link ResourceFileSystem} this file belongs to.
     *
     * @return  the {@code ResourceFileSystem}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFileSystem getFileSystem();

    /*******************************************************************************************************************
     *
     * Returns the name of this file (it doesn't include the full path).
     *
     * @return  the name of the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getName();

    /*******************************************************************************************************************
     *
     * Returns the full path of this file.
     * FIXME: the root object returns "" in place of "/" - this will change in future
     *
     * @return  the full path of the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath getPath();

    /*******************************************************************************************************************
     *
     * Returns {@code true} whether this file is a folder.
     *
     * @return  {@code true} for a folder
     *
     ******************************************************************************************************************/
    public boolean isFolder();

    /*******************************************************************************************************************
     *
     * Returns {@code true} whether this file is a plain file.
     *
     * @return  {@code true} for a file
     *
     ******************************************************************************************************************/
    public boolean isData();

    /*******************************************************************************************************************
     *
     * Returns the MIME type associated to the contents of this file. The value is achieved by querying the web server
     * context.
     *
     * @return  the MIME type
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getMimeType();

    /*******************************************************************************************************************
     *
     * Returns an {@link InputStream} that allows to read contents of this file.
     *
     * @return                         the {@code InputStream}
     * @throws  FileNotFoundException  if the physical data can't be accessed
     *
     ******************************************************************************************************************/
    @Nonnull
    public InputStream getInputStream()
      throws FileNotFoundException;

    /*******************************************************************************************************************
     *
     * Returns the full contents of this file as text.
     *
     * @param   encoding     the content encoding
     * @return               the contents
     * @throws  IOException  if an I/O error occurs
     *
     ******************************************************************************************************************/
    @Nonnull
    public String asText (@Nonnull String encoding)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Returns the full contents of this file as binary data.
     *
     * @return               the contents
     * @throws  IOException  if an I/O error occurs
     *
     ******************************************************************************************************************/
    @Nonnull
    public byte[] asBytes()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Returns the latest modification time of this file.
     *
     * @return  the latest modification time
     *
     ******************************************************************************************************************/
    @Nonnull
    public DateTime getLatestModificationTime();

    /*******************************************************************************************************************
     *
     * Returns the parent of this file.
     * FIXME: make @Nonnull, throws NotFoundException when no parent
     *
     * @return  the parent or null if no parent
     *
     ******************************************************************************************************************/
    public ResourceFile getParent();

    /*******************************************************************************************************************
     *
     * Returns a child file with the given name.
     * FIXME: make @Nonnull, throws NotFoundException when no parent
     *
     * @param   fileName  the child name
     * @return  the child or null if no child with that name
     *
     ******************************************************************************************************************/
    public ResourceFile getChildByName (@Nonnull String fileName);

    /*******************************************************************************************************************
     *
     * Returns all the direct children of this file.
     * TODO: merge with getChildren(true) using a Finder
     *
     * @return  the children
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<ResourceFile> getChildren();

    /*******************************************************************************************************************
     *
     * Returns all the children of this file.
     * TODO: merge with getChildren() using a Finder
     *
     * @param   recursive  if false, return only the direct child, if true returns all the descendants
     * @return  the children
     *
     ******************************************************************************************************************/
    @Nonnull
    public Collection<ResourceFile> getChildren (boolean recursive);

    /*******************************************************************************************************************
     *
     * FIXME: drop this - it won't work with virtual file systems
     *
     ******************************************************************************************************************/
    @Nonnull
    public File toFile();

    // TODO: methods below probably can be dropped, are only used in filesystem implementations
    /*******************************************************************************************************************
     *
     * Don't use
     *
     ******************************************************************************************************************/
    public void delete()
      throws IOException;

    /*******************************************************************************************************************
     *
     * Don't use
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFile createFolder (@Nonnull String name)
      throws IOException;

    /*******************************************************************************************************************
     *
     * Don't use
     *
     ******************************************************************************************************************/
    public void copyTo (@Nonnull ResourceFile targetFolder)
      throws IOException;
  }
