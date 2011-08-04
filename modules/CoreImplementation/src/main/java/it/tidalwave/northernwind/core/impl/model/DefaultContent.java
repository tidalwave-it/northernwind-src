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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import lombok.Delegate;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a {@code Node}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j @ToString
/* package */ class DefaultContent implements Content
  {
    @Inject @Nonnull
    private Site site;
    
    @Inject @Nonnull
    private ModelFactory modelFactory;
    
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;

    /*******************************************************************************************************************
     *
     * Creates a new {@code DefaultContent} with the given configuration file.
     * 
     * @param   file   the configuration file
     *
     ******************************************************************************************************************/
    public DefaultContent (final @Nonnull FileObject file)
      {
        resource = modelFactory.createResource(file);  
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<Content> findChildren() 
      {
        return new FolderBasedFinderSupport(this);
      }
  }
    
