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
package it.tidalwave.northernwind.frontend.model.impl;

import javax.annotation.Nonnull;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
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
@Configurable(preConstruction=true) @RequiredArgsConstructor @Slf4j @ToString(exclude={"viewFactory", "layout"})
/* package */ class DefaultSiteNode implements SiteNode
  {
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;
    
    @Nonnull @Getter
    private final String relativeUri;
    
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
        createLayout();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoe}
     *
     ******************************************************************************************************************/
    private void createLayout() 
      throws IOException, NotFoundException
      {
        // FIXME: this is temporary
//<layout>
//    <component id="base" type="http://northernwind.tidalwave.it/component/NodeContainer">
//        <component id="local" type="http://northernwind.tidalwave.it/component/Container">
//            <component id="local-1" type="http://northernwind.tidalwave.it/component/Sidebar">
//                <component id="content3" type="http://northernwind.tidalwave.it/component/Container">
//                    <component id="content3-1" type="http://northernwind.tidalwave.it/component/NewsIterator"/>
//                </component>
//            </component>
//        </component>
//        <component id="nav" type="http://northernwind.tidalwave.it/component/Container">
//            <component id="nav-1" type="http://northernwind.tidalwave.it/component/HorizontalMenu"/>
//        </component>
//        <component id="main" type="http://northernwind.tidalwave.it/component/Container">
//            <component id="main-1" type="http://northernwind.tidalwave.it/component/HtmlFragment"/>
//            <component id="main-2" type="http://northernwind.tidalwave.it/component/HtmlTextWithTitle"/>
//            <component id="main-3" type="http://northernwind.tidalwave.it/component/Top1000Ranking"/>
//        </component>
//        <component id="footer" type="http://northernwind.tidalwave.it/component/Container">
//            <component id="footer-1" type="http://northernwind.tidalwave.it/component/HtmlFragment"/>
//            <component id="footer-2" type="http://northernwind.tidalwave.it/component/StatCounter"/>
//        </component>
//        <component id="breadcrumb" type="http://northernwind.tidalwave.it/component/Container">
//            <component id="breadcrumb-1" type="http://northernwind.tidalwave.it/component/AddThis"/>
//        </component>
//    </component>
//</layout>
        
        layout = new DefaultLayout("base", "http://northernwind.tidalwave.it/component/NodeContainer");
        final DefaultLayout local = new DefaultLayout("local", "http://northernwind.tidalwave.it/component/Container");
        final DefaultLayout local_1 = new DefaultLayout("local-1", "http://northernwind.tidalwave.it/component/Sidebar");
        final DefaultLayout content3 = new DefaultLayout("content3", "http://northernwind.tidalwave.it/component/Container");
//        final DefaultLayout content3_1 = new DefaultLayout("content3-1", "http://northernwind.tidalwave.it/component/NewsIterator");
        final DefaultLayout nav = new DefaultLayout("nav", "http://northernwind.tidalwave.it/component/Container");
        final DefaultLayout nav_1 = new DefaultLayout("nav-1", "http://northernwind.tidalwave.it/component/HorizontalMenu");
        final DefaultLayout main = new DefaultLayout("main", "http://northernwind.tidalwave.it/component/Container");
        final DefaultLayout main_1 = new DefaultLayout("main-1", "http://northernwind.tidalwave.it/component/HtmlFragment");
        final DefaultLayout main_2 = new DefaultLayout("main-2", "http://northernwind.tidalwave.it/component/HtmlTextWithTitle");
//        final DefaultLayout main_3 = new DefaultLayout("main-3", "http://northernwind.tidalwave.it/component/Top1000Ranking");
        final DefaultLayout footer = new DefaultLayout("footer", "http://northernwind.tidalwave.it/component/Container");
        final DefaultLayout footer_1 = new DefaultLayout("footer-1", "http://northernwind.tidalwave.it/component/HtmlFragment");
        final DefaultLayout footer_2 = new DefaultLayout("footer-2", "http://northernwind.tidalwave.it/component/StatCounter");
        final DefaultLayout breadcrumb = new DefaultLayout("breadcrumb", "http://northernwind.tidalwave.it/component/Container");
        final DefaultLayout breadcrumb_1 = new DefaultLayout("breadcrumb-1", "http://northernwind.tidalwave.it/component/AddThis");
        
        layout.add(local);
        layout.add(nav);
        layout.add(main);
        layout.add(footer);
        layout.add(breadcrumb);
        
        local.add(local_1);
        local_1.add(content3);
//        content3.add(content3_1);
        
        nav.add(nav_1);
        
        main.add(main_1);
        main.add(main_2);
//        main.add(main_3);

        footer.add(footer_1);
        footer.add(footer_2);
        
        breadcrumb.add(breadcrumb_1);
        
//        final SiteNodeView pageContent = viewFactory.createSiteNodeView();
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/Sidebar", "local-1", this));
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/HorizontalMenu", "nav-1", this));
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/HtmlFragment", "main-1", this));
//        
//        if (relativeUri.contains("Blog"))
//          {
//            pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/Blog", "main-1", this));
//          }        
//        else
//          {
//            pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/HtmlTextWithTitle", "main-2", this));
//          }
//        
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/HtmlFragment", "footer-1", this));
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/StatCounter", "footer-2", this));
//        pageContent.add(viewFactory.getLayout("http://northernwind.tidalwave.it/component/AddThis", "breadcrumb-1", this));
//        return pageContent;
//        // END FIXME
      }
  }