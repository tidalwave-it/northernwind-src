/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.util.test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;

/***********************************************************************************************************************
 *
 * A facility that provides some common tasks for testing, such as creating a Spring context and manipulating files.
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class TestHelper
  {
    /*******************************************************************************************************************
     *
     * A manipulator of a pair of (actual file, expected file).
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor(access = PRIVATE)
    public class TestResource
      {
        @Nonnull
        private final String name;

        @Nonnull
        private final Path actualFile;

        @Nonnull
        private final Path expectedFile;

        /***************************************************************************************************************
         *
         * Assert that the content of the actual file are the same as the expected file.
         *
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        public void assertActualFileContentSameAsExpected()
          throws IOException
          {
            assertSameContents(expectedFile.toFile(), actualFile.toFile());
          }

        /***************************************************************************************************************
         *
         * Writes the given strings to the actual file.
         *
         * @param   strings         the strings
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        public void writeToActualFile (final @Nonnull String ... strings)
          throws IOException
          {
            Files.write(actualFile, Arrays.asList(strings), UTF_8);
          }

        /***************************************************************************************************************
         *
         * Writes the given bytes to the actual file.
         *
         * @param   bytes           the bytes
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        public void writeToActualFile (final @Nonnull byte[] bytes)
          throws IOException
          {
            Files.write(actualFile, bytes);
          }

        /***************************************************************************************************************
         *
         * Reads the content from the resource file as a single string.
         *
         * @return                  the string
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        @Nonnull
        public String readStringFromResource()
          throws IOException
          {
            return TestHelper.this.readStringFromResource(name);
          }
      }

    @NonNull
    private final Object test;

    /*******************************************************************************************************************
     *
     * Creates a Spring context configured with the given files. A further configuration file is appended, named
     * {@code test-class-simple-name/TestBeans.xml}.
     *
     * @param   configurationFiles  the configuration files
     * @return                      the Spring {@link ApplicationContext}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationContext createSpringContext (final @Nonnull String ... configurationFiles)
      {
        return createSpringContext(new ArrayList<>(Arrays.asList(configurationFiles)));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ApplicationContext createSpringContext (final @Nonnull Collection<String> configurationFiles)
      {
        configurationFiles.add(test.getClass().getSimpleName() + "/TestBeans.xml");
        return new ClassPathXmlApplicationContext(configurationFiles.toArray(new String[0]));
      }

    /*******************************************************************************************************************
     *
     * Reads the content from the resource file as a single string. The resource should be placed under
     * {@code src/test/resources/test-class-simple-name/test-resources/resource-name}. Note that the file actually
     * loaded is the one under {@code target/test-classes} copied there (and eventually filtered) by Maven.
     *
     * @param   resourceName    the resource name
     * @return                  the string
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public String readStringFromResource (final @Nonnull String resourceName)
      throws IOException
      {
        final String testName = test.getClass().getSimpleName();
        final Path path = Paths.get("target/test-classes", testName, "test-resources", resourceName);
        final StringBuilder buffer = new StringBuilder();
        String separator = "";

        for (final String string : Files.readAllLines(path, UTF_8))
          {
            buffer.append(separator).append(string);
            separator = "\n";
          }

        return buffer.toString();
//            return String.join("\n", Files.readAllLines(path, UTF_8)); TODO JDK 8
      }

    /*******************************************************************************************************************
     *
     * Create a {@link TestResource} for the given name. The actual file will be created under
     * {@code target/test-artifacts/test-class-simple-name/resourceName}. The expected file should be
     * placed in {@code src/test/resources/test-class-simple-name/expected-resoults/resource-name}. Note that the file
     * actually loaded is the one under {@code target/test-classes} copied there (and eventually filtered) by Maven.
     *
     * @param   resourceName    the name
     * @return                  the {@code TestResource}
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public TestResource testResourceFor (final @Nonnull String resourceName)
      throws IOException
      {
        final String testName = test.getClass().getSimpleName();
        final Path expectedFile = Paths.get("target/test-classes", testName, "expected-results", resourceName);
        final Path actualFile = Paths.get("target/test-artifacts", testName, resourceName);
        Files.createDirectories(actualFile.getParent());
        return new TestResource(resourceName, actualFile, expectedFile);
      }
  }