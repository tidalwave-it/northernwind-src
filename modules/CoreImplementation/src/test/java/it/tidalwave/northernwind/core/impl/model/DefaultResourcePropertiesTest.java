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
package it.tidalwave.northernwind.core.impl.model;

import it.tidalwave.util.Key;
import java.time.ZonedDateTime;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import javax.annotation.Nonnull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class DefaultResourcePropertiesTest
  {
    @Test(dataProvider = "values")
    public void testConvertValue (final @Nonnull Key<?> key, final @Nonnull Object value, final @Nonnull Object expectedValue)
      {
        // when
        final Object actualValue = DefaultResourceProperties.convertValue(key, value);
        // then
        assertThat(actualValue, is(expectedValue));
      }

    @DataProvider
    private Object[][] values()
      {
        return new Object[][]
          {
            { new Key<String>("") {},         "foo",      "foo" },
            { new Key<Integer>("") {},        "17",       17    },
            { new Key<Float>("") {},          "3.4",      3.4f  },
            { new Key<Double>("") {},         "5.2",      5.2   },
            { new Key<ZonedDateTime>("") {},  "2012-02-23T21:24:00.000+01:00", ZonedDateTime.parse("2012-02-23T21:24:00.000+01:00", ISO_ZONED_DATE_TIME)  },
//            { new Key<List<Integer>>("") {},   asList("1", "2"), asList(1, 2) },
          };
      }
}
