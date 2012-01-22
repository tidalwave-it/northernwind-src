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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;

/***********************************************************************************************************************
 *
 * The model for the whole site, it contains a collection of {@link Content}s, {@link Media} items and 
 * {@link SiteNode}s.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface Site
  {
    /*******************************************************************************************************************
     *
     * Returns the context path for this web site.
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getContextPath();

    /*******************************************************************************************************************
     *
     * Creates a link.
     * 
     * @return   the link
     *
     ******************************************************************************************************************/
    @Nonnull
    public String createLink (@Nonnull String relativeUri);
    
    /*******************************************************************************************************************
     *
     * Reloads the whole configuration, e.g. to get and update.
     * 
     ******************************************************************************************************************/
    public void reset()
      throws IOException, NotFoundException;
    
    /*******************************************************************************************************************
     *
     * Finds something.
     * 
     ******************************************************************************************************************/
    @Nonnull
    public <Type> SiteFinder<Type> find (@Nonnull Class<Type> type);
    
    /*******************************************************************************************************************
     *
     * Returns the {@link FileSystemProvider} used by this {@code Site}.
     * 
     * @return  the {@code FileSystemProvider}
     * 
     ******************************************************************************************************************/
    @Nonnull
    public FileSystemProvider getFileSystemProvider();
    
    /*******************************************************************************************************************
     *
     * Returns the {@link Locale}s configured for this site.
     * 
     * @return   the {@code Locale}s.
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Locale> getConfiguredLocales();

    /*******************************************************************************************************************
     *
     * Returns the version string of NorthernWind.
     * 
     * @return   the version
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getVersionString();
  }
