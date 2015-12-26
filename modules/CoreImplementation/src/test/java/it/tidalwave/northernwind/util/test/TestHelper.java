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
import it.tidalwave.util.test.FileComparisonUtils;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class TestHelper
  {
    @RequiredArgsConstructor(access = PRIVATE)
    public class TestResource
      {
        @Nonnull
        private final String name;

        @Nonnull
        private final Path actualFile;

        @Nonnull
        private final Path expectedFile;

        public void assertActualFileContentSameAsExpected()
          throws IOException
          {
            FileComparisonUtils.assertSameContents(expectedFile.toFile(), actualFile.toFile());
          }

        public void writeToActualFile (final @Nonnull String ... strings)
          throws IOException
          {
            Files.write(actualFile, Arrays.asList(strings), UTF_8);
          }

        @Nonnull
        public String readStringFromResource()
          throws IOException
          {
            return TestHelper.this.readStringFromResource(name);
          }
      }

    @NonNull
    private final Object test;

    @Nonnull
    public ApplicationContext createSpringContext (final @Nonnull String ... configurationFiles)
      {
        return createSpringContext(new ArrayList<>(Arrays.asList(configurationFiles)));
      }

    @Nonnull
    private ApplicationContext createSpringContext (final @Nonnull Collection<String> configurationFiles)
      {
        configurationFiles.add(test.getClass().getSimpleName() + "/TestBeans.xml");
        return new ClassPathXmlApplicationContext(configurationFiles.toArray(new String[0]));
      }

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

    @Nonnull
    public TestResource testResourceFor (final @Nonnull String name)
      throws IOException
      {
        final String testName = test.getClass().getSimpleName();
        final Path expectedFile = Paths.get("target/test-classes", testName, "expected-results", name);
        final Path actualFile = Paths.get("target", testName, "test-artifacts", name);
        Files.createDirectories(actualFile.getParent());
        return new TestResource(name, actualFile, expectedFile);
      }
  }
