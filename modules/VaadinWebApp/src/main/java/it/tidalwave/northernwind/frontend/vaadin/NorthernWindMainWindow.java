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
package it.tidalwave.northernwind.frontend.vaadin;

import com.vaadin.terminal.FileResource;
import javax.annotation.Nonnull;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;
import it.tidalwave.northernwind.frontend.vaadin.component.view.VaadinHorizontalMenuView;
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
public class NorthernWindMainWindow extends Window implements PageView
  {
    private final PageViewController controller;
    
    public NorthernWindMainWindow() 
      {
        controller = new DefaultPageViewController(this);
      }

    @Override
    public void setContent (final @Nonnull Object content) 
      {
        removeAllComponents();
        setBorder(0);
        final VaadinHorizontalMenuView menuView = new VaadinHorizontalMenuView();
        menuView.setLinks(Arrays.asList
          (
            new ResourceLink("Home", "/"),
            new ResourceLink("Features", "/Features"),
            new ResourceLink("Download", "/Download"),
            new ResourceLink("Screenshots", "/Screenshots"),
            new ResourceLink("Getting started", "/Getting+started"),
            new ResourceLink("Blog & News", "/Blog"),
            new ResourceLink("Contacts", "/Contact"),
            new ResourceLink("License", "/License"),
            new ResourceLink("Developers", "/Developers")
          ));
        
        final String h = "/home/fritz/Business/Tidalwave/Projects/WorkAreas/Tidalwave/tidalwave~other/InfoglueExporter/target/export";
        addComponent(new Embedded("", new FileResource(new File(h + "/content/media/blueBill_Mobile-Banner.png"), getApplication())));
        addComponent(menuView);
        addComponent((Component)content);
      }
  }
