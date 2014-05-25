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
package it.tidalwave.northernwind.core.model.spi;

import com.google.common.io.Files;
import it.tidalwave.northernwind.core.impl.filter.MacroFilterTestSupport;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor.Status;
import it.tidalwave.util.test.FileComparisonUtils;
import java.io.File;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.joda.time.DateTime;
import static org.mockito.Mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class AvailabilityEnforcerRequestProcessorTest extends MacroFilterTestSupport
  {
    private AvailabilityEnforcerRequestProcessor fixture;

    private Request request;

    private MockResponseHolder responseHolder;

    private final DateTime currentTime = new DateTime(1341242353456L);

    public AvailabilityEnforcerRequestProcessorTest()
      {
        super("AvailabilityEnforcerRequestProcessorTestBeans.xml");
      }
   
    @BeforeMethod
    public void setupFixture()
      {
        MockResponseHolder.setCurrentTime(currentTime);
        fixture = context.getBean(AvailabilityEnforcerRequestProcessor.class);
        responseHolder = context.getBean(MockResponseHolder.class);
        request = mock(Request.class);

        when(request.getBaseUrl()).thenReturn("http://acme.com");
        when(request.getOriginalRelativeUri()).thenReturn("/contextPath");
      }

    @Test
    public void must_do_nothing_when_site_is_available()
      throws Exception
      {
        when(siteProvider.isSiteAvailable()).thenReturn(true);

        final Status result = fixture.process(request);

        assertThat(result, is(Status.CONTINUE));
//        verifyZeroInteractions(responseHolder); FIXME
      }

    @Test
    public void must_return_status_503_when_site_is_not_available()
      throws Exception
      {
        when(siteProvider.isSiteAvailable()).thenReturn(false);

        final Status result = fixture.process(request);
        final File actualFile = new File("target/test-artifacts/response.txt");
        final File expectedFile = new File("src/test/resources/expected-results/response.txt");
        actualFile.getParentFile().mkdirs();
        Files.write(responseHolder.get(), actualFile);

        assertThat(result, is(Status.BREAK));
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }
  }
