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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;
        
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
    
    private ResourceFile mediaFile;
    
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
        mediaFile = mock(ResourceFile.class);
        siteNodeProperties = mock(ResourceProperties.class);
        mediaId = new Id("mediaId");

//        when(metadataCache.findMediaResourceFile(same(siteNodeProperties), eq(mediaId))).thenReturn(mediaFile);
//        when(metadataCache.loadImage(same(mediaFile))).thenReturn(mockedImage.image);
      }
  }
