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
import javax.annotation.concurrent.NotThreadSafe;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.spi.NodeViewBuilderVisitorSupport;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A visitor for {@link Layout} that builds a Vaadin view.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public class VaadinNodeViewBuilderVisitor extends NodeViewBuilderVisitorSupport<Component, ComponentContainer> 
  {
    public VaadinNodeViewBuilderVisitor (final @Nonnull SiteNode siteNode) 
      {
        super(siteNode);
      }
    
    // TODO: this could be done in a ViewFactory subclass? Or an aspect?
    @Override @Nonnull
    protected Component createPlaceHolderComponent (final @Nonnull Layout layout)
      {
        final Label panel = new Label(); // TODO: id?
        panel.setValue("Missing component: " + ((DefaultLayout)layout).getTypeUri()); // FIXME
        return panel;
      }

    @Override
    protected void attach (final @Nonnull ComponentContainer parent, final @Nonnull Component child)
      {
        parent.addComponent(child);
      }
  }