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
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
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
public abstract class NodeViewBuilderVisitorSupport<COMPONENT, CONTAINER> implements Visitor<Layout, COMPONENT>
  {
    @Nonnull
    protected final SiteNode siteNode;

    private COMPONENT rootComponent;

    private Stack<COMPONENT> components = new Stack<>();

    @Override
    public void preVisit (final @Nonnull Layout layout)
      {
        final COMPONENT component = createComponent(layout);

        if (rootComponent == null)
          {
            rootComponent = component;
          }
        else
          {
            attach((CONTAINER)components.peek(), component);
          }

        components.push(component);
      }

    @Override
    public void visit (final @Nonnull Layout layout)
      {
      }

    @Override
    public void postVisit (final @Nonnull Layout layout)
      {
        components.pop();
      }

    @Override @Nonnull
    public COMPONENT getValue()
      {
        return rootComponent;
      }

    @Nonnull
    private COMPONENT createComponent (@Nonnull Layout layout)
      {
        try
          {
            return (COMPONENT)layout.createViewAndController(siteNode).getView();
          }
        catch (NotFoundException e)
          {
            log.warn("Component not found", e);
            return createPlaceHolderComponent(layout, "Missing component: " + layout.getTypeUri());
          }
        catch (HttpStatusException e)
          {
            // FIXME: should set the status in the response - unfortunately at this level the ResponseHolder is abstract
            log.warn("Returning HTTP status {}", e.getHttpStatus());
            String message = "<h1>Status " + e.getHttpStatus() + "</h1>"; // FIXME: use a resource bundle

            if (e.getHttpStatus() == 404)
              {
                message = "<h1>Not found</h1>";
              }

            return createPlaceHolderComponent(layout, message);
          }
        catch (Throwable e)
          {
            log.warn("Internal error", e);
            return createPlaceHolderComponent(layout, "Error");
          }
      }

    @Nonnull
    protected abstract COMPONENT createPlaceHolderComponent (@Nonnull Layout layout, @Nonnull String message);

    protected abstract void attach (@Nonnull CONTAINER parent, @Nonnull COMPONENT child);
  }
