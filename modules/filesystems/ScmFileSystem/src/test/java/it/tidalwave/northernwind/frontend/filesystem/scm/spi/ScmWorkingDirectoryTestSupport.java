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
package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;
import static it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmPreparer.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class ScmWorkingDirectoryTestSupport
  {
    @Nonnull
    private final Class<? extends ScmWorkingDirectory> classUnderTest;

    @Nonnull
    private final ScmPreparer scmPreparer;

    private ScmWorkingDirectory underTest;

    private Path workingDirectory;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeClass
    public void createSourceRepository()
            throws Exception
      {
        // Don't create a working directory under target, which is a subdirectory of the project source repository.
        workingDirectory = Files.createTempDirectory("scm-working-directory");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
            throws Exception
      {
        // Unfortunately java.nio doesn't provide recursive deletion
        FileUtils.deleteDirectory(REPOSITORY_FOLDER.toFile());
        FileUtils.deleteDirectory(workingDirectory.toFile());
        underTest = classUnderTest.getConstructor(Path.class).newInstance(workingDirectory);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_clone_a_repository()
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_8);
        // when
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // then
        final Path configFolder = workingDirectory.resolve(underTest.getConfigFolderName());
        assertThat("Doesn't exist: " + configFolder, Files.exists(configFolder), is(true));
        assertThat("Not a directory: " + configFolder, Files.isDirectory(configFolder), is(true));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_clone_a_repository", dataProvider = "changesets")
    public void must_properly_enumerate_tags (@Nonnull final String tagName, final List<Tag> expectedTags)
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(new Tag(tagName));
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // when
        final List<Tag> tags = underTest.getTags();
        final Optional<Tag> latestTag = underTest.getLatestTagMatching(".*");
        final Optional<Tag> latestTagMatchingP = underTest.getLatestTagMatching("p.*");
        // then
        assertThat(tags, is(expectedTags));
        assertThat(latestTag.isPresent(), is(true));
        assertThat(latestTagMatchingP.isPresent(), is(true));
        assertThat(latestTagMatchingP.get().getName(), is(tagName));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_clone_a_repository")
    public void must_return_no_tag_when_empty_working_directory()
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_8);
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // when
        assertThat(underTest.getCurrentTag().isPresent(), is(false));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_clone_a_repository", dataProvider = "tagSequenceUpTo0.8")
    public void must_properly_checkout (@Nonnull final Tag tag)
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_8);
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // when
        underTest.checkOut(tag);
        // then
        final Optional<Tag> currentTag = underTest.getCurrentTag();
        assertThat(currentTag.isPresent(), is(true));
        assertThat(currentTag.get(), is(tag));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_clone_a_repository",
          dataProvider = "invalidTags",
          expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Invalid tag: .*")
    public void must_throw_exception_when_try_to_update_to_an_invalid_tag (@Nonnull final Tag tag)
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_8);
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // when
        underTest.checkOut(tag);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_clone_a_repository", dataProvider = "changesets")
    public void must_properly_fetch_changesets (@Nonnull final String tagName, final List<Tag> expectedTags)
            throws Exception
      {
        // given
        scmPreparer.prepareAtTag(new Tag(tagName));
        underTest.cloneFrom(REPOSITORY_FOLDER.toUri());
        // when
        underTest.fetchChangesets();
        // then
        assertThat(underTest.getTags(), is(expectedTags));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "changesets")
    public Object[][] changesets()
      {
        return new Object[][]
          {
            { "published-0.8", ALL_TAGS_UP_TO_PUBLISHED_0_8},
            { "published-0.9", ALL_TAGS_UP_TO_PUBLISHED_0_9}
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "tagSequenceUpTo0.8") @Nonnull
    public Object[][] tagSequenceUpTo0_8()
      {
        return ALL_TAGS_UP_TO_PUBLISHED_0_8.stream().map(t -> new Object[]{t}).collect(toList()).toArray(new Object[0][0]);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider @Nonnull
    public Object[][] invalidTags()
      {
        return new Object[][]
          {
            {new Tag("tag1")},
            {new Tag("tag2")},
            {new Tag("tag3")},
            {new Tag("tag4")}
          };
      }
  }
