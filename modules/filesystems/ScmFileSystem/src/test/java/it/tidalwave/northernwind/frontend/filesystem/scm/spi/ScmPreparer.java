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
import java.util.stream.IntStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * An utility class that allows to manipulate a test working directory. Concrete implementations should subclass it by
 * implementing {@link #stripChangesetsAfter(String)}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class ScmPreparer
  {
    /** The folder where the test repository is stored. */
    public static final Path REPOSITORY_FOLDER;

    public static final Tag TAG_PUBLISHED_0_8 = new Tag("published-0.8");

    public static final Tag TAG_PUBLISHED_0_9 = new Tag("published-0.9");

    public static final List<Tag> ALL_TAGS_UP_TO_PUBLISHED_0_8 =
            createTagNames(8).stream().map(Tag::new).collect(toList());

    public static final List<Tag> ALL_TAGS_UP_TO_PUBLISHED_0_9 =
            createTagNames(9).stream().map(Tag::new).collect(toList());

    static
      {
        try
          {
            // Don't create a SCM repository under target, which is a subdirectory of the project's SCM workarea...
            REPOSITORY_FOLDER = Files.createTempDirectory("scm-repository");
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     * Returns a sequence of strings {@code [{@published-0.1} .. {@published-0.n}]}.
     *
     * @param n the upper limit
     * @return the sequence of strings
     *
     ******************************************************************************************************************/
    @Nonnull
    public static List<String> createTagNames (final int n)
      {
        return IntStream.rangeClosed(1, n).mapToObj(i -> "published-0." + i).collect(toList());
      }

    /*******************************************************************************************************************
     *
     * Prepares the repository removing all changesets after the given tag.
     *
     * @param tag the tag
     * @throws Exception  in case of error
     *
     ******************************************************************************************************************/
    public void prepareAtTag (@Nonnull final Tag tag)
            throws Exception
      {
        log.info("======== Preparing repository at {} with {}", REPOSITORY_FOLDER.toFile().getCanonicalPath(), tag);

        if (Files.exists(REPOSITORY_FOLDER))
          {
            FileUtils.deleteDirectory(REPOSITORY_FOLDER.toFile());
          }

        assertThat(REPOSITORY_FOLDER.toFile().mkdirs(), is(true));
        stripChangesetsAfter(tag.getName());
        // Files.walk(SOURCE_REPOSITORY_FOLDER).sorted().forEach(p -> log.debug(">>>> {}", p));
      }

    /*******************************************************************************************************************
     *
     * Strips changesets after the given tags.
     *
     * @param tag the tag
     * @throws IOException in case of error
     * @throws InterruptedException in case of error
     *
     ******************************************************************************************************************/
    protected abstract void stripChangesetsAfter (@Nonnull String tag)
            throws IOException, InterruptedException;
  }
