/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.media.impl;

import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class EmbeddedMediaMetadataProviderTest
  {
    private ApplicationContext context;

    private EmbeddedMediaMetadataProvider fixture;
    
    private MetadataCache metadataCache;
    
    private Id mediaId;
    
    private ResourceProperties siteNodeProperties;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("EmbeddedMediaMetadataProviderTestBeans.xml");
        fixture = context.getBean(EmbeddedMediaMetadataProvider.class);
        metadataCache = context.getBean(MetadataCache.class);
        siteNodeProperties = mock(ResourceProperties.class);
        mediaId = new Id("mediaId");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_the_interpolated_string_when_metadata_is_found() 
      throws Exception
      {
        final Metadata metadata = mock(Metadata.class);
        when(metadata.interpolateMetadataString(same(siteNodeProperties), anyString()))
                .thenReturn("result");
        when(metadataCache.findMetadataById(eq(mediaId), same(siteNodeProperties))).thenReturn(metadata);
        
        final String result = fixture.getMetadataString(mediaId, "format", siteNodeProperties);
        
        assertThat(result, is("result"));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_empty_string_when_media_not_found()
      throws Exception
      {
        when(metadataCache.findMetadataById(eq(mediaId), same(siteNodeProperties)))
                .thenThrow(new NotFoundException("Media not found"));
        
        final String result = fixture.getMetadataString(mediaId, "format", siteNodeProperties);

        assertThat(result, is(""));
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_empty_string_when_io_error()
      throws Exception
      {
        when(metadataCache.findMetadataById(eq(mediaId), same(siteNodeProperties)))
                .thenThrow(new IOException("Cannot open file"));
        
        final String result = fixture.getMetadataString(mediaId, "format", siteNodeProperties);

        assertThat(result, is(""));
      }
  }
