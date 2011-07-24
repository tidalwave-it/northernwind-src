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
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import lombok.Getter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Getter @ToString(exclude="childrenMapById")
public class DefaultLayout implements Layout
  {
    @Nonnull
    private final String id;
    
    @Nonnull
    private final String typeUri;
    
    private final List<DefaultLayout> children = new ArrayList<DefaultLayout>();
    
    private final Map<String, DefaultLayout> childrenMapById = new HashMap<String, DefaultLayout>();
    
    @Inject @Nonnull
    private ViewFactory viewFactory;

    public DefaultLayout (final @Nonnull String id, final @Nonnull String typeUri)
      {
        this.id = id;
        this.typeUri = typeUri;
      }
      
    public void add (final @Nonnull DefaultLayout layout)
      {
        children.add(layout);
        childrenMapById.put(layout.id, layout);
      }
    
    @Nonnull
    public DefaultLayout findSubComponentById (final @Nonnull String id)
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
        
        for (final DefaultLayout child : children)
          {
            child.accept(visitor);   
          }
        
        visitor.postVisit(this);
        
        return visitor.getValue();
      }
  }
