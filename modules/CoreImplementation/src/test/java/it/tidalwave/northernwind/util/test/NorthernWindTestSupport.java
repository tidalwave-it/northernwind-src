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
package it.tidalwave.northernwind.util.test;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.model.mock.MockContentSiteFinder;
import it.tidalwave.northernwind.core.impl.model.mock.MockSiteNodeSiteFinder;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.annotations.BeforeMethod;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * A support class for testing. It:
 *
 * <ul>
 *     <li>creates a Spring {@link ApplicationContext} out of a Spring Beans file, which should contain at least mock
 *         implementations of {@link SiteProvider} and {@link Site};</li>
 *     <li>binds the {@code Site} instance to {@code SiteProvider};</li>
 *     <li>provides a {@link MockContentSiteFinder} for any {@code site.find(Content}};</li>
 *     <li>provides a {@link MockSiteNodeSiteFinder} for any {@code site.find(SiteNode}};</li>
 *     <li>provides a mock implementation of {@code site.createLink(resource)} which returns a string
 *     {@code "/LINK<resource.toString()"}</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class NorthernWindTestSupport
  {
    protected final SpringTestHelper helper = new SpringTestHelper(this);

    protected ApplicationContext context;

    protected SiteProvider siteProvider;

    protected Site site;

    @BeforeMethod
    protected final void setupContext()
      {
        setupContext(ctx -> {});
      }

    protected void setupContext (final @Nonnull Consumer<GenericApplicationContext> modifier)
      {
        context      = helper.createSpringContext(modifier);
        siteProvider = context.getBean(SiteProvider.class);
        site         = context.getBean(Site.class);

        when(siteProvider.getSite()).thenReturn(site);

        when(site.find(eq(Content))).thenReturn(new MockContentSiteFinder());
        when(site.find(eq(SiteNode))).thenReturn(new MockSiteNodeSiteFinder());
        when(site.createLink(any(ResourcePath.class))).thenAnswer(
                invocation -> "/LINK" + ((ResourcePath)invocation.getArguments()[0]).asString());
//                return ((ResourcePath)invocation.getArguments()[0]).prepend("LINK").asString();
      }
  }
