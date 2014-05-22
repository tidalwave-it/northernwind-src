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
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.test.FileComparisonUtils;
import java.io.File;
import javax.annotation.Nonnull;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.*;
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
public class ResponseHolderTest
  {
    private ResponseHolder<?> fixture;
            
    private ResourceFile resourceFile;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        resourceFile = mock(ResourceFile.class);
        when(resourceFile.asBytes()).thenReturn("FILE CONTENT".getBytes());
        when(resourceFile.getMimeType()).thenReturn("text/plain");
        when(resourceFile.getLatestModificationTime()).thenReturn(new DateTime(1341242553456L));
        
        fixture = new MockResponseHolder();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void mustProperlyOutputAResourceFile()
      throws Exception
      {
        final byte[] response = (byte[])(fixture.response().fromFile(resourceFile).build());
        assertContents(response, "ResourceFileOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void m1()
      throws Exception
      {
        final NotFoundException e = new NotFoundException("foo bar");
        final byte[] response = (byte[])(fixture.response().forException(e).build());
        assertContents(response, "NotFoundExceptionOutput.txt");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void assertContents (final @Nonnull byte[] response, final String fileName)
      throws Exception
      {
        final File actualFile = new File("target/test-results/" + fileName);
        final File expectedFile = new File("src/test/resources/expected-results/" + fileName);
        actualFile.getParentFile().mkdirs();
        Files.write(response, actualFile);
        FileComparisonUtils.assertSameContents(expectedFile, actualFile);
      }
  }
