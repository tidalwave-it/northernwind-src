/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.frontend.filesystem.basic.LocalFileSystemProvider;
import it.tidalwave.util.test.FileComparisonUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LayeredFileSystemProviderTest
  {
    private static final String FS_BASE = "target/filesystems/";

    private LayeredFileSystemProvider fixture;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void createFixture()
      {
        fixture = new LayeredFileSystemProvider();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void createFiles()
      throws IOException
      {
        // 2nd filesystem overrides a single file
        createFile("TC1", "fs1", "/dir1/dir2/file1.txt");
        createFile("TC1", "fs1", "/dir1/dir2/file2.txt");
        createFile("TC1", "fs1", "/dir1/dir2/file3.txt");
        createFile("TC1", "fs1", "/dir1/dir2/file4.txt");
        createFile("TC1", "fs2", "/dir1/dir2/file3.txt");

        // 2nd filesystem adds a new file in existing dir
        createFile("TC2", "fs1", "/dir1/dir2/file1.txt");
        createFile("TC2", "fs1", "/dir1/dir2/file2.txt");
        createFile("TC2", "fs1", "/dir1/dir2/file3.txt");
        createFile("TC2", "fs1", "/dir1/dir2/file4.txt");
        createFile("TC2", "fs2", "/dir1/dir2/file5.txt");

        // 2nd filesystem adds a new file in a new dir
        createFile("TC3", "fs1", "/dir1/dir2/file1.txt");
        createFile("TC3", "fs1", "/dir1/dir2/file2.txt");
        createFile("TC3", "fs1", "/dir1/dir2/file3.txt");
        createFile("TC3", "fs1", "/dir1/dir2/file4.txt");
        createFile("TC3", "fs2", "/dir1/dir3/file5.txt");

        // 2nd filesystem adds a new file in a new dir just under root
        createFile("TC4", "fs1", "/dir1/dir2/file1.txt");
        createFile("TC4", "fs1", "/dir1/dir2/file2.txt");
        createFile("TC4", "fs1", "/dir1/dir2/file3.txt");
        createFile("TC4", "fs1", "/dir1/dir2/file4.txt");
        createFile("TC4", "fs2", "/dir4/dir3/file5.txt");
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "testCases")
    public void must_navigate_through_the_whole_filesystem (final @Nonnull String testCase,
                                                            final @Nonnull String[] fileSystemNames)
      throws IOException
      {
        final List<LocalFileSystemProvider> fileSystemProviders = new ArrayList<>();

        for (final String fileSystemName : fileSystemNames)
          {
            LocalFileSystemProvider fs1 = new LocalFileSystemProvider();
            fs1.setRootPath(FS_BASE + testCase + fileSystemName);
            fileSystemProviders.add(fs1);
          }

        fixture.setDelegates(fileSystemProviders);

        final File expectedFile = new File(String.format("src/test/resources/expected-results/%s.txt", testCase));
        final File actualFile = new File(String.format("target/test-artifacts/%s.txt", testCase));
        actualFile.getParentFile().mkdirs();
        dump(actualFile, fixture.getFileSystem());

        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @DataProvider(name = "testCases")
    public Object[][] getTestCases()
      {
        return new Object[][]
          {
            { "TC1", new String[] { "fs1", "fs2" } },
            { "TC2", new String[] { "fs1", "fs2" } },
            { "TC3", new String[] { "fs1", "fs2" } },
            { "TC4", new String[] { "fs1", "fs2" } }
          };
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void dump (final @Nonnull File file, final @Nonnull ResourceFileSystem fileSystem)
      throws IOException
      {
        final List<String> lines = new ArrayList<String>();
        dump(lines, fixture.getFileSystem().getRoot());
        Collections.sort(lines);

        FileUtils.writeLines(file, lines, "\n");
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private static void dump (final @Nonnull List<String> lines, final @Nonnull ResourceFile fileObject)
      throws IOException
      {
        if (fileObject.isData())
          {
            lines.add(String.format("%s: %s", fileObject.getPath().asString(), fileObject.asText("UTF-8")));
          }
        else
          {
            for (final ResourceFile child : fileObject.findChildren().results())
              {
                dump(lines, child);
              }
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private static void createFile (final @Nonnull String testCase,
                                    final @Nonnull String fileSystemName,
                                    final @Nonnull String path)
      throws IOException
      {
        final File file = new File(FS_BASE + testCase + fileSystemName + path);
        file.getParentFile().mkdirs();
        FileUtils.write(file, fileSystemName + ": " + path);
        log.info("Created {} - {}:{}", testCase, fileSystemName, path);
      }
  }
