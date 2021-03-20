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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmPreparer;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ProcessExecutor;

/***********************************************************************************************************************
 *
 * A Mercurial implementation of the {@link ScmPreparer}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class MercurialPreparer extends ScmPreparer
  {
    @Override
    protected void stripChangesetsAfter (@Nonnull final String tag)
            throws IOException, InterruptedException
      {
        final Path sourceBundle = Paths.get("src/test/resources/MercurialFileSystemProviderTest/hg.bundle");
        hgCommand().withArguments("clone", "--noupdate", sourceBundle.toFile().getCanonicalPath(), ".")
                   .start()
                   .waitForCompletion();

        switch (tag)
          {
            case "published-0.8":
              hgCommand().withArguments("strip", "published-0.9").start().waitForCompletion();
              break;

            case "published-0.9":
              break;

            default:
              throw new IllegalArgumentException("Only published-0.8 or published-0.9 allowed");
          }
      }

    @Nonnull
    private static ProcessExecutor hgCommand()
            throws IOException
      {
        return ProcessExecutor.forExecutable("hg").withWorkingDirectory(ScmPreparer.REPOSITORY_FOLDER);
      }
  }

