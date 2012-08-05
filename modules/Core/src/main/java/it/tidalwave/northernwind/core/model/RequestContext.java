/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.spi.RequestResettable;

/***********************************************************************************************************************
 *
 * The context for a {@link Filter} provides access to the current {@link Resource} and {@link SiteNode} being 
 * processed.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface RequestContext extends RequestResettable // FIXME: rename to RequestContext?
  {
    /*******************************************************************************************************************
     *
     * Sets the current {@link Content}.
     * 
     * @param  content  the current {@code Content}
     *
     ******************************************************************************************************************/
    public void setContent (@Nonnull Content content);
    
    /*******************************************************************************************************************
     *
     * Returns the current {@link Content} being processed.
     * 
     * @return  the current {@code Content}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Content getContent(); // FIXME: throws NotFoundException
    
    /*******************************************************************************************************************
     *
     * Clears the current {@link Resource}.
     *
     ******************************************************************************************************************/
    public void clearContent();
    
    /*******************************************************************************************************************
     *
     * Sets the current {@link SiteNode}.
     * 
     * @param  resource  the current {@code SiteNode}
     *
     ******************************************************************************************************************/
    public void setNode (@Nonnull SiteNode node);
    
    /*******************************************************************************************************************
     *
     * Returns the current {@link SiteNode} being processed.
     * 
     * @return  the current {@code SiteNode}
     *
     ******************************************************************************************************************/
    @Nonnull
    public SiteNode getNode();// FIXME: throws NotFoundException
    
    /*******************************************************************************************************************
     *
     * Clears the current {@link SiteNode}.
     *
     ******************************************************************************************************************/
    public void clearNode();
  }
