/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;

/***********************************************************************************************************************
 *
 * This interface defines the operations required for accessing a working directory of a repository.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface ScmWorkingDirectory
  {
    /*******************************************************************************************************************
     *
     * Clones the contents from a given repository.
     *
     * @param  url                     the URL of the source repo
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    public void cloneFrom (@Nonnull URI url)
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Fetches changesets from the remote repository.
     *
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    public void fetchChangesets()
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Returns the current tag of this repository, if present.
     *
     * @return the current tag
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<Tag> getCurrentTag()
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Checks out the given tag.
     *
     * @param  tag                     the tag
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    public void checkOut (@Nonnull Tag tag)
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Returns all the tags in this repository.
     *
     * @return the tags
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    @Nonnull
    public List<Tag> getTags()
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Returns the latest tag in this repository matching the given regular expression, if present.
     *
     * @param  regexp                  the regular expression
     * @return the tag
     * @throws InterruptedException    if the operation has been interrupted
     * @throws IOException             if something fails
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<Tag> getLatestTagMatching (@Nonnull String regexp)
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Returns the path of the working area.
     *
     * @return the path to the working area
     *
     ******************************************************************************************************************/
    @Nonnull
    public Path getFolder();

    /*******************************************************************************************************************
     *
     * Returns whether the repository is empty.
     *
     * @return whether the repository is empty
     *
     ******************************************************************************************************************/
    public boolean isEmpty();

    /*******************************************************************************************************************
     *
     * Returns the name of the configuration folder (e.g. {@code .git} or {@code .hg}.
     *
     * @return the name of the configuration folder
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getConfigFolderName();
  }
