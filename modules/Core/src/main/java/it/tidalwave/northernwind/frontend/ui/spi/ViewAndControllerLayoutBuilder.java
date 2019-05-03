/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.Optional;
import java.util.function.BiFunction;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.ViewController;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * A {@link Layout} visitor that instantiate views and controllers for a given {@code Layout}. The whole {@code Layout}
 * tree is visited and for each child an instance of {@link ViewAndController} is created, the controller being called
 * with the {@link ViewController#prepareRendering(it.tidalwave.northernwind.frontend.ui.ViewController.RenderContext)}
 * method. No rendering is performed, but instances of {@code ViewAndController} are stored for later retrieval by means
 * of the method {@link #getViewAndControllerFor(it.tidalwave.northernwind.frontend.ui.Layout)}.
 *
 * @stereotype  Visitor
 * @author      Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @RequiredArgsConstructor
public class ViewAndControllerLayoutBuilder extends VisitorSupport<Layout, ViewAndControllerLayoutBuilder>
  {
    private static final ViewController VOID_CONTROLLER = new ViewController() {};

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final RenderContext renderContext;

    @Getter @Nonnull
    private final BiFunction<Layout, Throwable, Object> errorViewSupplier;

    private final Map<Layout, ViewAndController> viewAndControllerMapByLayout = new IdentityHashMap<>();

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override
    public void visit (final @Nonnull Layout layout)
      {
        try
          {
            final ViewAndController vac = createComponent(layout);
            vac.getController().prepareRendering(renderContext);
            viewAndControllerMapByLayout.put(layout, vac);
          }
        catch (Exception e)
          {
            viewAndControllerMapByLayout.put(layout, createErrorView(layout, e));
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ViewAndControllerLayoutBuilder getValue()
      {
        return this;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<ViewAndController> getViewAndControllerFor (final @Nonnull Layout layout)
      {
        return Optional.ofNullable(viewAndControllerMapByLayout.get(layout));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ViewAndController createComponent (@Nonnull Layout layout)
      {
        try
          {
            return layout.createViewAndController(siteNode);
          }
        catch (NotFoundException e)
          {
            return createErrorView(layout, new NotFoundException("Missing component for: " + layout.getTypeUri()));
          }
        catch (Throwable e)
          {
            return createErrorView(layout, e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private ViewAndController createErrorView (@Nonnull Layout layout, @Nonnull Throwable e)
      {
        return new ViewAndController(errorViewSupplier.apply(layout, e), VOID_CONTROLLER);
      }
  }
