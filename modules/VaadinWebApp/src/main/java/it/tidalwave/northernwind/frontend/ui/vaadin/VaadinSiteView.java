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
package it.tidalwave.northernwind.frontend.ui.vaadin;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.Media.Media;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link SiteView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class VaadinSiteView extends Window implements SiteView
  {
    @Inject @Nonnull
    private Site site;
            
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public VaadinSiteView() 
      {
        setStyleName("component-" + "page");
        ((AbstractLayout)getContent()).setMargin(false);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     * 
     * @param  layout   must be a Vaadin component
     *
     ******************************************************************************************************************/
    @Override
    public void renderSiteNode (final @Nonnull SiteNode siteNode) 
      throws IOException
      {
        log.info("renderSiteNode({})", siteNode);
        
        removeAllComponents();
        
        try // FIXME to be moved to CSS
          {
            final Media media = site.find(Media).withRelativeUri("/blueBill_Mobile-Banner.png").result();
            final FileObject file = media.getFile();
            final InputStream is = file.getInputStream();
            addComponent(new Embedded("", new StreamResource(new StreamResource.StreamSource() 
              {
                @Override @Nonnull
                public InputStream getStream() 
                  {
                    return is;
                  }
              }, file.getNameExt(), getApplication())));
            
            final Visitor<Layout, Component> nodeViewBuilderVisitor = new VaadinNodeViewBuilderVisitor(siteNode);
            addComponent(siteNode.getLayout().accept(nodeViewBuilderVisitor));        
          }
        catch (NotFoundException e) 
          {
            log.error("", e);
          }
      }
  }