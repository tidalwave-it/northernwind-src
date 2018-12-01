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
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.role.ContextManager;
import it.tidalwave.role.spi.DefaultContextManagerProvider;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.basic.LocalFileSystemProvider;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import it.tidalwave.northernwind.util.test.SpringTestHelper.TestResource;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class LayeredFileSystemProviderTest
  {
    private static final String FS_BASE = "target/filesystems/";

    private final SpringTestHelper helper = new SpringTestHelper(this);

    private LayeredFileSystemProvider underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        ContextManager.Locator.set(new DefaultContextManagerProvider()); // TODO: try to get rid of this
        underTest = new LayeredFileSystemProvider();
      }

    /*******************************************************************************************************************
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
     ******************************************************************************************************************/
    @Test(dataProvider = "testCases")
    public void must_navigate_through_the_whole_filesystem (final @Nonnull String testCase,
                                                            final @Nonnull String[] fileSystemNames)
      throws IOException
      {
        // given
        final List<ResourceFileSystemProvider> fileSystemProviders = new ArrayList<>();

        for (final String fileSystemName : fileSystemNames)
          {
            // TODO: should mock a ResourceFileSystemProvider instead of using a LocalFileSystemProvider
            //       Otherwise, declare this an integration test.
            final LocalFileSystemProvider fs1 = new LocalFileSystemProvider();
            fs1.setRootPath(FS_BASE + testCase + fileSystemName);
            fileSystemProviders.add(fs1);
          }
        // when
        underTest.setDelegates(fileSystemProviders);
        // then
        final TestResource tr = helper.testResourceFor(testCase + ".txt");
        dump(underTest.getFileSystem(), tr);
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] testCases()
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
     ******************************************************************************************************************/
    private void dump (final @Nonnull ResourceFileSystem fileSystem, final @Nonnull TestResource tr)
      throws IOException
      {
        final List<String> lines = new ArrayList<>();
        dump(fileSystem.getRoot(), lines);
        Collections.sort(lines);
        tr.writeToActualFile(lines);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void dump (final @Nonnull ResourceFile file, final @Nonnull List<String> lines)
      throws IOException
      {
        if (file.isData())
          {
            lines.add(String.format("path: %s; content: %s", file.getPath().asString(), file.asText("UTF-8").replace("\n", "")));
          }
        else
          {
            for (final ResourceFile child : file.findChildren().results())
              {
                dump(child, lines);
              }
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void createFile (final @Nonnull String testCase,
                                    final @Nonnull String fileSystemName,
                                    final @Nonnull String path)
      throws IOException
      {
        final Path file = Paths.get(FS_BASE, testCase + fileSystemName, path);
        Files.createDirectories(file.getParent());
        Files.write(file, Arrays.asList(fileSystemName + ": " + path));
        log.info("Created {} - {}:{}", testCase, fileSystemName, path);
      }
  }
