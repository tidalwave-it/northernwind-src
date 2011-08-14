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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.role.spring.SpringAsSupport;
import java.util.Stack;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Getter
public class DefaultLayout extends SpringAsSupport implements Layout
  {
    @Nonnull
    private final Id id;
    
    @Nonnull
    private /* final FIXME */ String typeUri;
    
    private final List<Layout> children = new ArrayList<Layout>();
    
    private final Map<Id, Layout> childrenMapById = new HashMap<Id, Layout>();
    
    static class CloneVisitor implements Visitor<Layout, DefaultLayout>
      {
        private DefaultLayout rootLayout;
        
        private Stack<DefaultLayout> layouts = new Stack<DefaultLayout>();

        @Override
        public void preVisit (final @Nonnull Layout layout) 
          {
            final DefaultLayout newLayout = new DefaultLayout(((DefaultLayout)layout).id, ((DefaultLayout)layout).typeUri);

            if (rootLayout == null)
              {
                rootLayout = newLayout;  
              }
            else
              {
                layouts.peek().add(newLayout);
              }

            layouts.push(newLayout);
          }

        @Override
        public void visit (final @Nonnull Layout layout) 
          {
          }

        @Override
        public void postVisit (final @Nonnull Layout layout) 
          {
            layouts.pop();
          }

        @Override @Nonnull
        public DefaultLayout getValue() 
          {
            return rootLayout;
          }    
      };
    
    @Inject @Nonnull
    private ViewFactory viewFactory;

    public DefaultLayout()
      {
        this.id = new Id("");
        this.typeUri = "";
      }
    
    public DefaultLayout (final @Nonnull Id id, final @Nonnull String typeUri)
      {
        this.id = id;
        this.typeUri = typeUri;
      }
    
    @Override @Nonnull
    public DefaultLayout clone()
      {
        try 
          {
            return accept(new CloneVisitor());
          }
        catch (NotFoundException e) 
          {
            throw new RuntimeException(e);
          }
      }  
    
    @Override @Nonnull
    public Layout withOverride (final @Nonnull Layout override)
      { 
        final DefaultLayout result = clone();
        result.applyOverride(((DefaultLayout)override).clone());
        return result;
      }
    
    // Here everything is already cloned
    private void applyOverride (final @Nonnull Layout override)
      {
        final boolean sameType = this.getTypeUri().equals(override.getTypeUri());
        this.typeUri = override.getTypeUri(); // FIXME: don't like this approach, as it requires typeUri non final

        // Complex rule, but it's Infoglue. 
        if (sameType)
          {
            for (final Layout overridingChild : override.getChildren())
              {
                final Layout overriddenChild = childrenMapById.get(overridingChild.getId());

                if (overriddenChild == null)
                  {
                    add(overridingChild);  
                  }
                else
                  {
                    childrenMapById.put(overridingChild.getId(), overridingChild);
                    final int i = children.indexOf(overriddenChild);
                    
                    if (i < 0)
                      {
                        throw new IllegalArgumentException();  
                      }
                    
                    children.set(i, overridingChild);
                    //                    ((DefaultLayout)overriddenChild).applyOverride(overridingChild);                    
                  }
              }
          }
        else
          {
            this.children.clear();
            this.childrenMapById.clear();

            for (final Layout overridingChild : override.getChildren())
              {
                add(overridingChild);  
              }
          }
      }
      
    @Override @Nonnull
    public Layout withLayout (final @Nonnull Layout layout)
      {
        final DefaultLayout clone = clone();
        clone.children.add(layout);
        clone.childrenMapById.put(layout.getId(), layout);
        
        return clone;
      }
    
    public void add (final @Nonnull Layout layout) // FIXME: drop this
      {
        children.add(layout); // FIXME: clone
        childrenMapById.put(layout.getId(), layout);// FIXME: clone
      }
    
    @Override @Nonnull
    public Layout findSubComponentById (final @Nonnull Id id)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(childrenMapById.get(id), "Can't find " + id);
      }

    @Override @Nonnull
    public Object createView (final @Nonnull SiteNode siteNode) 
      throws NotFoundException
      {
        return viewFactory.createView(typeUri, id, siteNode);
      }
    
    @Override @Nonnull // TODO: refactor with Composite
    public <Type> Type accept (final @Nonnull Visitor<Layout, Type> visitor) 
      throws NotFoundException
      {
        visitor.preVisit(this);    
        visitor.visit(this);    
        
        for (final Layout child : children)
          {
            child.accept(visitor);   
          }
        
        visitor.postVisit(this);
        
        return visitor.getValue();
      }
    
    @Override @Nonnull
    public String toString()
      {
        return String.format("DefaultLayout(id=%s, typeUri=%s, children=%d)", id, typeUri, children.size());  
      }
  }
