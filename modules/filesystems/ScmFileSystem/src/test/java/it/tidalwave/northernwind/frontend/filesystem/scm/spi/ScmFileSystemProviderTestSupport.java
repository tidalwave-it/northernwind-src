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
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import it.tidalwave.messagebus.MessageBus;
import lombok.RequiredArgsConstructor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.util.test.SpringTestHelper;
import static it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmPreparer.*;
import static it.tidalwave.northernwind.frontend.filesystem.scm.spi.ResourceFileSystemChangedEventMatcher.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * A support class for writing tests of implementations of {@link ScmFileSystemProvider}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@SuppressWarnings("NewClassNamingConvention") @RequiredArgsConstructor
public class ScmFileSystemProviderTestSupport
  {
    @Nonnull
    private final Class<? extends ScmFileSystemProvider> classUnderTest;

    @Nonnull
    private final ScmPreparer scmPreparer;

    private final SpringTestHelper springHelper = new SpringTestHelper(this);

    private ScmFileSystemProvider underTest;

    private MessageBus messageBus;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
            throws Exception
      {
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_8);
        final Map<String, Object> properties = new HashMap<>();
        properties.put("test.repositoryUrl", REPOSITORY_FOLDER.toUri().toString());
        properties.put("test.workAreaFolder", Files.createTempDirectory("working-dir").toFile().getAbsolutePath());
        final var context = springHelper.createSpringContext(properties);
        underTest = context.getBean(classUnderTest);
        messageBus = context.getBean(MessageBus.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_initialize()
            throws Exception
      {
        // given the initialization
        // then
        assertInvariantPostConditions();
        assertThat(underTest.exposedWorkingDirectory.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThat(underTest.alternateWorkingDirectory.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThatHasNoCurrentTag(underTest.exposedWorkingDirectory);
        assertThatHasNoCurrentTag(underTest.alternateWorkingDirectory);
        assertThat(underTest.swapCounter, is(0));
        verifyZeroInteractions(messageBus);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_initialize")
    public void checkForUpdates_must_do_nothing_when_there_are_no_updates()
            throws Exception
      {
        // given
        populateWorkingDirectory(underTest.exposedWorkingDirectory.getFolder(), new Tag("published-0.8"));
        final var previousSwapCounter = underTest.swapCounter;
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        final var currentTag = underTest.exposedWorkingDirectory.getCurrentTag();
        assertThat("Tag not present", currentTag.isPresent(), is(true));
        assertThat("Wrong tag: ", currentTag.get().getName(), is("published-0.8"));
        assertThat("Wrong swap counter", underTest.swapCounter, is(previousSwapCounter));
        verifyZeroInteractions(messageBus);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods = "must_properly_initialize")
    public void checkForUpdates_must_update_and_fire_event_when_there_are_updates()
            throws Exception
      {
        // given
        populateWorkingDirectory(underTest.exposedWorkingDirectory.getFolder(), new Tag("published-0.8"));
        final var previousSwapCounter = underTest.swapCounter;
        scmPreparer.prepareAtTag(TAG_PUBLISHED_0_9);
        //final ZonedDateTime now = ZonedDateTime.now();
        //DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        final var currentTag = underTest.exposedWorkingDirectory.getCurrentTag();
        assertThat("Tag not present", currentTag.isPresent(), is(true));
        assertThat("Wrong tag: ", currentTag.get().getName(), is("published-0.9"));
        assertThat("Wrong swap counter", underTest.swapCounter, is(previousSwapCounter + 1));
        verify(messageBus).publish(is(argThat(fileSystemChangedEvent().withResourceFileSystemProvider(underTest))));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void populateWorkingDirectory (@Nonnull final Path folder, @Nonnull final Tag tag)
            throws Exception
      {
        classUnderTest.getConstructor().newInstance().createWorkingDirectory(folder).checkOut(tag);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertInvariantPostConditions()
      {
        assertThat(underTest.exposedWorkingDirectory.getFolder(),
                   is(not(underTest.alternateWorkingDirectory.getFolder())));
        assertThat(underTest.fileSystemDelegate.getRootDirectory().toPath(),
                   is(underTest.exposedWorkingDirectory.getFolder()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void assertThatHasNoCurrentTag (@Nonnull final ScmWorkingDirectory workingDirectory)
            throws Exception
      {
        final var tag = workingDirectory.getCurrentTag();
        assertThat("Repository should have not current tag, it has " + tag, tag.isPresent(), is(false));
      }
  }
