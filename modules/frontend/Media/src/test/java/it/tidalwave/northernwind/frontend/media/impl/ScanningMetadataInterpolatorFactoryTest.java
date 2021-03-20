/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.media.impl;

import java.util.Collection;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolator;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.MetadataInterpolatorFactory;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.ShootingDataInterpolator;
import it.tidalwave.northernwind.frontend.media.impl.interpolator.XmpDcTitleInterpolator;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class ScanningMetadataInterpolatorFactoryTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ApplicationContext context;

    private MetadataInterpolatorFactory underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        context = helper.createSpringContext();
        underTest = context.getBean(MetadataInterpolatorFactory.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_find_all_the_interpolators()
      {
        // when
        final Collection<MetadataInterpolator> interpolators = underTest.getInterpolators();
        // then
        assertThat(interpolators, hasSize(2));
        assertThat(interpolators, hasItem(isA(ShootingDataInterpolator.class)));
        assertThat(interpolators, hasItem(isA(XmpDcTitleInterpolator.class)));
      }
  }
