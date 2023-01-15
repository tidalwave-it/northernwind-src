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
package it.tidalwave.northernwind.frontend.filesystem.git.impl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.util.ProcessExecutor;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmPreparer;

/***********************************************************************************************************************
 *
 * A Git implementation of the {@link ScmPreparer}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class GitPreparer extends ScmPreparer
  {
    @Override
    protected void stripChangesetsAfter (@Nonnull final String tag)
            throws IOException, InterruptedException
      {
        final Path sourceBundle = Paths.get("src/test/resources/GitFileSystemProviderTest/git.bundle");
        gitCommand().withArguments("clone", sourceBundle.toFile().getCanonicalPath(), ".")
                    .start()
                    .waitForCompletion();

        switch (tag)
          {
            case "published-0.8":
              // FIXME: this doesn't actually delete changesets, but it's enough for our testing.
              gitCommand().withArguments("tag", "-d", "published-0.9").start().waitForCompletion();
              break;

            case "published-0.9":
              break;

            default:
              throw new IllegalArgumentException("Only published-0.8 or published-0.9 allowed");
          }

      }

    @Nonnull
    private static ProcessExecutor gitCommand()
            throws IOException
      {
        return ProcessExecutor.forExecutable("git").withWorkingDirectory(ScmPreparer.REPOSITORY_FOLDER);
      }
  }
