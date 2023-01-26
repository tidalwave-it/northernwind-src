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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourceFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lombok.RequiredArgsConstructor;
import it.tidalwave.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;
import static it.tidalwave.northernwind.core.model.spi.ResponseBuilderSupport.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class ResponseBuilderTest
  {
    protected final SpringTestHelper helper = new SpringTestHelper(this);

    private final ZonedDateTime currentTime = Instant.ofEpochMilli(1341242353456L).atZone(ZoneId.of("GMT"));

    private final ZonedDateTime resourceLatestModifiedTime = Instant.ofEpochMilli(1341242553456L).atZone(ZoneId.of("GMT"));

    @Nonnull
    private final Supplier<? extends MockResponseHolder> responseHolderFactory;

    private ResourceFile resourceFile;

    private Request request;

    private Map<String, String> headers;

    private ResponseBuilder<?> underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public ResponseBuilderTest()
      {
        responseHolderFactory = MockResponseHolder::new;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      throws Exception
      {
        resourceFile = mock(ResourceFile.class);
        when(resourceFile.asBytes()).thenReturn("FILE CONTENT".getBytes());
        when(resourceFile.getMimeType()).thenReturn("text/plain");
        when(resourceFile.getLatestModificationTime()).thenReturn(resourceLatestModifiedTime);

        headers = new HashMap<>();
        request = mock(Request.class);
        when(request.getHeader(anyString())).thenAnswer(i -> Optional.ofNullable(headers.get(i.getArgument(0))));

        final var responseHolder = responseHolderFactory.get();
        responseHolder.setClockSupplier(() -> Clock.fixed(currentTime.toInstant(), currentTime.getZone()));
        underTest = responseHolder.response();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_ResourceFile()
      throws Exception
      {
        // when
        final var builder = underTest.fromFile(resourceFile);
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
        final var builder = underTest.fromFile(resourceFile).withExpirationTime(Duration.ofDays(7));
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
        final var builder = underTest.fromFile(resourceFile).forRequest(request);
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
        final var builder = underTest.fromFile(resourceFile).forRequest(request);
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
        for (var deltaSeconds = -10; deltaSeconds < 0; deltaSeconds++)
          {
            // given
            final var ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toRfc1123String(ifModifiedSinceTime));
            // when
            final var builder = underTest.fromFile(resourceFile).forRequest(request);
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
        for (var deltaSeconds = 0; deltaSeconds < 10; deltaSeconds++)
          {
            // given
            final var ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toRfc1123String(ifModifiedSinceTime));
            // when
            final var builder = underTest.fromFile(resourceFile).forRequest(request);
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
        final var e = new NotFoundException("foo bar");
        // when
        final var builder = underTest.forException(e);
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
        final var e = new IOException("foo bar");
        // when
        final var builder = underTest.forException(e);
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
        final var builder = underTest.permanentRedirect("http://acme.com");
        // then
        assertContents(builder, "PermanentRedirectOutput.txt");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void assertContents (@Nonnull final ResponseBuilder<?> builder, final String fileName)
      throws Exception
      {
        final var tr = helper.testResourceFor(fileName);
        tr.writeToActualFile((byte[])builder.build());
        tr.assertActualFileContentSameAsExpected();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String toRfc1123String (@Nonnull final ZonedDateTime dateTime)
      {
        return dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
      }
  }
