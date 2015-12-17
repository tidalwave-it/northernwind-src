/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.List;
import java.io.IOException;
import java.net.URI;
import it.tidalwave.util.NotFoundException;
import java.nio.file.Path;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MercurialRepository
  {
    public boolean isEmpty();

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

    /*******************************************************************************************************************
     *
     * Returns the current tag of the workarea.
     *
     ******************************************************************************************************************/
    @Nonnull
    public Tag getCurrentTag()
      throws IOException, NotFoundException;

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
     * Returns the latest tag matching the given regular expression.
     *
     * @param   regexp              the regular expression
     * @return                      the <code>Tag</code>
     * @throws  NotFoundException   if no tag is found
     * @throws  IOException         in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Tag getLatestTagMatching (@Nonnull String regexp)
      throws IOException, NotFoundException;
    
    /*******************************************************************************************************************
     *
     * Updates the repository to the given tag.
     *
     ******************************************************************************************************************/
    public void updateTo (@Nonnull Tag tag)
      throws IOException;

    @Nonnull
    public Path getWorkArea();
  }
