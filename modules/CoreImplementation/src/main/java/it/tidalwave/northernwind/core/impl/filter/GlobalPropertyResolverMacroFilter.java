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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.regex.Matcher;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;

/***********************************************************************************************************************
 *
 * TODO: is still needed now that we have SiteNodePropertyMacroFilter?
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE)
public class GlobalPropertyResolverMacroFilter extends MacroFilter
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    public GlobalPropertyResolverMacroFilter()
      {
        super("\\$globalProperty\\(name='([^']*)'\\)\\$");
      }

    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      {
        try
          {
            // FIXME: should be pushed into @PostConstruct, but can't - see NW-224
            final Site site = siteProvider.get().getSite();
            final SiteNode rootSiteNode = site.find(SiteNode).withRelativeUri("/").result(); // See NW-223
            // END FIXME
            final String propertyName = matcher.group(1);
            return rootSiteNode.getProperties().getProperty(new Key<String>(propertyName), "");
          }
        catch (NotFoundException | IOException e)
          {
            return "ERR";
          }
      }
  }
