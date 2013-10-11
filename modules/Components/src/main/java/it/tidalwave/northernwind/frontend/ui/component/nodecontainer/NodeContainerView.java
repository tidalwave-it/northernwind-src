/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 * 
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import javax.annotation.Nonnull;
import it.tidalwave.role.Identifiable;

/***********************************************************************************************************************
 *
 * The container of a "screen" of the application, mapped to a SiteNode. This, for instance, is a page with a
 * HTML rendering technology.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface NodeContainerView extends Identifiable
  {
    public void addAttribute (@Nonnull String string, @Nonnull String property);

    public void setTemplate (@Nonnull String template);
  }
