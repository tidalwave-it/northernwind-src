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

import com.vaadin.terminal.StreamResource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.ui.component.menu.vaadin.VaadinHorizontalMenuView;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.util.NotFoundException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import static it.tidalwave.northernwind.frontend.model.Media.Media;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link PageView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class VaadinPageView extends Window implements PageView
  {
    @Inject @Nonnull
    private WebSite webSite;
            
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public VaadinPageView() 
      {
        setStyleName("component-" + "page");
        ((AbstractLayout)getContent()).setMargin(false);
      }

    /*******************************************************************************************************************
     *
     * Set the contents.
     * 
     * @param  content  the new contents (must be a Vaadin {@link Component})
     *
     ******************************************************************************************************************/
    @Override
    public void setContents (final @Nonnull Object content) 
      throws IOException
      {
        log.info("setContents({} - {})", content.getClass(), content);
        removeAllComponents();
        // FIXME: this must be built from the configuration
        final VaadinHorizontalMenuView menuView = new VaadinHorizontalMenuView("nav");
        
        try { // FIXME
        menuView.setLinks(Arrays.asList
          (
            "/",
            "/Features",
            "/Download",
            "/Screenshots",
            "/Getting started",
            "/Blog & News (new)",
//            "/Contact",
            "/License",
            "/Developers"
          )); }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        try // FIXME to be moved to CSS
          {
            final Media media = webSite.find(Media).withRelativeUri("/blueBill_Mobile-Banner.png").result();
            final FileObject file = media.getResource().getFile();
            final InputStream is = file.getInputStream();
            addComponent(new Embedded("", new StreamResource(new StreamResource.StreamSource() 
              {
                @Override @Nonnull
                public InputStream getStream() 
                  {
                    return is;
                  }
              }, file.getNameExt(), getApplication())));
          }
        catch (NotFoundException e) 
          {
            log.error("", e);
          }
       
        addComponent(menuView);
        addComponent((Component)content);
      }
  }
