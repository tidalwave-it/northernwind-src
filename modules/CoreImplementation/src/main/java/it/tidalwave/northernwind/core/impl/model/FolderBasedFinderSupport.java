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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.UnsupportedEncodingException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.impl.util.UriUtilities.*;

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a page.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString
public class FolderBasedFinderSupport<Type extends Resource> extends SimpleFinderSupport<Type>
  {
    @Nonnull
    private final Class<Type> typeClass;
    
    @Nonnull
    private final FileObject file;
    
    @Inject @Nonnull
    private Site site;
    
    private final String uriPrefix;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public FolderBasedFinderSupport (final @Nonnull Type owner) 
      {
        this.typeClass = (Class<Type>)owner.getClass().getInterfaces()[0]; // FIXME assumes the interesting interface is [0]
        this.file = owner.getFile();
        this.uriPrefix = "/content/document"; // FIXME: site.getRelativeUriPrefix(typeClass);
      }
        
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected List<? extends Type> computeResults() 
      {
        final List<Type> result = new ArrayList<Type>();

        for (final FileObject childFile : Collections.list(file.getChildren(true)))
          {
            if (childFile.isFolder())
              {
                try 
                  {
                    final String relativeUri = urlDecodedPath((childFile.getPath()).substring(uriPrefix.length()));
                    result.add(site.find(typeClass).withRelativeUri(relativeUri).result());
                  }
                catch (UnsupportedEncodingException e) 
                  {
                    log.error("", e);
                  }
                catch (NotFoundException e) 
                  {
                    log.error("", e);
                  }
              }
          }

        return result;
      }
  }
