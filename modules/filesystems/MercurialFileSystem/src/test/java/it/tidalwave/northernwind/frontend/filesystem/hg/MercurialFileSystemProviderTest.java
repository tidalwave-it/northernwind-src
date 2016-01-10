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
package it.tidalwave.northernwind.frontend.filesystem.hg;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.time.ZonedDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.ContextManager;
import it.tidalwave.role.spi.DefaultContextManagerProvider;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.DefaultMercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.MercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.Tag;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.TestHelper;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static it.tidalwave.northernwind.frontend.filesystem.hg.impl.TestRepositoryHelper.*;
import static it.tidalwave.northernwind.frontend.filesystem.hg.ResourceFileSystemChangedEventMatcher.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MercurialFileSystemProviderTest
  {
    private final TestHelper helper = new TestHelper(this);

    private ApplicationContext context;

    private MercurialFileSystemProvider underTest;

    private MessageBus messageBus;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        ContextManager.Locator.set(new DefaultContextManagerProvider()); // TODO: try to get rid of this
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_8);
        final Map<String, Object> properties = new HashMap<>();
        properties.put("test.repositoryUrl", sourceRepository.toUri().toASCIIString());
        properties.put("test.workAreaFolder", Files.createTempDirectory("workarea").toFile().getAbsolutePath());
        context = helper.createSpringContext(properties);
        underTest = context.getBean(MercurialFileSystemProvider.class);
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
        assertThat(underTest.exposedRepository.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThat(underTest.alternateRepository.getTags(), is(ALL_TAGS_UP_TO_PUBLISHED_0_8));
        assertThatHasNoCurrentTag(underTest.exposedRepository);
        assertThatHasNoCurrentTag(underTest.alternateRepository);
        assertThat(underTest.swapCounter, is(0));
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
        final int previousSwapCounter = underTest.swapCounter;
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        assertThat(underTest.getCurrentTag().getName(), is("published-0.8"));
        assertThat(underTest.swapCounter, is(previousSwapCounter));
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
        final int previousSwapCounter = underTest.swapCounter;
        prepareSourceRepository(Option.UPDATE_TO_PUBLISHED_0_9);
        final ZonedDateTime now = ZonedDateTime.now();
//        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        // when
        underTest.checkForUpdates();
        // then
        assertInvariantPostConditions();
        assertThat(underTest.getCurrentTag().getName(), is("published-0.9"));
        assertThat(underTest.swapCounter, is(previousSwapCounter + 1));
        verify(messageBus).publish(is(argThat(fileSystemChangedEvent().withResourceFileSystemProvider(underTest))));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected static void updateWorkAreaTo (final @Nonnull Path workArea, final @Nonnull Tag tag)
      throws IOException
      {
        new DefaultMercurialRepository(workArea).updateTo(tag);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertInvariantPostConditions()
      {
        assertThat(underTest.exposedRepository.getWorkArea(), is(not(underTest.alternateRepository.getWorkArea())));
        assertThat(underTest.fileSystemDelegate.getRootDirectory().toPath(), is(underTest.exposedRepository.getWorkArea()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertThatHasNoCurrentTag (final @Nonnull MercurialRepository repository)
      throws IOException
      {
        try
          {
            final Tag tag = repository.getCurrentTag();
            fail("Repository should have not current tag, it has " + tag);
          }
        catch (NotFoundException e)
          {
            // ok
          }
      }
  }
