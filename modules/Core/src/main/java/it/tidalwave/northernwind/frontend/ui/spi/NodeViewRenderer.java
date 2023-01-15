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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * A {@link Layout} visitor that renders everything for the given layout.
 *
 * @stereotype  Visitor
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe
public class NodeViewRenderer<T> extends VisitorSupport<Layout, T>
  {
    @Nonnull
    private final ViewAndControllerLayoutBuilder vacLayoutBuilder;

    @Nonnull
    private final BiConsumer<T, T> attacher;

    @Nonnull
    private final RenderContext renderContext;

    private final Deque<T> componentStack = new ArrayDeque<>();

    @Getter
    private T rootComponent;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public NodeViewRenderer (@Nonnull final Request request,
                             @Nonnull final RequestContext requestContext,
                             @Nonnull final ViewAndControllerLayoutBuilder vacLayoutBuilder,
                             @Nonnull final BiConsumer<T, T> attacher)
      {
        this.vacLayoutBuilder = vacLayoutBuilder;
        this.attacher         = attacher;
        this.renderContext    = new DefaultRenderContext(request, requestContext);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void preVisit (@Nonnull final Layout layout)
      {
        final ViewAndController vac = vacLayoutBuilder.getViewAndControllerFor(layout).get();
        final T component = renderView(vac, layout);

        if (rootComponent == null)
          {
            rootComponent = component;
          }
        else
          {
            attacher.accept(componentStack.peek(), component);
          }

        componentStack.push(component);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void postVisit (@Nonnull final Layout layout)
      {
        componentStack.pop();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private T renderView (@Nonnull final ViewAndController vac, @Nonnull final Layout layout)
      {
        try
          {
            return (T)vac.renderView(renderContext);
          }
        catch (Throwable e)
          {
            return (T)vacLayoutBuilder.getErrorViewSupplier().apply(layout, e);
          }
      }
  }
