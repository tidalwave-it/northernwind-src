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

import javax.annotation.Nonnull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.is;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class NormalizedLinkPostProcessorTest
  {
    private NormalizedLinkPostProcessor underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setUp()
      {
        underTest = new NormalizedLinkPostProcessor();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "links")
    public void must_create_correct_links (final @Nonnull String link, final @Nonnull String expectedResult)
      throws Exception
      {
        // when
        final String result = underTest.postProcess(link);
        // then
        assertThat(result, is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    public Object[][] links()
      {
        return new Object[][]
          {
            { "/link",          "/link/"         },
            { "/link/",         "/link/"         },
            { "/link?arg=val",  "/link?arg=val"  },
            { "/link/?arg=val", "/link/?arg=val" },
            { "/image.jpg",     "/image.jpg"     },

            //
            { "http://acme.com/link",          "http://acme.com/link/"         },
            { "http://acme.com/link/",         "http://acme.com/link/"         },
            { "http://acme.com/link?arg=val",  "http://acme.com/link?arg=val"  },
            { "http://acme.com/link/?arg=val", "http://acme.com/link/?arg=val" },
            { "http://acme.com/image.jpg",     "http://acme.com/image.jpg"     },

            // TODO: add more
          };
      }
  }
