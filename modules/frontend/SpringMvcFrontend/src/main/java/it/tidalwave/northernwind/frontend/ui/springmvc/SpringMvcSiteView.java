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
package it.tidalwave.northernwind.frontend.ui.springmvc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.spi.NodeViewBuilderVisitor;
import it.tidalwave.northernwind.frontend.ui.spi.NodeViewRendererVisitor;
import it.tidalwave.northernwind.frontend.springmvc.SpringMvcResponseHolder;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The Spring MVC implementation of {@link SiteView}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Scope(value = "session") @Slf4j
public class SpringMvcSiteView implements SiteView
  {
    @Inject
    private RequestContext requestContext;

    @Inject
    private SpringMvcResponseHolder responseHolder;

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

        final NodeViewBuilderVisitor nodeViewBuilderVisitor = new NodeViewBuilderVisitor(siteNode, this::createFallbackView);
        siteNode.getLayout().accept(nodeViewBuilderVisitor);
//        log.info(">>>> DYNAMIC ATTRIBUTES {}", nodeViewBuilderVisitor.getAttributes());
        final NodeViewRendererVisitor<TextHolder, TextHolder> nodeViewRendererVisitor =
                new NodeViewRendererVisitor<>(requestContext, nodeViewBuilderVisitor, this::attach);
        siteNode.getLayout().accept(nodeViewRendererVisitor);
        final TextHolder textHolder = nodeViewRendererVisitor.getRootComponent();
        responseHolder.response().withStatus(nodeViewRendererVisitor.getStatus())
                                 .withBody(textHolder.asBytes("UTF-8"))
                                 .withContentType(textHolder.getMimeType())
                                 .put();
      }

    @Override
    public void setCaption (final String string)
      {
//        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Nonnull
    private TextHolder createFallbackView (final @Nonnull Layout layout, final @Nonnull String message)
      {
        return new HtmlHolder("<div>" + message + "</div>");
      }

    private void attach (final @Nonnull TextHolder parent, final @Nonnull TextHolder child)
      {
        parent.addComponent(child);
      }
  }
