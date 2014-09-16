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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.test.FileComparisonUtils;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.ResourceFile;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
    public void setupFixture()
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
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile);
        assertContents(builder, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_ResourceFile_with_ExpirationTime()
      throws Exception
      {
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                             .withExpirationTime(Duration.standardDays(7));
        assertContents(builder, "ResourceFileOutputWithExpirationTime.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_a_ResourceFile_when_Etag_not_Matching()
      throws Exception
      {
        headers.put(HEADER_IF_NONE_MATCH, "\"xxxx\"");
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                             .forRequest(request);
        assertContents(builder, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_output_NotModified_when_Etag_Matching()
      throws Exception
      {
        headers.put(HEADER_IF_NONE_MATCH, "\"1341242553456\"");
        final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                             .forRequest(request);
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
            final DateTime ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toString(ifModifiedSinceTime));
            final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                                 .forRequest(request);
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
            final DateTime ifModifiedSinceTime = resourceLatestModifiedTime.plusSeconds(deltaSeconds);
            headers.put(HEADER_IF_MODIFIED_SINCE, toString(ifModifiedSinceTime));
            final ResponseBuilder<?> builder = underTest.response().fromFile(resourceFile)
                                                                 .forRequest(request);
            assertContents(builder, "ResourceFileNotModifiedOutput.txt");
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_roperly_output_NotFound()
      throws Exception
      {
        final NotFoundException e = new NotFoundException("foo bar");
        final ResponseBuilder<?> builder = underTest.response().forException(e);
        assertContents(builder, "NotFoundExceptionOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_an_internal_error()
      throws Exception
      {
        final IOException e = new IOException("foo bar");
        final ResponseBuilder<?> builder = underTest.response().forException(e);
        assertContents(builder, "InternalErrorOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_output_a_PermanentRedirect()
      throws Exception
      {
        final ResponseBuilder<?> builder = underTest.response().permanentRedirect("http://acme.com");
        assertContents(builder, "PermanentRedirectOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertContents (final @Nonnull ResponseBuilder<?> builder, final String fileName)
      throws Exception
      {
        final File actualFile = new File("target/test-results/" + fileName);
        final File expectedFile = new File("src/test/resources/expected-results/" + fileName);
        actualFile.getParentFile().mkdirs();
        Files.write((byte[])builder.build(), actualFile);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
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