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
import java.util.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class ScmRepositorySupport implements ScmRepository
  {
    @Getter @Nonnull
    private final String configFolderName;

    @Getter @Nonnull
    protected final Path workArea;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public boolean isEmpty()
      {
        return !workArea.toFile().exists() || (workArea.toFile().list().length == 0);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void clone (final @Nonnull URI uri)
      throws InterruptedException, IOException
      {
        workArea.toFile().mkdirs();

        if (!workArea.toFile().exists())
          {
            throw new IOException("Cannot mkdirs " + workArea);
          }

        cloneRepository(uri);
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
    public Optional<Tag> getLatestTagMatching (final @Nonnull String regexp)
      throws InterruptedException, IOException
      {
        final List<Tag> tags = getTags();
        Collections.reverse(tags);
        return tags.stream().filter(tag -> tag.getName().matches(regexp)).findFirst();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public abstract List<String> listTags()
      throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public abstract void cloneRepository (@Nonnull URI uri)
      throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Override
    public abstract void updateTo (@Nonnull Tag tag)
      throws InterruptedException, IOException;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public abstract void pull()
      throws InterruptedException, IOException;
  }
