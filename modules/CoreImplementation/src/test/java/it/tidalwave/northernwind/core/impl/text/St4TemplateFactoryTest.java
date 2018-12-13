/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.text;

import java.util.Optional;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.ContextManager;
import it.tidalwave.role.spi.DefaultContextManagerProvider;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static it.tidalwave.northernwind.core.model.Content.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.CoreMatchers.is;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class St4TemplateFactoryTest
  {
    private St4TemplateFactory underTest;

    private Site site;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    private void setup()
      {
        ContextManager.Locator.set(new DefaultContextManagerProvider()); // TODO: try to get rid of this

        site = mock(Site.class);
        MockContentSiteFinder.registerTo(site);
        underTest = new St4TemplateFactory(St4TemplateFactoryTest.class, site);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_retrieve_template_from_properly_configured_Content()
      throws NotFoundException
      {
        // given
        final String expectedResult = "Text of test template";
        final ResourcePath templatePath = ResourcePath.of("/the/path");
        final Content content = site.find(Content).withRelativePath(templatePath).result();
        when(content.getProperties().getProperty(eq(P_TEMPLATE))).thenReturn(Optional.of(expectedResult));
        // when
        final Optional<String> actualResult = underTest.getTemplate(templatePath);
        // then
        assertThat(actualResult.isPresent(), is(true));
        assertThat(actualResult.get(), is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_empty_when_Content_has_no_property()
      {
        // given
        final ResourcePath templatePath = ResourcePath.of("/the/path");
        // no P_TEMPLATE configured
        // when
        final Optional<String> actualResult = underTest.getTemplate(templatePath);
        // then
        assertThat(actualResult.isPresent(), is(false));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_return_empty_when_no_Content_found()
      {
        // given
        final ResourcePath templatePath = ResourcePath.of("/path/of/inexistent/content");
        // when
        final Optional<String> template = underTest.getTemplate(templatePath);
        // then
        assertThat(template.isPresent(), is(false));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_read_the_contents_of_the_embedded_template()
      {
        // when
        final String actualResult = underTest.getEmbeddedTemplate("testTemplate.txt");
        // then
        assertThat(actualResult, is("Text of test template"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = RuntimeException.class)
    public void must_properly_notify_a_missing_embedded_template()
      {
        // when
        underTest.getEmbeddedTemplate("notExistingTemplate.txt");
      }
  }
