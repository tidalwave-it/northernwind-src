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
import java.io.File;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import com.google.common.io.Files;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.test.FileComparisonUtils;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder.ResponseBuilderSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResponseHolderTest
  {
    private ResponseHolder<?> fixture;
            
    private ResourceFile resourceFile;
    
    private DateTime currentTime = new DateTime(1341242353456L);

    private DateTime resourceLatestModifiedTime = new DateTime(1341242553456L);
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        MockResponseHolder.setCurrentTime(currentTime);
        
        resourceFile = mock(ResourceFile.class);
        when(resourceFile.asBytes()).thenReturn("FILE CONTENT".getBytes());
        when(resourceFile.getMimeType()).thenReturn("text/plain");
        when(resourceFile.getLatestModificationTime()).thenReturn(resourceLatestModifiedTime);
        
        fixture = new MockResponseHolder();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFile()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile);
        assertContents(builder, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFileWithEtagNotMatching()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile)
                                                                    .withRequestIfNoneMatch("\"xxxx\"");
        assertContents(builder, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFileWithEtagMatching()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile)
                                                                    .withRequestIfNoneMatch("\"1341242553456\"");
        assertContents(builder, "ResourceFileNotModifiedOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFileWithIfModifiedSinceNotModified()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile)
                                                                    .withRequestIfModifiedSince(toString(resourceLatestModifiedTime));
        assertContents(builder, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFileWithIfModifiedSinceModified()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile)
                                                                    .withRequestIfModifiedSince(toString(resourceLatestModifiedTime.plusSeconds(1)));
        assertContents(builder, "ResourceFileNotModifiedOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFileWithExpirationTime()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().fromFile(resourceFile)
                                                                    .withExpirationTime(Duration.standardDays(7));
        assertContents(builder, "ResourceFileOutputWithExpirationTime.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputANotFound()
      throws Exception
      {
        final NotFoundException e = new NotFoundException("foo bar");
        final ResponseBuilderSupport<?> builder = fixture.response().forException(e);
        assertContents(builder, "NotFoundExceptionOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAnInternalError()
      throws Exception
      {
        final IOException e = new IOException("foo bar");
        final ResponseBuilderSupport<?> builder = fixture.response().forException(e);
        assertContents(builder, "InternalErrorOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAPermanentRedirect()
      throws Exception
      {
        final ResponseBuilderSupport<?> builder = fixture.response().permanentRedirect("http://acme.com");
        assertContents(builder, "PermanentRedirectOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertContents (final @Nonnull ResponseBuilderSupport<?> builder, final String fileName)
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
        return ResponseBuilderSupport.createFormatter(ResponseBuilderSupport.PATTERN_RFC1123).format(dateTime.toDate());
      }
  }
