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
import java.util.Stack;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A visitor for {@link Layout} that builds a Vaadin view.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @RequiredArgsConstructor @Slf4j
public class VaadinNodeViewBuilderVisitor implements Visitor<Layout, Component>
  {
    @Nonnull
    private final SiteNode siteNode;
    
    private Component rootComponent;
    
    private Stack<Component> components = new Stack<Component>();

    @Override
    public void preVisit (final @Nonnull Layout layout) 
      {
        final Component component = createComponent(layout);

        if (rootComponent == null)
          {
            rootComponent = component;  
          }
        else
          {
            ((ComponentContainer)components.peek()).addComponent(component);  
          }

        components.push(component);
      }
    
    @Override
    public void visit (final @Nonnull Layout layout) 
      {
      }

    @Override
    public void postVisit (final @Nonnull Layout layout) 
      {
        components.pop();
      }

    @Override @Nonnull
    public Component getValue() 
      {
        return rootComponent;
      }
    
    @Nonnull
    protected Component createComponent (final @Nonnull Layout layout)
      {
        try
          {
            return (Component)layout.createView(siteNode); 
          }
        catch (NotFoundException e) // FIXME; somewhere there's a Visitor whose methods can throw checked exceptions
          {
            final Panel panel = new Panel(); // TODO: id?
            panel.setCaption("Missing component: ");
            return panel;
          }
      }
  }
