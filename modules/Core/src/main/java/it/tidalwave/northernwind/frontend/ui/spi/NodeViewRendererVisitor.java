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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Stack;
import java.util.function.BiConsumer;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewController.RenderContext;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A support class for creating visitors for {@link Layout} that build a view implementation.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public class NodeViewRendererVisitor<COMPONENT, CONTAINER> extends VisitorSupport<Layout, COMPONENT>
  {
    @Nonnull
    private final NodeViewBuilderVisitor nodeViewBuilderVisitor;

    @Nonnull
    private final BiConsumer<CONTAINER, COMPONENT> attacher;

    private final RenderContext renderContext;

    private final Stack<COMPONENT> components = new Stack<>();

    @Getter
    private int status = 200;

    @Getter
    private COMPONENT rootComponent;

    public NodeViewRendererVisitor (final @Nonnull RequestContext requestContext,
                                    final @Nonnull NodeViewBuilderVisitor nodeViewBuilderVisitor,
                                    final @Nonnull BiConsumer<CONTAINER, COMPONENT> attacher)
      {
        this.nodeViewBuilderVisitor = nodeViewBuilderVisitor;
        this.attacher = attacher;
        this.renderContext = new RenderContext(requestContext);
      }


    @Override
    public void preVisit (final @Nonnull Layout layout)
      {
        final ViewAndController viewAndController = nodeViewBuilderVisitor.getValue().get(layout);
        final COMPONENT component = createComponent(viewAndController, layout);

        if (rootComponent == null)
          {
            rootComponent = component;
          }
        else
          {
            attacher.accept((CONTAINER)components.peek(), component);
          }

        components.push(component);
      }


    @Override
    public void postVisit (final @Nonnull Layout layout)
      {
        components.pop();
      }

    @Nonnull
    private COMPONENT createComponent (final @Nonnull ViewAndController viewAndController, final @Nonnull Layout layout)
      {
        assert viewAndController != null : "No ViewAndController for" + layout;

        try
          {
            return (COMPONENT)viewAndController.renderView(renderContext);
          }
        catch (HttpStatusException e)
          {
            // FIXME: should set the status in the response - unfortunately at this level the ResponseHolder is abstract
            log.warn("Returning HTTP status {}", e.getHttpStatus());
            status = e.getHttpStatus();

            String message = "<h1>Status " + e.getHttpStatus() + "</h1>"; // FIXME: use a resource bundle

            if (e.getHttpStatus() == 404)
              {
                message = "<h1>Not found</h1>";
              }

            if (e.getHttpStatus() == 500)
              {
                message = "<h1>Internal error</h1>";
              }

            return (COMPONENT)nodeViewBuilderVisitor.getFallbackViewSupplier().apply(layout, message);
          }
        catch (Throwable e)
          {
            log.warn("Internal error", e);
            status = 500;
            return (COMPONENT)nodeViewBuilderVisitor.getFallbackViewSupplier().apply(layout, e.toString());
          }
      }
  }
