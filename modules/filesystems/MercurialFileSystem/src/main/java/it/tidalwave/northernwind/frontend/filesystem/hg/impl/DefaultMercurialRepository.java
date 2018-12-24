/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
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
public class DefaultMercurialRepository implements MercurialRepository
  {
    private static final String HG = "hg";

    @Getter @Nonnull
    private final Path workArea;

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

        ProcessExecutor.forExecutable(HG).withArgument("clone")
                                         .withArgument("--noupdate")
                                         .withArgument(uri.toASCIIString())
                                         .withArgument(".")
                                         .withWorkingDirectory(workArea)
                                         .start()
                                         .waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Optional<Tag> getCurrentTag()
      throws InterruptedException, IOException
      {
        try
          {
            final ProcessExecutor executor = ProcessExecutor.forExecutable(HG).withArgument("id")
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
            final Scanner scanner = executor.getStdout().waitForCompleted().filteredAndSplitBy("(.*)", " ");
            scanner.next();
            return Optional.of(new Tag(scanner.next()));
          }
        catch (NoSuchElementException e)
          {
            return Optional.empty();
          }
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
        final ProcessExecutor executor = ProcessExecutor.forExecutable(HG).withArgument("tags")
                                                        .withWorkingDirectory(workArea)
                                                        .start()
                                                        .waitForCompletion();
        final List<String> filteredBy = executor.getStdout().waitForCompleted().filteredBy("([^ ]*) *.*$");
        Collections.reverse(filteredBy);
        return toTagList(filteredBy);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void updateTo (final @Nonnull Tag tag)
      throws InterruptedException, IOException
      {
        ProcessExecutor.forExecutable(HG).withArgument("update")
                                         .withArgument("-C")
                                         .withArgument(tag.getName())
                                         .withWorkingDirectory(workArea)
                                         .start()
                                         .waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void pull()
      throws InterruptedException, IOException
      {
        ProcessExecutor.forExecutable(HG).withArgument("pull")
                                         .withWorkingDirectory(workArea)
                                         .start()
                                         .waitForCompletion();
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
    private static List<Tag> toTagList (final @Nonnull List<String> strings)
      {
        return strings.stream().map(Tag::new).collect(toList());
      }
  }
