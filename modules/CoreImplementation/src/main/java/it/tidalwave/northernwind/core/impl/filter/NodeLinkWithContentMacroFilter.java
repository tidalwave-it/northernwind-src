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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.regex.Matcher;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.spi.ParameterLanguageOverrideLinkPostProcessor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(NodeLinkMacroFilter.ORDER - 1) @Slf4j
public class NodeLinkWithContentMacroFilter extends MacroFilter
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    // FIXME: what about @AutoWired(required=false)?
    @Inject
    private ApplicationContext context;

    @CheckForNull
    private ParameterLanguageOverrideLinkPostProcessor postProcessor;

    // FIXME: merge with NodeLinkMacroFilter, using an optional block for contentRelativePath
    public NodeLinkWithContentMacroFilter()
      {
        super("\\$nodeLink\\(relativePath='([^']*)', contentRelativePath='([^']*)'(, language='([^']*)')?\\)\\$");
      }

    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      throws NotFoundException, IOException
      {
        final String relativePath = matcher.group(1);
        final String contentRelativePath = matcher.group(2);
        final String language = matcher.group(4);
        final Site site = siteProvider.get().getSite();
        final SiteNode siteNode = site.find(SiteNode.class).withRelativePath(relativePath).result();
        final Content content = site.find(Content.class).withRelativePath(contentRelativePath).result();
        final ResourcePath path = siteNode.getRelativeUri().appendedWith(content.getExposedUri());
        final String link = site.createLink(path);

        return ((language == null) || (postProcessor == null)) ? link : postProcessor.postProcess(link, language);
      }

    @PostConstruct
    private void initialize()
      {
        try
          {
            postProcessor = context.getBean(ParameterLanguageOverrideLinkPostProcessor.class);
          }
        catch (NoSuchBeanDefinitionException e)
          {
            // ok, it's optional
          }
      }
  }
