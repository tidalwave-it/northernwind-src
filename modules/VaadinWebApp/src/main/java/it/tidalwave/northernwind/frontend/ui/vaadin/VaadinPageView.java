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

import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.model.StructureLink;
import it.tidalwave.northernwind.frontend.ui.PageViewController;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractLayout;
import javax.annotation.Nonnull;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;
import it.tidalwave.northernwind.frontend.ui.component.menu.vaadin.VaadinHorizontalMenuView;
import java.io.File;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class VaadinPageView extends Window implements PageView
  {
    private final PageViewController controller;
    
    public VaadinPageView() 
      {
        log.info("VaadinPageView()");
        controller = new VaadinPageViewController(this); 
        setStyleName("component-" + "page");
        ((AbstractLayout)getContent()).setMargin(false);
      }

    @Override
    public void setContents (final @Nonnull Object content) 
      {
        removeAllComponents();
        final VaadinHorizontalMenuView menuView = new VaadinHorizontalMenuView("nav");
        menuView.setLinks(Arrays.asList
          (
            new StructureLink("Home", "/"),
            new StructureLink("Features", "/Features"),
            new StructureLink("Download", "/Download"),
            new StructureLink("Screenshots", "/Screenshots"),
            new StructureLink("Getting started", "/Getting started"),
            new StructureLink("Blog & News", "/Blog & News (new)"),
            new StructureLink("Contacts", "/Contact"),
            new StructureLink("License", "/License"),
            new StructureLink("Developers", "/Developers")
          ));
        
        final String h = "/home/fritz/Business/Tidalwave/Projects/WorkAreas/Tidalwave/tidalwave~other/InfoglueExporter/target/export";
        addComponent(new Embedded("", new FileResource(new File(h + "/content/media/blueBill_Mobile-Banner.png"), getApplication())));
        addComponent(menuView);
        addComponent((Component)content);
      }
  }
