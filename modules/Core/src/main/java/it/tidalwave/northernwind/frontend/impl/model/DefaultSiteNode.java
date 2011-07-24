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
package it.tidalwave.northernwind.frontend.impl.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayoutXmlUnmarshaller;
import it.tidalwave.northernwind.frontend.impl.ui.LayoutLoggerVisitor;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @RequiredArgsConstructor @Slf4j @ToString(exclude={"layout"})
/* package */ class DefaultSiteNode implements SiteNode
  {
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;
    
    @Nonnull @Getter
    private final String relativeUri; // TODO: is this needed?
    
    @Nonnull @Getter
    private DefaultLayout layout;

    /*******************************************************************************************************************
     *
     * Creates a new instance with the given configuration file and mapped to the given URI.
     * 
     * @param  file          the file with the configuration
     * @param  relativeUri   the bound URI
     *
     ******************************************************************************************************************/
    public DefaultSiteNode (final @Nonnull FileObject file, final @Nonnull String relativeUri)
      throws IOException, NotFoundException
      {
        resource = new DefaultResource(file);  
        this.relativeUri = relativeUri;
        loadLayout();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoe}
     *
     ******************************************************************************************************************/
    private void loadLayout() 
      throws IOException, NotFoundException
      {
        DefaultLayout tempLayout = null;
        
        for (final FileObject layoutFile : Utilities.getInheritedPropertyFiles(resource.getFile(), "Layout_en.xml"))
          {
            log.trace(">>>> reading layout from /{}...", layoutFile.getPath());
            final DefaultLayout localLayout = new DefaultLayoutXmlUnmarshaller(layoutFile).unmarshal();
            localLayout.accept(new LayoutLoggerVisitor());           
            tempLayout = (tempLayout == null) ? localLayout : (DefaultLayout)tempLayout.withOverride(localLayout);
          }
          
        this.layout = tempLayout;
        log.info(">>>> layout for /{}:", resource.getFile().getPath());
        layout.accept(new LayoutLoggerVisitor());
      }
  }