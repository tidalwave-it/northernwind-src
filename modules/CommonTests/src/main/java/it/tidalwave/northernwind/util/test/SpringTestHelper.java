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
package it.tidalwave.northernwind.util.test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;

/***********************************************************************************************************************
 *
 * A facility that provides some common tasks for testing, such as creating a Spring context and manipulating files.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class SpringTestHelper
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
        public void writeToActualFile (@Nonnull final String ... strings)
          throws IOException
          {
            writeToActualFile(List.of(strings));
          }

        /***************************************************************************************************************
         *
         * Writes the given strings to the actual file.
         *
         * @param   strings         the strings
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        public void writeToActualFile (@Nonnull final Iterable<String> strings)
          throws IOException
          {
            Files.write(actualFile, strings, UTF_8);
          }

        /***************************************************************************************************************
         *
         * Writes the given bytes to the actual file.
         *
         * @param   bytes           the bytes
         * @throws  IOException     in case of error
         *
         **************************************************************************************************************/
        public void writeToActualFile (@Nonnull final byte[] bytes)
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
            return SpringTestHelper.this.readStringFromResource(name);
          }
      }

    @Nonnull
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
    public ApplicationContext createSpringContext (@Nonnull final String ... configurationFiles)
      {
        return createSpringContext(Collections.emptyMap(), configurationFiles);
      }

    /*******************************************************************************************************************
     *
     * Creates a Spring context configured with the given files. A further configuration file is appended, named
     * {@code test-class-simple-name/TestBeans.xml}.
     *
     * @param   properties          the properties
     * @param   configurationFiles  the configuration files
     * @return                      the Spring {@link ApplicationContext}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationContext createSpringContext (@Nonnull final Map<String, Object> properties,
                                                   @Nonnull final String ... configurationFiles)
      {
        return createSpringContext(properties, context -> {}, new ArrayList<>(List.of(configurationFiles)));
      }

    /*******************************************************************************************************************
     *
     * Creates a Spring context configured with the given files. A further configuration file is appended, named
     * {@code test-class-simple-name/TestBeans.xml}.
     *
     * @param   configurationFiles  the configuration files
     * @param   modifier            a processor to modify the contents of the context
     * @return                      the Spring {@link ApplicationContext}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationContext createSpringContext (@Nonnull final Consumer<GenericApplicationContext> modifier,
                                                   @Nonnull final String ... configurationFiles)
      {
        return createSpringContext(Collections.emptyMap(), modifier, configurationFiles);
      }

    /*******************************************************************************************************************
     *
     * Creates a Spring context configured with the given files. A further configuration file is appended, named
     * {@code test-class-simple-name/TestBeans.xml}.
     *
     * @param   properties          the properties
     * @param   modifier            a processor to modify the contents of the context
     * @param   configurationFiles  the configuration files
     * @return                      the Spring {@link ApplicationContext}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ApplicationContext createSpringContext (@Nonnull final Map<String, Object> properties,
                                                   @Nonnull final Consumer<GenericApplicationContext> modifier,
                                                   @Nonnull final String ... configurationFiles)
      {
        return createSpringContext(properties, modifier, new ArrayList<>(List.of(configurationFiles)));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ApplicationContext createSpringContext (@Nonnull final Map<String, Object> properties,
                                                    @Nonnull final Consumer<GenericApplicationContext> modifier,
                                                    @Nonnull final Collection<String> configurationFiles)
      {
        configurationFiles.add(test.getClass().getSimpleName() + "/TestBeans.xml");

        final StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setEnvironment(environment);
        context.load(configurationFiles.toArray(new String[0]));
        modifier.accept(context);
        context.refresh();
        log.info("Beans: {}", List.of(context.getBeanFactory().getBeanDefinitionNames()));

        return context;
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Path} for a resource file. The resource should be placed under
     * {@code src/test/resources/test-class-simple-name/test-resources/resource-name}. Note that the file actually
     * loaded is the one under {@code target/test-classes} copied there (and eventually filtered) by Maven.
     *
     * @param   resourceName    the resource name
     * @return                  the {@code Path}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Path resourceFileFor (@Nonnull final String resourceName)
      {
        final String testName = test.getClass().getSimpleName();
        return Paths.get("target/test-classes", testName, "test-resources", resourceName);
      }

    /*******************************************************************************************************************
     *
     * Reads the content from the resource file as a single string. See {@link #resourceFileFor(java.lang.String)} for
     * further info.
     *
     * @param   resourceName    the resource name
     * @return                  the string
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public String readStringFromResource (@Nonnull final String resourceName)
      throws IOException
      {
        final Path file = resourceFileFor(resourceName);
        final StringBuilder buffer = new StringBuilder();
        String separator = "";

        for (final String string : Files.readAllLines(file, UTF_8))
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
     * placed in {@code src/test/resources/test-class-simple-name/expected-results/resource-name}. Note that the file
     * actually loaded is the one under {@code target/test-classes} copied there (and eventually filtered) by Maven.
     * The {@code test-class-simple-name} is tried first with the current test, and then with its eventual
     * super-classes; this allows to extend existing test suites. Note that if the resource files for a super class are
     * not in the current project module, they should be explicitly copied here (for instance, by means of the
     * Maven dependency plugin).
     *
     * @param   resourceName    the name
     * @return                  the {@code TestResource}
     * @throws  IOException     in case of error
     *
     ******************************************************************************************************************/
    @Nonnull
    public TestResource testResourceFor (@Nonnull final String resourceName)
      throws IOException
      {
        final String testName = test.getClass().getSimpleName();
        final Path expectedFile = findExpectedFilePath(resourceName);
        final Path actualFile = Paths.get("target/test-artifacts", testName, resourceName);
        Files.createDirectories(actualFile.getParent());
        return new TestResource(resourceName, actualFile, expectedFile);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Path findExpectedFilePath (@Nonnull final String resourceName)
      throws IOException
      {
        for (Class<?> testClass = test.getClass(); testClass != null; testClass = testClass.getSuperclass())
          {
            final Path expectedFile = Paths.get("target/test-classes", testClass.getSimpleName(), "expected-results", resourceName);

            if (Files.exists(expectedFile))
              {
                return expectedFile;
              }
          }

        throw new FileNotFoundException("Expected file for test " + resourceName);
      }
  }
