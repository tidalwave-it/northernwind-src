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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class TestRepositoryHelper
  {
    public enum Option
      { 
        VOID_OPTION
          {
            @Override
            public void apply()
              {
              }
          },
        
        STRIP_TO_PUBLISHED_0_9
          {
            @Override
            public void apply()
              throws Exception
              {
                Executor.forExecutable("hg")
                        .withArgument("strip")
                        .withArgument("published-0.9")
                        .withWorkingDirectory(sourceRepository)
                        .start()
                        .waitForCompletion();
              }
          };
        
        public abstract void apply()
          throws Exception;
      }

    public static final List<Tag> EXPECTED_TAGS_1 = new ArrayList<>();

    public static final List<Tag> EXPECTED_TAGS_2 = new ArrayList<>();

    public static final Path sourceRepository;

    private static final Path sourceBundle;

    static
      {
        try 
          {
            // FIXME: on Mac OS X cloning inside the project workarea makes a strage 'merged' workarea together with
            // the project sources
            // sourceRepository = new File("target/source-repository").toPath();
            sourceRepository = Files.createTempDirectory("hg-source-repository");
          } 
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
        
        sourceBundle = new File("./src/test/resources/hg.bundle").toPath();

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

    public static void prepareSourceRepository (final @Nonnull Option option)
      throws Exception
      {
        log.info("======== Preparing source repository at {} with {}", 
                sourceRepository.toFile().getCanonicalPath(), option);

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

        option.apply();

        log.info("======== Source repository prepared ========");
      }
  }
