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
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.SiteNodeView;
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
@Configurable(preConstruction=true) @RequiredArgsConstructor @Slf4j @ToString(exclude="viewFactory")
/* package */ class DefaultSiteNode implements SiteNode
  {
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
    public DefaultSiteNode (final @Nonnull FileObject file, final @Nonnull String relativeUri)
      {
        resource = new DefaultResource(file);  
        this.relativeUri = relativeUri;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoe}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNodeView createView() 
      throws IOException, NotFoundException
      {
        // FIXME: this is temporary
//base.content = /content/document/Google Analytics
//content3.content = /content/document/News
//footer.content = /content/document/Copyright
//main.content = /content/document/Mobile
//nav.content = /content/document/Mobile, Features, Download, Screenshots, Getting started, Blog & News, Contacts, License, Developers          
          
        final SiteNodeView pageContent = viewFactory.createSiteNodeView();
        pageContent.add(viewFactory.createView("http://northernwind.tidalwave.it/component/HorizontalMenu", "nav", this));
        
        if (relativeUri.contains("Blog"))
          {
            pageContent.add(viewFactory.createView("http://northernwind.tidalwave.it/component/Blog", "main", this));
          }        
        else
          {
            pageContent.add(viewFactory.createView("http://northernwind.tidalwave.it/component/Article", "main", this));
          }
        
        pageContent.add(viewFactory.createView("http://northernwind.tidalwave.it/component/Article", "footer", this));
        return pageContent;
        // END FIXME
      }
  }