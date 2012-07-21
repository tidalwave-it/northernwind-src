/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static lombok.AccessLevel.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access=PRIVATE) @Slf4j
public final class Utilities 
  {
    /*******************************************************************************************************************
     *
     * Computes a list of property files to implement inheritance. Property files are enumerated starting from the root
     * up to the current folder, plus an eventual local override.
     * 
     * @return  a list of property files
     *
     ******************************************************************************************************************/
    @Nonnull
    public static List<FileObject> getInheritedPropertyFiles (final @Nonnull FileObject folder, final @Nonnull String propertyFileName)
      {
        log.trace("getInheritedPropertyFiles({}, {})", folder.getPath(), propertyFileName);
        
        final List<FileObject> files = new ArrayList<FileObject>();
        
        for (FileObject parent = folder; parent.getParent() != null; parent = parent.getParent()) // TODO: refactor with recursion
          {            
            log.trace(">>>> probing {} ...", parent.getPath() + "/" + propertyFileName);
            final FileObject propertyFile = parent.getFileObject(propertyFileName);
            
            if (propertyFile != null)
              {  
                files.add(propertyFile);                    
              }
          }
        
        Collections.reverse(files);

        final FileObject propertyFile = folder.getFileObject("Override" + propertyFileName);

        if (propertyFile != null)
          {  
            files.add(propertyFile);                    
          }
        
        log.trace(">>>> property file candidates: {}", files);
        
        return files;
      }
   }
