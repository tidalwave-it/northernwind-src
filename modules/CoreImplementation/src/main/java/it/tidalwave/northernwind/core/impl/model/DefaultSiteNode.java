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

import it.tidalwave.northernwind.core.model.ModelFactory;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.impl.ui.LayoutLoggerVisitor;
import lombok.Cleanup;
import lombok.Delegate;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j @ToString(exclude={"layout", "site"})
/* package */ class DefaultSiteNode implements SiteNode
  {
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;
    
    @Nonnull @Getter
    private final Layout layout;
    
    @Inject @Nonnull
    private DefaultSite site;

    @Inject @Nonnull
    private ModelFactory modelFactory;
    
    /*******************************************************************************************************************
     *
     * Creates a new instance with the given configuration file and mapped to the given URI.
     * 
     * @param  file          the file with the configuration
     * @param  relativeUri   the bound URI
     *
     ******************************************************************************************************************/
    public DefaultSiteNode (final @Nonnull FileObject file)
      throws IOException, NotFoundException
      {
        resource = modelFactory.createResource(file);  
        layout = loadLayout();

        if (site.isLogConfigurationEnabled() || log.isDebugEnabled())
          {
            log.info(">>>> layout for /{}:", resource.getFile().getPath());
            layout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.INFO));
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoe}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Layout loadLayout() 
      throws IOException, NotFoundException
      {
        Layout layout = null;
        
        for (final FileObject layoutFile : Utilities.getInheritedPropertyFiles(resource.getFile(), "Layout_en.xml"))
          {
            log.trace(">>>> reading layout from /{}...", layoutFile.getPath());
            final @Cleanup InputStream is = layoutFile.getInputStream();
            final DefaultLayout overridingLayout = new DefaultLayout().as(Unmarshallable).unmarshal(is);
            is.close();
            layout = (layout == null) ? overridingLayout : layout.withOverride(overridingLayout);
            
            if (log.isDebugEnabled())
              { 
                overridingLayout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.DEBUG));           
              }
          }
        
        return layout;
      }
  }