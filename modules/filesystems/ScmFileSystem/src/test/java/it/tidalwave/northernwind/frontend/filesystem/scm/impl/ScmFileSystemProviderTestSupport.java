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
package it.tidalwave.northernwind.frontend.filesystem.scm.impl;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.role.ContextManager;
import it.tidalwave.role.spi.DefaultContextManagerProvider;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmFileSystemProvider;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmRepository;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.Tag;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.RequiredArgsConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static it.tidalwave.northernwind.frontend.filesystem.scm.impl.ScmPreparer.*;
import static it.tidalwave.northernwind.frontend.filesystem.scm.impl.ResourceFileSystemChangedEventMatcher.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class ScmFileSystemProviderTestSupport
  {
    @Nonnull
    private final Class<? extends ScmFileSystemProvider> underTestClass;

    @Nonnull
    private final ScmPreparer repositoryPreparer;

    private final SpringTestHelper springHelper = new SpringTestHelper(this);

    private ApplicationContext context;

    private ScmFileSystemProvider underTest;

    private MessageBus messageBus;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        ContextManager.Locator.set(new DefaultContextManagerProvider()); // TODO: try to get rid of this
        repositoryPreparer.prepare(TAG_PUBLISHED_0_8);
        final Map<String, Object> properties = new HashMap<>();
        properties.put("test.repositoryUrl", SOURCE_REPOSITORY_FOLDER.toUri().toASCIIString());
        properties.put("test.workAreaFolder", Files.createTempDirectory("workarea").toFile().getAbsolutePath());
        context = springHelper.createSpringContext(properties);
        underTest = context.getBean(underTestClass);
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
        assertThat(underTest.getExposedRepository().getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThat(underTest.getAlternateRepository().getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThatHasNoCurrentTag(underTest.getExposedRepository());
        assertThatHasNoCurrentTag(underTest.getAlternateRepository());
        assertThat(underTest.getSwapCounter(), is(0));
        verifyZeroInteractions(messageBus);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_initialize")
    public void checkForUpdates_must_do_nothing_when_there_are_no_updates()
      throws Exception
      {
        // given
        updateWorkAreaTo(underTest.getCurrentWorkArea(), new Tag("published-0.8"));
        final int previousSwapCounter = underTest.getSwapCounter();
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        assertThat(underTest.getCurrentTag().isPresent(), is(true));
        assertThat(underTest.getCurrentTag().get().getName(), is("published-0.8"));
        assertThat(underTest.getSwapCounter(), is(previousSwapCounter));
        verifyZeroInteractions(messageBus);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dependsOnMethods="must_properly_initialize")
    public void checkForUpdates_must_update_and_fire_event_when_there_are_updates()
      throws Exception
      {
        // given
        updateWorkAreaTo(underTest.getCurrentWorkArea(), new Tag("published-0.8"));
        final int previousSwapCounter = underTest.getSwapCounter();
        repositoryPreparer.prepare(TAG_PUBLISHED_0_9);
        final ZonedDateTime now = ZonedDateTime.now();
//        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        assertThat("Not present: " + underTest.getCurrentTag(), underTest.getCurrentTag().isPresent(), is(true));
        assertThat("Wrong name", underTest.getCurrentTag().get().getName(), is("published-0.9"));
        assertThat("Wrong swap counter", underTest.getSwapCounter(), is(previousSwapCounter + 1));
        verify(messageBus).publish(is(argThat(fileSystemChangedEvent().withResourceFileSystemProvider(underTest))));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void updateWorkAreaTo (final @Nonnull Path workArea, final @Nonnull Tag tag)
      throws Exception
      {
        underTestClass.newInstance().createRepository(workArea).updateTo(tag);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertInvariantPostConditions()
      {
        assertThat(underTest.getExposedRepository().getWorkArea(), is(not(underTest.getAlternateRepository().getWorkArea())));
        assertThat(underTest.getFileSystemDelegate().getRootDirectory().toPath(), is(underTest.getExposedRepository().getWorkArea()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertThatHasNoCurrentTag (final @Nonnull ScmRepository repository)
      throws Exception
      {
        final Optional<Tag> tag = repository.getCurrentTag();
        assertThat("Repository should have not current tag, it has " + tag, tag.isPresent(), is(false));
      }
  }
