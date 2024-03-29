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
package it.tidalwave.northernwind.core.model.spi;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.NorthernWindTestSupport;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class AvailabilityEnforcerRequestProcessorTest extends NorthernWindTestSupport
  {
    private AvailabilityEnforcerRequestProcessor underTest;

    private Request request;

    private MockResponseHolder responseHolder;

    private final ZonedDateTime currentTime = Instant.ofEpochMilli(1341242353456L).atZone(ZoneId.of("GMT"));

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        underTest = context.getBean(AvailabilityEnforcerRequestProcessor.class);
        responseHolder = context.getBean(MockResponseHolder.class);
        responseHolder.setClockSupplier(() -> Clock.fixed(currentTime.toInstant(), currentTime.getZone()));
        request = mock(Request.class);

        when(request.getBaseUrl()).thenReturn("http://acme.com");
        when(request.getOriginalRelativeUri()).thenReturn("/contextPath");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_do_nothing_when_site_is_available()
      {
        // given
        when(siteProvider.isSiteAvailable()).thenReturn(true);
        // when
        final var result = underTest.process(request);
        // then
        assertThat(result, is(Status.CONTINUE));
//        verifyNoInteractions(responseHolder); FIXME would require a spy(), but it breaks DI
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_status_503_when_site_is_not_available()
      throws Exception
      {
        // given
        when(siteProvider.isSiteAvailable()).thenReturn(false);
        // when
        final var result = underTest.process(request);
        // then
        assertThat(result, is(Status.BREAK));

        final var tr = helper.testResourceFor("response.txt");
        tr.writeToActualFile(responseHolder.get());
        tr.assertActualFileContentSameAsExpected();
      }
  }
