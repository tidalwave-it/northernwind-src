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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Order(NodeLinkMacroFilter.ORDER) @Slf4j
public class NodeLinkMacroFilter extends MacroFilter
  {
    /* package */ static final int ORDER = 20;

    @Inject
    private Provider<SiteProvider> siteProvider;

    public NodeLinkMacroFilter()
      {
        super("\\$nodeLink\\(relativePath='([^']*)'\\)\\$");
      }

    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      throws NotFoundException
      {
        final String relativePath = matcher.group(1);
        final Site site = siteProvider.get().getSite();
        final SiteNode siteNode = site.find(SiteNode.class).withRelativePath(relativePath).result();
        return site.createLink(siteNode.getRelativeUri());
      }
  }
