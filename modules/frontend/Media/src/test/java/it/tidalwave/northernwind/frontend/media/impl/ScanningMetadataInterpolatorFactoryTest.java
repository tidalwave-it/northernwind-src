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
package it.tidalwave.northernwind.frontend.media.impl;

import java.util.Collection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolator;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.ShootingDataInterpolator;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.XmpDcTitleInterpolator;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
        
/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ScanningMetadataInterpolatorFactoryTest
  {
    private ApplicationContext context;

    private MetadataInterpolatorFactory fixture;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setupFixture()
      throws Exception
      {
        context = new ClassPathXmlApplicationContext("ScanningMetadataInterpolatorFactoryTestBeans.xml");
        fixture = context.getBean(MetadataInterpolatorFactory.class);
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_find_all_the_interpolators()
      {
        final Collection<MetadataInterpolator> interpolators = fixture.getInterpolators();
        
        assertThat(interpolators, hasSize(2));
        assertThat(interpolators, hasItem(isA(ShootingDataInterpolator.class)));
        assertThat(interpolators, hasItem(isA(XmpDcTitleInterpolator.class)));
      }
  }
