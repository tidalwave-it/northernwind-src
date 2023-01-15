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
package it.tidalwave.northernwind.util.test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class FileTestHelper
  {
    private final Path base;

    private final Path actualResults;

    private final Path expectedResults;

    public FileTestHelper (@Nonnull final String name)
      {
        base = Paths.get("src/test/resources/" + name);
        actualResults = Paths.get("target/test-results/" + name);
        expectedResults = base.resolve("expected-results");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public void assertFileContents (@Nonnull final byte[] content, @Nonnull final String fileName)
      throws IOException
      {
        final Path actualPath = actualResults.resolve(fileName);
        final Path expectedPath = expectedResults.resolve(fileName);
        Files.createDirectories(actualResults);
        Files.write(actualPath, content);
        assertSameContents(expectedPath.toFile(), actualPath.toFile());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public Path resolve (@Nonnull final String segment)
      {
        return base.resolve(segment);
      }
  }
