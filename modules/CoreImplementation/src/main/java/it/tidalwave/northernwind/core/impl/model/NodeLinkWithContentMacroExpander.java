/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(NodeLinkMacroExpander.ORDER - 1) @Slf4j
public class NodeLinkWithContentMacroExpander extends MacroExpander
  {
    public NodeLinkWithContentMacroExpander()
      {
        super("\\$nodeLink\\(relativePath='(.*)', contentRelativePath='(.*)'\\)\\$");
      } 
    
    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      throws NotFoundException, IOException
      {
        final String relativePath = matcher.group(1);
        final String contentRelativePath = matcher.group(2);
        final SiteNode siteNode = site.find(SiteNode.class).withRelativePath(relativePath).result();
        final Content content = site.find(Content.class).withRelativePath(contentRelativePath).result();
        return site.createLink(siteNode.getRelativeUri() + "/" + content.getExposedUri());
      }
  }
