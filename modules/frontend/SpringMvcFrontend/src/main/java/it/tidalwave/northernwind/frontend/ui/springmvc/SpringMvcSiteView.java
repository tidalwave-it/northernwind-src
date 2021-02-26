/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.SiteView;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.DefaultRenderContext;
import it.tidalwave.northernwind.frontend.ui.spi.NodeViewRenderer;
import it.tidalwave.northernwind.frontend.ui.spi.ViewAndControllerLayoutBuilder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.TextHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.springmvc.SpringMvcResponseHolder;
import lombok.extern.slf4j.Slf4j;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    private final ThreadLocal<HttpStatus> httpStatus = new ThreadLocal<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderSiteNode (final @Nonnull Request request, final @Nonnull SiteNode siteNode)
      {
        log.info("renderSiteNode({})", siteNode);
        httpStatus.set(HttpStatus.OK);
        final RenderContext renderContext = new DefaultRenderContext(request, requestContext);
        final ViewAndControllerLayoutBuilder vacBuilder = new ViewAndControllerLayoutBuilder(siteNode,
                                                                                             renderContext,
                                                                                             this::createErrorView);
        siteNode.getLayout().accept(vacBuilder);
        final NodeViewRenderer<TextHolder> renderer = new NodeViewRenderer<>(request, requestContext, vacBuilder, this::attach);
        siteNode.getLayout().accept(renderer);
        final TextHolder textHolder = renderer.getRootComponent();
        responseHolder.response().withStatus(httpStatus.get().value())
                                 .withBody(textHolder.asBytes(UTF_8))
                                 .withContentType(textHolder.getMimeType())
                                 .put();
      }

    /*******************************************************************************************************************
     *
     * TODO: sometimes this gets wrapped in a layout (e.g. when it's thrown by a Node Controller), otherwise it just
     * renders as bare markup. If there is no container, it should be wrapped by a small embedded template.
     *
     * Only for codes such as 404 and 500, you could configure a wrapping template from a Node, so that the usual
     * layout is rendered.
     *
     ******************************************************************************************************************/
    @Nonnull
    private TextHolder createErrorView (final @Nonnull Layout layout, final @Nonnull Throwable t)
      {
        log.warn("While processing " + layout, t);
        final HttpStatus status = (t instanceof HttpStatusException)
                ? HttpStatus.valueOf(((HttpStatusException)t).getHttpStatus())
                : HttpStatus.INTERNAL_SERVER_ERROR;
        httpStatus.set(status);
        return new HtmlHolder("<div><h1>" + status.getReasonPhrase() + "</h1></div>");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void attach (final @Nonnull TextHolder parent, final @Nonnull TextHolder child)
      {
        parent.addComponent(child);
      }
  }
