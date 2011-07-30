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
package it.tidalwave.northernwind.frontend.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import org.openide.filesystems.FileObject;

/***********************************************************************************************************************
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
     * Returns the file backing this resource.
     * 
     * @return  the file
     *
     ******************************************************************************************************************/
    @Nonnull
    public FileObject getFile();
    
    /*******************************************************************************************************************
     *
     * Retrieves a property.
     * 
     * @param   key                 the property key
     * @return                      the property value
     * @throws  NotFoundException   if the property doesn't exist
     *
     ******************************************************************************************************************/
    @Nonnull
    public <Type> Type getProperty (@Nonnull Key<Type> key)
      throws NotFoundException, IOException;

    /*******************************************************************************************************************
     *
     * Retrieves a property, eventualyl returning a default value.
     * 
     * @param   key                 the property key
     * @param   defaultValue        the default value to return when the property doesn't exist
     * @return                      the property value
     *
     ******************************************************************************************************************/
    @Nonnull
    public <Type> Type getProperty (@Nonnull Key<Type> key, @Nonnull Type defaultValue)
      throws IOException;
  }