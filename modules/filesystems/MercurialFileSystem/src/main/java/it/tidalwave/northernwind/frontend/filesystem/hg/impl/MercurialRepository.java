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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ProcessExecutor;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmRepositorySupport;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.Tag;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.reverse;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class MercurialRepository extends ScmRepositorySupport
  {
    private static final String HG = "hg";

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public MercurialRepository (final @Nonnull Path workArea)
      {
        super(".hg", workArea);
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
            final ProcessExecutor executor = hgCommand().withArgument("id").start().waitForCompletion();
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
                .collect(collectingAndThen(toList(), l -> { reverse(l); return l; }));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void cloneRepository (final @Nonnull URI uri)
      throws InterruptedException, IOException
      {
        hgCommand().withArguments("clone", "--noupdate", uri.toASCIIString(), ".").start().waitForCompletion();
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
        hgCommand().withArguments("update", "-C", tag.getName()).start().waitForCompletion();
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
        hgCommand().withArgument("pull").start().waitForCompletion();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
      private ProcessExecutor hgCommand () throws IOException
      {
        return ProcessExecutor.forExecutable(HG).withWorkingDirectory(workArea);
      }
  }
