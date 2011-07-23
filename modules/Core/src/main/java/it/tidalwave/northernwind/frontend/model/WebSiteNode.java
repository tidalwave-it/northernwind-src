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
import javax.inject.Inject;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A node of the website, mapped to a given URL.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @RequiredArgsConstructor @Slf4j @ToString
public class WebSiteNode
  {
    public static final Class<WebSiteNode> WebSiteNode = WebSiteNode.class;
    
    public static final Key<String> PROP_NAVIGATION_TITLE = new Key<String>("NavigationTitle");
    
    @Nonnull @Inject
    private ViewFactory viewFactory;
    
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;
    
    @Nonnull @Getter
    private final String relativeUri;

    /*******************************************************************************************************************
     *
     * Creates a new instance with the given configuration file and mapped to the given URI.
     * 
     * @param  file          the file with the configuration
     * @param  relativeUri   the bound URI
     *
     ******************************************************************************************************************/
    public WebSiteNode (final @Nonnull FileObject file, final @Nonnull String relativeUri)
      {
        resource = new Resource(file);  
        this.relativeUri = relativeUri;
      }

    /*******************************************************************************************************************
     *
     * Creates the UI contents for this {@code WebSiteNode}.
     * 
     * @return   the contents
     *
     ******************************************************************************************************************/
    @Nonnull
    public Object createContents() // FIXME: rename to createView
      throws IOException, NotFoundException
      {
        // FIXME: this is temporary
        return viewFactory.createView("http://northernwind.tidalwave.it/component/Article", "main", this); 
        // END FIXME
      }
  }