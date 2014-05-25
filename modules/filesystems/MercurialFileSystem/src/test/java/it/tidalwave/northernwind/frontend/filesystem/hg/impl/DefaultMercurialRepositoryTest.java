/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import it.tidalwave.util.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static it.tidalwave.northernwind.frontend.filesystem.hg.impl.TestRepositoryHelper.*;
import java.io.File;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMercurialRepositoryTest
  {
    private MercurialRepository fixture;

    private Path workArea;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeClass
    public void createSourceRepository()
      throws Exception
      {
        // FIXME: on Mac OS X cloning inside the project workarea makes a strage 'merged' workarea together with
        // the project sources
//        workArea = new File("target/workarea").toPath();
        workArea = Files.createTempDirectory("hg-workarea");
        workArea.toFile().delete();
        workArea = Files.createTempDirectory("hg-workarea");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        FileUtils.deleteDirectory(sourceRepository.toFile());
        FileUtils.deleteDirectory(workArea.toFile());
        fixture = new DefaultMercurialRepository(workArea);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_clone_a_repository()
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);

        fixture.clone(sourceRepository.toUri());

        assertThat(new File(workArea.toFile(), ".hg").exists(), is(true));
        assertThat(new File(workArea.toFile(), ".hg").isDirectory(), is(true));
        // TODO: assert contents in .hg
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_clone_a_repository")
    public void must_properly_enumerate_tags()
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);

        fixture.clone(sourceRepository.toUri());

        assertThat(fixture.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThat(fixture.getLatestTagMatching(".*").getName(), is("tip"));
        assertThat(fixture.getLatestTagMatching("p.*").getName(), is("published-0.8"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_clone_a_repository",
          expectedExceptions=NotFoundException.class)
    public void must_throw_NotFoundException_when_asking_for_the_current_tag_in_an_empty_workarea()
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);

        fixture.clone(sourceRepository.toUri());

        fixture.getCurrentTag();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_clone_a_repository",
          dataProvider="tagSequenceUpTo0.8")
    public void must_properly_update_to_a_tag (final @Nonnull Tag tag)
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);

        fixture.clone(sourceRepository.toUri());
        fixture.updateTo(tag);

        assertThat(fixture.getCurrentTag(), is(tag));
        // TODO: assert contents
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_clone_a_repository",
          dataProvider="invalidTags",
          expectedExceptions=IOException.class,
          expectedExceptionsMessageRegExp="Process exited with 255")
    public void must_throw_exception_when_try_to_update_to_an_invalid_tag (final @Nonnull Tag tag)
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);

        fixture.clone(sourceRepository.toUri());

        fixture.updateTo(tag);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_clone_a_repository")
    public void must_properly_pull_changesets()
      throws Exception
      {
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);
        fixture.clone(sourceRepository.toUri());

        fixture.pull();

        assertThat(fixture.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThat(fixture.getLatestTagMatching(".*").getName(), is("tip"));
        assertThat(fixture.getLatestTagMatching("p.*").getName(), is("published-0.8"));

        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_9);

        fixture.pull();

        assertThat(fixture.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_9));
        assertThat(fixture.getLatestTagMatching(".*").getName(), is("tip"));
        assertThat(fixture.getLatestTagMatching("p.*").getName(), is("published-0.9"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name="tagSequenceUpTo0.8")
    public Object[][] tagSequenceUpTo0_8()
      {
        final List<Object[]> validTags = new ArrayList<>();

        for (final Tag tag : ALL_TAGS_UP_TO_PUBLISHED_0_8)
          {
            validTags.add(new Object[] { tag });
          }

        return validTags.toArray(new Object[0][0]);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name="invalidTags")
    public Object[][] getInvalidTags()
      {
        return new Object[][]
          {
            { new Tag("tag1") },
            { new Tag("tag2") },
            { new Tag("tag3") },
            { new Tag("tag4") }
          };
      }
  }
