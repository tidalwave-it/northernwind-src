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
package it.tidalwave.northernwind.frontend.filesystem.scm.impl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public abstract class ScmPreparer
  {
    public static final Path SOURCE_REPOSITORY_FOLDER;

    public final static Tag TAG_PUBLISHED_0_8 = new Tag("published-0.8");

    public final static Tag TAG_PUBLISHED_0_9 = new Tag("published-0.9");

    public static final List<Tag> ALL_TAGS_UP_TO_PUBLISHED_0_8 = new ArrayList<>();

    public static final List<Tag> ALL_TAGS_UP_TO_PUBLISHED_0_9 = new ArrayList<>();

    static
      {
        try
          {
            // Don't create a SCM workarea under target, which is a subdirectory of the project's SCM workarea...
            SOURCE_REPOSITORY_FOLDER = Files.createTempDirectory("scm-repository");
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }

        ALL_TAGS_UP_TO_PUBLISHED_0_8.addAll(createTagNames(8).stream().distinct().map(Tag::new).collect(toList()));
        ALL_TAGS_UP_TO_PUBLISHED_0_9.addAll(createTagNames(9).stream().distinct().map(Tag::new).collect(toList()));
      }

    public static List<String> createTagNames (final int n)
      {
        return IntStream.rangeClosed(1, n).mapToObj(i -> "published-0." + i).collect(toList());
      }

    public void prepare (final @Nonnull Tag tag)
      throws Exception
      {
        log.info("======== Preparing repository at {} with {}", SOURCE_REPOSITORY_FOLDER.toFile().getCanonicalPath(), tag);

        if (SOURCE_REPOSITORY_FOLDER.toFile().exists())
          {
            FileUtils.deleteDirectory(SOURCE_REPOSITORY_FOLDER.toFile());
          }

        assertThat(SOURCE_REPOSITORY_FOLDER.toFile().mkdirs(), is(true));
        doPrepare(tag.getName());
        // Files.walk(SOURCE_REPOSITORY_FOLDER).sorted().forEach(p -> log.debug(">>>> {}", p));
      }

    @Nonnull
    protected abstract void doPrepare (@Nonnull String tag)
      throws IOException, InterruptedException;
  }
