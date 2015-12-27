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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourceFile;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.TestHelper;
import it.tidalwave.northernwind.util.test.TestHelper.TestResource;
import static org.mockito.Mockito.*;
import static it.tidalwave.northernwind.core.model.spi.ResponseBuilderSupport.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResponseBuilderTest
  {
    private final TestHelper helper = new TestHelper(this);

    private ResponseHolder<?> underTest;

    private ResourceFile resourceFile;

    private Request request;

    private Map<String, String> headers;

    private final DateTime currentTime = new DateTime(1341242353456L);

    private final DateTime resourceLatestModifiedTime = new DateTime(1341242553456L);

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        MockResponseBuilder.setCurrentTime(currentTime);

        resourceFile = mock(ResourceFile.class);
        when(resourceFile.asBytes()).thenReturn("FILE CONTENT".getBytes());
        when(resourceFile.getMimeType()).thenReturn("text/plain");
        when(resourceFile.getLatestModificationTime()).thenReturn(resourceLatestModifiedTime);

        headers = new HashMap<>();
        request = mock(Request.class);
        when(request.getHeader(anyString())).thenAnswer(new Answer<String>()
          {
            @Override @Nonnull
            public String answer (final @Nonnull InvocationOnMock invocation)
              throws NotFoundException
              {
                final String name = (String)invocation.getArguments()[0];
                return NotFoundException.throwWhenNull(headers.get(name), "Not found " + name);
              }
          });

        underTest = new MockResponseHolder();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_ResourceFile()
      throws Exception
      {
        // when
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile);
        // then
        assertContents(builder, "ResourceFileOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_ResourceFile_with_ExpirationTime()
      throws Exception
      {
        // when
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                               .withExpirationTime(Duration.standardDays(7));
        // then
        assertContents(builder, "ResourceFileOutputWithExpirationTime.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_a_ResourceFile_when_Etag_not_Matching()
      throws Exception
      {
        // given
        headers.put(HEADER_IF_NONE_MATCH, "\"xxxx\"");
        // when
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                               .forRequest(request);
        // then
        assertContents(builder, "ResourceFileOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_NotModified_when_Etag_Matching()
      throws Exception
      {
        // given
        headers.put(HEADER_IF_NONE_MATCH, "\"1341242553456\"");
        // when
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                               .forRequest(request);
        // then
        assertContents(builder, "ResourceFileNotModifiedOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_a_ResourceFile_when_IfModifiedSince_less_recent_than_ModifiedTime()
      throws Exception
      {
        for (int deltaSeconds = -10; deltaSeconds < 0; deltaSeconds++)
          {
            // given
            final DateTime ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toString(ifModifiedSinceTime));
            // when
            final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                                   .forRequest(request);
            // then
            assertContents(builder, "ResourceFileOutput.txt");
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_NotModified_when_IfModifiedSince_more_recent_than_or_equal_to_ModifiedTime()
      throws Exception
      {
        // corner case: same time should return NotModified
        for (int deltaSeconds = 0; deltaSeconds < 10; deltaSeconds++)
          {
            // given
            final DateTime ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toString(ifModifiedSinceTime));
            // when
            final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                                   .forRequest(request);
            // then
            assertContents(builder, "ResourceFileNotModifiedOutput.txt");
          }
      }

    // FIXME: refactor the two tests above into a single parameterized one: (delta, expectedResult)

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_NotFound()
      throws Exception
      {
        // given
        final NotFoundException e = new NotFoundException("foo bar");
        // when
        final ResponseBuilder<?> builder = underTest.response().forException(e);
        // then
        assertContents(builder, "NotFoundExceptionOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_an_internal_error()
      throws Exception
      {
        // given
        final IOException e = new IOException("foo bar");
        // when
        final ResponseBuilder<?> builder = underTest.response().forException(e);
        // then
        assertContents(builder, "InternalErrorOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_PermanentRedirect()
      throws Exception
      {
        // when
        final ResponseBuilder<?> builder = underTest.response().permanentRedirect("http://acme.com");
        // then
        assertContents(builder, "PermanentRedirectOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertContents (final @Nonnull ResponseBuilder<?> builder, final String fileName)
      throws Exception
      {
        final TestResource tr = helper.testResourceFor(fileName);
        tr.writeToActualFile((byte[])builder.build());
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String toString (final @Nonnull DateTime dateTime)
      {
        return ResponseBuilderSupport.createFormatter(PATTERN_RFC1123).format(dateTime.toDate());
      }
  }
