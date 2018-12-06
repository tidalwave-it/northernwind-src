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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import it.tidalwave.northernwind.frontend.ui.ViewController.RenderContext;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A support class for creating visitors for {@link Layout} that build a view implementation.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @RequiredArgsConstructor @Slf4j
public class NodeViewBuilderVisitor extends VisitorSupport<Layout, Map<Layout, ViewAndController>>
  {
    private static final ViewController VOID_CONTROLLER = new ViewController() {};

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final RenderContext renderContext;

    @Getter @Nonnull
    private final BiFunction<Layout, String, Object> fallbackViewSupplier;

    private final Map<Layout, ViewAndController> viewAndControllerMapByLayout = new IdentityHashMap<>();

    @Override
    public void visit (final @Nonnull Layout layout)
      {
        try
          {
            final ViewAndController createComponent = createComponent(layout);
            createComponent.getController().initialize(renderContext);
            viewAndControllerMapByLayout.put(layout, createComponent);
          }
        catch (Exception e)
          {
            viewAndControllerMapByLayout.put(layout, createFallback(layout, e.toString()));
          }
      }

    @Override @Nonnull
    public Map<Layout, ViewAndController> getValue()
      {
        return viewAndControllerMapByLayout;
      }

    @Nonnull
    private ViewAndController createComponent (@Nonnull Layout layout)
      {
        try
          {
            return layout.createViewAndController(siteNode);
          }
        catch (NotFoundException e)
          {
            log.warn("Component not found", e);
            return createFallback(layout, "Missing component for: " + layout.getTypeUri());
          }
        catch (Throwable e)
          {
            log.warn("Internal error", e);
            return createFallback(layout, "Error");
          }
      }

    @Nonnull
    protected ViewAndController createFallback (@Nonnull Layout layout, @Nonnull String message)
      {
        return new ViewAndController(fallbackViewSupplier.apply(layout, message), VOID_CONTROLLER);
      }
  }
