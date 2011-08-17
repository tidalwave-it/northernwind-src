/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import org.openide.filesystems.FileObject;

/***********************************************************************************************************************
 *
 * A resource is the basic entity of NorthernWind. It's something located in the filesystem and represented by a file
 * or a folder, with a bag of properties.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface Resource 
  {
    public static final Class<Resource> Resource = Resource.class;
     
    /*******************************************************************************************************************
     *
     * Returns the file backing this resource. It can be a plain file or a directory in function of the resource type.
     * 
     * @return  the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public FileObject getFile();
    
    /*******************************************************************************************************************
     *
     * Returns the properties of this resource.
     * 
     * @return  the properties
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getProperties();
    
    /*******************************************************************************************************************
     *
     * Returns the property group of this resources with the given id.
     * 
     * @param   id                  the id of the property group
     * @return                      the properties
     * @throws  NotFoundException   if no property group with that id exists
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceProperties getPropertyGroup (@Nonnull Id id)
      throws NotFoundException;
    
    /*******************************************************************************************************************
     *
     * A placeholder resource doesn't contain anything, it just provides a placeholder for a path element. For instance,
     * if in the pair parent/child child is a placeholder, the relative URI /parent/child will be mapped to parent 
     * (which supposedly manages path params).
     *
     ******************************************************************************************************************/
    public boolean isPlaceHolder();
  }