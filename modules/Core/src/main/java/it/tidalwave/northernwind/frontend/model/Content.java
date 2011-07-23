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
import org.openide.filesystems.FileObject;
import lombok.Delegate;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a page.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString
public class Content 
  {
    public static final Class<Content> Content = Content.class;
    
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;

    /*******************************************************************************************************************
     *
     * Creates a new {@code Content} with the given configuration file.
     * 
     * @param   file   the configuration file
     *
     ******************************************************************************************************************/
    public Content (final @Nonnull FileObject file)
      {
        resource = new Resource(file);  
      }
  }
