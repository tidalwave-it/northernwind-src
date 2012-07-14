/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultMercurialRepositoryTest
  {
    enum Option { STRIP, DONT_STRIP }
    
    private static final List<Tag> EXPECTED_TAGS_1 = new ArrayList<>();
    
    private static final List<Tag> EXPECTED_TAGS_2 = new ArrayList<>();
    
    private MercurialRepository fixture;
    
    private Path sourceBundle;
    
    private Path sourceRepository;
    
    private Path workArea;

    static
      {
        EXPECTED_TAGS_1.add(new Tag("published-0.1"));
        EXPECTED_TAGS_1.add(new Tag("published-0.2"));
        EXPECTED_TAGS_1.add(new Tag("published-0.3"));
        EXPECTED_TAGS_1.add(new Tag("published-0.4"));
        EXPECTED_TAGS_1.add(new Tag("published-0.5"));
        EXPECTED_TAGS_1.add(new Tag("published-0.6"));
        EXPECTED_TAGS_1.add(new Tag("published-0.7"));
        EXPECTED_TAGS_1.add(new Tag("published-0.8"));
        EXPECTED_TAGS_1.add(new Tag("tip"));  
        
        EXPECTED_TAGS_2.add(new Tag("published-0.1"));
        EXPECTED_TAGS_2.add(new Tag("published-0.2"));
        EXPECTED_TAGS_2.add(new Tag("published-0.3"));
        EXPECTED_TAGS_2.add(new Tag("published-0.4"));
        EXPECTED_TAGS_2.add(new Tag("published-0.5"));
        EXPECTED_TAGS_2.add(new Tag("published-0.6"));
        EXPECTED_TAGS_2.add(new Tag("published-0.7"));
        EXPECTED_TAGS_2.add(new Tag("published-0.8"));
        EXPECTED_TAGS_2.add(new Tag("published-0.9"));
        EXPECTED_TAGS_2.add(new Tag("tip"));       
      }
    
    @BeforeClass
    public void createSourceRepository()
      throws Exception
      {
        sourceBundle = new File("./src/test/resources/hg.bundle").toPath();
        sourceRepository = new File("target/source-repository").toPath();
        workArea = new File("target/workarea").toPath();        
      }
    
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        FileUtils.deleteDirectory(sourceRepository.toFile());
        FileUtils.deleteDirectory(workArea.toFile());
        fixture = new DefaultMercurialRepository(workArea);
      }
    
    @Test
    public void must_properly_clone_a_repository() 
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        // TODO: assert contents
      }
    
    @Test(dependsOnMethods="must_properly_clone_a_repository")
    public void must_properly_enumerate_tags()
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        
        assertThat(fixture.getTags(), is(EXPECTED_TAGS_1));
      }
    
    @Test(dependsOnMethods="must_properly_clone_a_repository")
    public void must_properly_return_the_latest_tag()
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        
        assertThat(fixture.getLatestTag(), is(new Tag("published-0.8")));
      }
    
    @Test(dependsOnMethods="must_properly_clone_a_repository",
          dataProvider="validTags1")
    public void must_properly_update_to_a_tag (final @Nonnull Tag tag)
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        
        fixture.updateTo(tag);
        assertThat(fixture.getId(), is(tag));
        // TODO: assert contents
      }
    
    @Test(dependsOnMethods="must_properly_clone_a_repository",
          dataProvider="invalidTags", 
          expectedExceptions=IOException.class, 
          expectedExceptionsMessageRegExp="Process exited with 255")
    public void must_throw_exception_when_try_to_update_to_an_invalid_tag (final @Nonnull Tag tag)
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        
        fixture.updateTo(tag);
      }
    
    @Test(dependsOnMethods="must_properly_clone_a_repository")
    public void must_properly_pull_changesets()
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        fixture.clone(sourceRepository.toUri());
        prepareSourceRepository(Option.DONT_STRIP);
        
        fixture.pull();
        
        assertThat(fixture.getLatestTag(), is(new Tag("published-0.9")));
        assertThat(fixture.getTags(), is(EXPECTED_TAGS_2));        
      }
    
    @DataProvider(name="validTags1")
    public Object[][] getValidTags1()
      {
        final List<Object[]> validTags = new ArrayList<>();
        
        for (final Tag tag : EXPECTED_TAGS_1)
          {
            validTags.add(new Object[] { tag });
          }
        
        return validTags.toArray(new Object[0][0]);
      }    
    
    @DataProvider(name="validTags2")
    public Object[][] getValidTags2()
      {
        final List<Object[]> validTags = new ArrayList<>();
        
        for (final Tag tag : EXPECTED_TAGS_2)
          {
            validTags.add(new Object[] { tag });
          }
        
        return validTags.toArray(new Object[0][0]);
      }    
    
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
    
    private void prepareSourceRepository (final @Nonnull Option option)
      throws Exception
      {
        log.info("======== Preparing source repository at {}", sourceRepository.toFile().getCanonicalPath());
        
        if (sourceRepository.toFile().exists())
          {
            FileUtils.deleteDirectory(sourceRepository.toFile());  
          }
        
        assertThat(sourceRepository.toFile().mkdirs(), is(true));
        
        Executor.forExecutable("hg")
                .withArgument("clone")
                .withArgument("--noupdate")
                .withArgument(sourceBundle.toFile().getCanonicalPath())
                .withArgument(".")
                .withWorkingDirectory(sourceRepository)
                .start()
                .waitForCompletion();
        
        if (option == Option.STRIP)
          {
            Executor.forExecutable("hg")
                    .withArgument("strip")
                    .withArgument("published-0.9")
                    .withWorkingDirectory(sourceRepository)
                    .start()
                    .waitForCompletion();
          }
        
        log.info("======== Source repository prepared ========");
      }
  }
