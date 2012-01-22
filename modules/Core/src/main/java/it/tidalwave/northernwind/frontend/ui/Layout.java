/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui;

import it.tidalwave.northernwind.core.model.HttpStatusException;
import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.role.Identifiable;
import it.tidalwave.northernwind.core.model.SiteNode;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface Layout extends As, Identifiable
  {
    @Nonnull
    public Layout withLayout (@Nonnull Layout layout);
    
    @Nonnull
    public Layout withOverride (@Nonnull Layout override);

    /*******************************************************************************************************************
     *
     * @throws  NotFoundException   if no view component is found
     * @throws  HttpStatusException if a component asked to return a specific HTTP status
     * 
     ******************************************************************************************************************/
    @Nonnull
    public Object createView (@Nonnull SiteNode siteNode)
      throws NotFoundException, HttpStatusException;
    
    @Nonnull // TODO: refactor with Composite
    public <Type> Type accept (@Nonnull Visitor<Layout, Type> visitor) 
      throws NotFoundException;
    
    @Nonnull
    public String getTypeUri();
    
    @Nonnull
    public List<Layout> getChildren();
    
    @Nonnull
    public Layout findSubComponentById (@Nonnull Id id)
      throws NotFoundException;
  }
