/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.springmvc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.springmvc.ResponseEntityHolder;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link SiteView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value="session") @Slf4j
public class SpringMvcSiteView implements SiteView
  {
    @Inject @Nonnull
    private ResponseEntityHolder responseHolder;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderSiteNode (final @Nonnull SiteNode siteNode) 
      throws IOException
      {
        log.info("renderSiteNode({})", siteNode);
        
        try
          {
            final Visitor<Layout, TextHolder> nodeViewBuilderVisitor = new SpringMvcNodeViewBuilderVisitor(siteNode);
            final TextHolder textHolder = siteNode.getLayout().accept(nodeViewBuilderVisitor);    
            responseHolder.response().withBody(textHolder.asBytes("UTF-8"))
                                     .withContentType(textHolder.getMimeType())
                                     .put();
          }
        catch (NotFoundException e) 
          {
            log.error("", e);
            responseHolder.response().withStatus(HttpStatus.NOT_FOUND.value()) 
                                     .withBody(e.toString())
                                     .withContentType("text/html")
                                     .put();
          }
      }

    @Override
    public void setCaption(String string)
      {
//        throw new UnsupportedOperationException("Not supported yet.");
      }
  }
