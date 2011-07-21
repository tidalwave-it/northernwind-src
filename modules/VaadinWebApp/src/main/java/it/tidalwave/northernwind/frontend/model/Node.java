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
package it.tidalwave.northernwind.frontend.model;

import it.tidalwave.northernwind.frontend.ui.component.article.DefaultArticleViewController;
import it.tidalwave.northernwind.frontend.ui.component.article.vaadin.VaadinArticleView;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @RequiredArgsConstructor @Slf4j
public class Node 
  {
    @Nonnull @Inject
    private WebSiteModel webSiteModel;
    
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;
    
    @Nonnull @Getter
    private final String uri;

    public Node (final @Nonnull File file, final @Nonnull String uri)
      {
        resource = new Resource(file);  
        this.uri = uri;
      }

    @Nonnull
    public Object createContents()
      throws IOException 
      {
        final String contentUri = resource.getProperties().getProperty("main.content");
        final VaadinArticleView articleView = new VaadinArticleView("main");
        new DefaultArticleViewController(articleView, contentUri.replaceAll("/content/document/Mobile", "").replaceAll("/content/document", ""));
        return articleView;
      }
  }