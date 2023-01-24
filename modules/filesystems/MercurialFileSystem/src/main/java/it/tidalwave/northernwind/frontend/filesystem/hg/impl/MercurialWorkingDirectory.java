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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import it.tidalwave.util.ProcessExecutor;
import it.tidalwave.util.ProcessExecutorException;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmWorkingDirectorySupport;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.Tag;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * A Mercurial implementation of {@link it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmWorkingDirectory}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class MercurialWorkingDirectory extends ScmWorkingDirectorySupport
  {
    private static final String HG = "hg";

    /*******************************************************************************************************************
     *
     * Creates a new instance for the given folder.
     *
     * @param folder the folder
     *
     ******************************************************************************************************************/
    public MercurialWorkingDirectory (@Nonnull final Path folder)
      {
        super(".hg", folder);
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
            final var executor = hgCommand().withArgument("id").start().waitForCompletion();
            final var scanner = executor.getStdout().waitForCompleted().filteredAndSplitBy("(.*)", " ");
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
    @Nonnull
    public List<String> listTags()
            throws InterruptedException, IOException
      {
        return hgCommand().withArgument("tags")
                          .start()
                          .waitForCompletion()
                          .getStdout()
                          .waitForCompleted()
                          .filteredBy("([^ ]*) *.*$")
                          .stream()
                          .filter(s -> !"tip".equals(s))
                          .collect(collectingAndThen(toList(), ScmWorkingDirectorySupport::reversed));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void cloneFrom (@Nonnull final URI uri)
            throws InterruptedException, IOException
      {
        Files.createDirectories(folder);
        hgCommand().withArguments("clone", "--noupdate", uri.toASCIIString(), ".").start().waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void checkOut (@Nonnull final Tag tag)
            throws InterruptedException, IOException
      {
        try
          {
            hgCommand().withArguments("update", "-C", tag.getName()).start().waitForCompletion();
          }
        catch (ProcessExecutorException e)
          {
            if ((e.getExitCode() == 255) &&
                (e.getStderr().stream().anyMatch(s -> s.contains("abort: unknown revision"))))
              {
                throw new IllegalArgumentException("Invalid tag: " + tag.getName());
              }

            throw e;
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void fetchChangesets()
            throws InterruptedException, IOException
      {
        hgCommand().withArgument("pull").start().waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     * Creates an executor for Mercurial.
     *
     * @return the executor
     * @throws IOException in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    private ProcessExecutor hgCommand()
            throws IOException
      {
        return ProcessExecutor.forExecutable(HG).withWorkingDirectory(folder);
      }
  }
