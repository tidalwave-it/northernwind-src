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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.List;
import java.io.IOException;
import java.net.URI;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MercurialRepository 
  {
    /*******************************************************************************************************************
     *
     * Clones a mercurial repo.
     *
     ******************************************************************************************************************/
    public void clone (@Nonnull URI uri)
      throws IOException;
    
    /*******************************************************************************************************************
     *
     * Pulls changes from the remote repository.
     *
     ******************************************************************************************************************/
    public void pull()
      throws IOException;
    
    @Nonnull
    public Tag getId() // FIXME: perhaps this is useless
      throws IOException;
    
    /*******************************************************************************************************************
     *
     * Returns the tags of the repository.
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Tag> getTags()
      throws IOException;
    
    /*******************************************************************************************************************
     *
     * Returns the latest tag of the repository, if exists.
     *
     ******************************************************************************************************************/
    @Nonnull
    public Tag getLatestTag()
      throws IOException, NotFoundException;
    
    /*******************************************************************************************************************
     *
     * Updates the repository to the given tag.
     *
     ******************************************************************************************************************/
    public void updateTo (@Nonnull Tag tag)
      throws IOException;
  }
