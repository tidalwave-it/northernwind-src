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
package it.tidalwave.northernwind.frontend.filesystem.git.impl;

import javax.annotation.Nonnull;
import java.util.List;
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

/***********************************************************************************************************************
 *
 * A Git implementation of {@link it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmWorkingDirectory}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class GitWorkingDirectory extends ScmWorkingDirectorySupport
  {
    private static final String GIT = "git";

    /*******************************************************************************************************************
     *
     * Creates a new instance for the given folder.
     *
     * @param folder the folder
     *
     ******************************************************************************************************************/
    public GitWorkingDirectory (@Nonnull final Path folder)
      {
        super(".git", folder);
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
            final ProcessExecutor executor =
                    gitCommand().withArguments("describe", "--tags", "--candidates=0").start().waitForCompletion();
            return executor.getStdout().waitForCompleted().getContent().stream().findFirst().map(Tag::new);
          }
        catch (ProcessExecutorException e)
          {
            if ((e.getExitCode() == 128) &&
                e.getStderr().stream().anyMatch(s -> s.contains("fatal: no tag exactly matches ")))
              {
                return Optional.empty();
              }

            throw e;
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
        return gitCommand().withArguments("tag", "-l", "--sort=v:refname")
                           .start()
                           .waitForCompletion()
                           .getStdout()
                           .waitForCompleted()
                           .filteredBy("([^ ]*) *.*$");
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
        gitCommand().withArguments("clone", "--no-checkout", uri.toASCIIString(), ".").start().waitForCompletion();
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
            gitCommand().withArguments("checkout", tag.getName()).start().waitForCompletion();
          }
        catch (ProcessExecutorException e)
          {
            if ((e.getExitCode() == 1) &&
                (e.getStderr().stream().anyMatch(s -> s.contains("did not match any file(s) known to git"))))
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
        gitCommand().withArguments("fetch", "--all").start().waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     * Creates an executor for git.
     *
     * @return the executor
     * @throws IOException in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    private ProcessExecutor gitCommand()
            throws IOException
      {
        return ProcessExecutor.forExecutable(GIT).withWorkingDirectory(folder);
      }
  }
