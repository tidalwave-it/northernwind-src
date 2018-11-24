/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ParameterLanguageOverrideLinkPostProcessorTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private ParameterLanguageOverrideLinkPostProcessor underTest;

    private ApplicationContext context;

    private ParameterLanguageOverrideRequestProcessor plorp;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        context = helper.createSpringContext();
        plorp = context.getBean(ParameterLanguageOverrideRequestProcessor.class);
        when(plorp.getParameterName()).thenReturn("lang");
        underTest = context.getBean(ParameterLanguageOverrideLinkPostProcessor.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "linkProvider")
    public void must_properly_postProcess (final @Nonnull String link,
                                           final @Nonnull String parameterValue,
                                           final @Nonnull String expectedResult)
      {
        // when
        final String result = underTest.postProcess(link, parameterValue);
        // then
        assertThat(result, is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] linkProvider()
      {
        return new Object[][]
          {
            { "http://acme.com/path/resource",                  "en", "http://acme.com/path/resource?lang=en"         },
            { "http://acme.com/path/resource?a=b",              "en", "http://acme.com/path/resource?a=b&lang=en"     },
            { "http://acme.com/path/resource?lang=it",          "en", "http://acme.com/path/resource?lang=en"         },
            { "http://acme.com/path/resource?lang=it&a=b",      "en", "http://acme.com/path/resource?lang=en&a=b"     },
            { "http://acme.com/path/resource?c=d&lang=it",      "en", "http://acme.com/path/resource?c=d&lang=en"     },
            { "http://acme.com/path/resource?c=d&lang=it&a=b",  "en", "http://acme.com/path/resource?c=d&lang=en&a=b" },

            { "http://acme.com/path/resource/",                 "en", "http://acme.com/path/resource/?lang=en"        },
            { "http://acme.com/path/resource/?a=b",             "en", "http://acme.com/path/resource/?a=b&lang=en"    },
            { "http://acme.com/path/resource/?lang=it",         "en", "http://acme.com/path/resource/?lang=en"        },
            { "http://acme.com/path/resource/?lang=it&a=b",     "en", "http://acme.com/path/resource/?lang=en&a=b"    },
            { "http://acme.com/path/resource/?c=d&lang=it",     "en", "http://acme.com/path/resource/?c=d&lang=en"    },
            { "http://acme.com/path/resource/?c=d&lang=it&a=b", "en", "http://acme.com/path/resource/?c=d&lang=en&a=b"},

            // NW-165
            { "http://acme.com/path/image.jpg",                 "en", "http://acme.com/path/image.jpg?lang=en"        },
            { "http://acme.com/path/image.jpg?a=b",             "en", "http://acme.com/path/image.jpg?a=b&lang=en"    },
            { "http://acme.com/path/image.jpg?lang=it",         "en", "http://acme.com/path/image.jpg?lang=en"        },
            { "http://acme.com/path/image.jpg?lang=it&a=b",     "en", "http://acme.com/path/image.jpg?lang=en&a=b"    },
            { "http://acme.com/path/image.jpg?c=d&lang=it",     "en", "http://acme.com/path/image.jpg?c=d&lang=en"    },
            { "http://acme.com/path/image.jpg?c=d&lang=it&a=b", "en", "http://acme.com/path/image.jpg?c=d&lang=en&a=b"}
          };
      }
  }