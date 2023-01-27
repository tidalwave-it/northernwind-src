/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * A support class for implementing {@link ScmWorkingDirectory}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class ScmWorkingDirectorySupport implements ScmWorkingDirectory
  {
    /** The name of the configuration folder - e.g. .git or .hg. */
    @Getter @Nonnull
    private final String configFolderName;

    /** The folder where this working directory is stored. */
    @Getter @Nonnull
    protected final Path folder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean isEmpty()
      {
        return !folder.toFile().exists() || (folder.toFile().list().length == 0);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Tag> getTags()
            throws InterruptedException, IOException
      {
        return listTags().stream().map(Tag::new).collect(toList());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Optional<Tag> getLatestTagMatching (@Nonnull final String regexp)
            throws InterruptedException, IOException
      {
        final var tags = getTags();
        Collections.reverse(tags);
        return tags.stream().filter(tag -> tag.getName().matches(regexp)).findFirst();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public abstract List<String> listTags()
            throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * Returns a reversed list.
     *
     * @param list the list
     * @param <T> the list generic type
     * @return the reversed list
     *
     ******************************************************************************************************************/
    @Nonnull
    protected static <T> List<T> reversed (@Nonnull final List<T> list)
      {
        Collections.reverse(list);
        return list;
      }
  }
