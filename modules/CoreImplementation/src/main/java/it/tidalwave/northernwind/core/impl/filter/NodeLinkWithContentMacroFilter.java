/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Optional;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.spi.ParameterLanguageOverrideLinkPostProcessor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content._Content_;
import static it.tidalwave.northernwind.core.model.SiteNode._SiteNode_;

/***********************************************************************************************************************
 *
 * Implements the filter which expands the macro {@code nodeLink} into a link.
 *
 * <pre>
 * $nodeLink(relativePath='path1' contentRelativePath='path2' language='lang')$
 * </pre>
 *
 * <ul>
 * <li>{@code relativePath} must point to a node;</li>
 * <li>{@code contentRelativePath} must point to a content path inside that node;</li>
 * <li>{@code language} (optional) represents a language.</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Order(NodeLinkMacroFilter.ORDER - 1) @Slf4j
public class NodeLinkWithContentMacroFilter extends MacroFilter
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    @Inject
    private Optional<ParameterLanguageOverrideLinkPostProcessor> postProcessor;

    // FIXME: merge with NodeLinkMacroFilter, using an optional block for contentRelativePath
    public NodeLinkWithContentMacroFilter()
      {
        super("\\$nodeLink\\(relativePath='(?<relativePath>[^']*)', "
                          + "contentRelativePath='(?<contentRelativePath>[^']*)'"
                          + "(, language='(?<language>[^']*)')?\\)\\$");
      }

    @Override @Nonnull
    protected String filter (@Nonnull final Matcher matcher)
      throws NotFoundException
      {
        final var relativePath = matcher.group("relativePath");
        final var contentRelativePath = matcher.group("contentRelativePath");
        final var language = Optional.ofNullable(matcher.group("language"));

        final var site = siteProvider.get().getSite();
        final var siteNode = site.find(_SiteNode_).withRelativePath(relativePath).result();
        final var content = site.find(_Content_).withRelativePath(contentRelativePath).result();
        final var nodePath = siteNode.getRelativeUri();
        final var contentPath = content.getExposedUri().orElseThrow(() -> new NotFoundException("Content with no exposed URI"));
        final var link = site.createLink(nodePath.appendedWith(contentPath));

        return language.flatMap(l -> postProcessor.map(pp -> pp.postProcess(link, l))).orElse(link);
      }
  }
