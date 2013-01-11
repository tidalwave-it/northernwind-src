/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.hg;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.DefaultMercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.MercurialRepository;
import it.tidalwave.northernwind.frontend.filesystem.hg.impl.Tag;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static it.tidalwave.northernwind.frontend.filesystem.hg.impl.TestRepositoryHelper.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MercurialFileSystemProviderTest
  {
    private MercurialFileSystemProvider fixture;

    private GenericXmlApplicationContext context;

    private MessageBus messageBus;

    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        prepareSourceRepository(Option.STRIP);
        final Map<String, Object> properties = new HashMap<>();
        properties.put("test.repositoryUrl", sourceRepository.toUri().toASCIIString());
        properties.put("test.workAreaFolder", "target/workarea");
        final StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        context = new GenericXmlApplicationContext();
        context.setEnvironment(environment);
        context.load("/MercurialFileSystemTestBeans.xml");
        context.refresh();
        fixture = context.getBean(MercurialFileSystemProvider.class);
        messageBus = context.getBean(MessageBus.class);
      }

    @Test
    public void must_properly_initialize()
      throws Exception
      {
//        assertThat(fixture.getCurrentTag().getName(), is("published-0.8"));
        assertThat(fixture.swapCounter, is(0));
        verifyZeroInteractions(messageBus);
      }

    @Test(dependsOnMethods="must_properly_initialize")
    public void checkForUpdates_must_do_nothing_when_there_are_no_updates()
      throws Exception
      {
        // Simulate a previous update
        final MercurialRepository mercurialRepository = new DefaultMercurialRepository(fixture.getCurrentWorkArea());
        mercurialRepository.pull();
        mercurialRepository.updateTo(new Tag("published-0.8"));
        fixture.swapCounter = 0;

        fixture.checkForUpdates();

        assertThat(fixture.getCurrentTag().getName(), is("published-0.8"));
        assertThat(fixture.swapCounter, is(0));
        verifyZeroInteractions(messageBus);
      }

    @Test(dependsOnMethods="must_properly_initialize")
    public void checkForUpdates_must_update_and_fire_event_when_there_are_updates()
      throws Exception
      {
        fixture.swapCounter = 0;
        prepareSourceRepository(Option.DONT_STRIP);

        fixture.checkForUpdates();

        assertThat(fixture.getCurrentTag().getName(), is("published-0.9"));
        assertThat(fixture.swapCounter, is(1));

        verify(messageBus).publish(is(argThat(new BaseMatcher<ResourceFileSystemChangedEvent>()
          {
            @Override
            public boolean matches (final @Nonnull Object item)
              {
                if (! (item instanceof ResourceFileSystemChangedEvent))
                  {
                    return false;
                  }

                return true;
              }

            @Override
            public void describeTo(Description description)
              {

              }
          })));
      }
  }
