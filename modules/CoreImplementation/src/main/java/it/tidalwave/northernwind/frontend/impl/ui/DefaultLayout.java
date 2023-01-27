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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.LayoutFinder;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Getter;
import lombok.experimental.Delegate;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Getter
public class DefaultLayout implements Layout, Cloneable
  {
    @Inject
    private ViewFactory viewFactory;

    @Nonnull
    private final Id id;

    @Nonnull
    private /* final FIXME */ String typeUri;

    private final List<Layout> children = new ArrayList<>();

    private final Map<Id, Layout> childrenMapById = new HashMap<>();

    @Delegate
    private final As asSupport = As.forObject(this);

    // FIXME: make it Immutable

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    static class CloneVisitor implements Visitor<Layout, DefaultLayout>
      {
        private DefaultLayout rootLayout;

        private final Deque<DefaultLayout> layoutStack = new ArrayDeque<>();

        @Override
        public void preVisit (@Nonnull final Layout layout)
          {
            final var clone = new DefaultLayout(((DefaultLayout)layout).id,
                                                ((DefaultLayout)layout).typeUri);

            if (rootLayout == null)
              {
                rootLayout = clone;
              }
            else
              {
                layoutStack.peek().add(clone);
              }

            layoutStack.push(clone);
          }

        @Override
        public void postVisit (@Nonnull final Layout layout)
          {
            layoutStack.pop();
          }

        @Override @Nonnull
        public Optional<DefaultLayout> getValue()
          {
            return Optional.of(rootLayout);
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultLayout()
      {
        this.id = new Id("");
        this.typeUri = "";
      }

    /** Clone - FIXME: public only for InfoglueImporter */
    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultLayout (@Nonnull final Id id, @Nonnull final String typeUri)
      {
        this.id = id;
        this.typeUri = typeUri;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultLayout (@Nonnull final Builder builder)
      {
        this.id = builder.getId();
        this.typeUri = builder.getType();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultLayout clone()
      {
        return accept(new CloneVisitor()).orElseThrow(RuntimeException::new);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout withOverride (@Nonnull final Layout override)
      {
        final var result = clone();
        result.applyOverride(((DefaultLayout)override).clone());
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout withChild (@Nonnull final Layout layout)
      {
        final var clone = clone();
        clone.children.add(layout);
        clone.childrenMapById.put(layout.getId(), layout);

        return clone;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public LayoutFinder findChildren()
      {
        return new DefaultLayoutFinder(children, childrenMapById);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    public void add (@Nonnull final Layout layout) // FIXME: drop this - used only by the CloneVisitor and Infoglue converter
      {
        children.add(layout); // FIXME: clone
        childrenMapById.put(layout.getId(), layout);// FIXME: clone
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ViewAndController createViewAndController (@Nonnull final SiteNode siteNode)
            throws NotFoundException, HttpStatusException
      {
        return viewFactory.createViewAndController(typeUri, id, siteNode);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull // TODO: push up to CompositeSupport
    public <T> Optional<T> accept (@Nonnull final Visitor<? super Layout, T> visitor)
      {
        visitor.preVisit(this);
        visitor.visit(this);

        for (final var child : children)
          {
            child.accept(visitor);
          }

        visitor.postVisit(this);
        return visitor.getValue();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return String.format("DefaultLayout(id=%s, typeUri=%s, children.count=%d)", id, typeUri, children.size());
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    // Here everything is already cloned
    private void applyOverride (@Nonnull final Layout override)
      {
        final var sameType = this.getTypeUri().equals(override.getTypeUri());
        this.typeUri = override.getTypeUri(); // FIXME: don't like this approach, as it requires typeUri non final

        // Complex rule, but it's to keep compatibility with Infoglue.
        if (sameType)
          {
            for (final var overridingChild : override.findChildren().results())
              {
                final var overriddenChild = childrenMapById.get(overridingChild.getId());

                if (overriddenChild == null)
                  {
                    add(overridingChild);
                  }
                else
                  {
                    childrenMapById.put(overridingChild.getId(), overridingChild);
                    final var i = children.indexOf(overriddenChild);

                    if (i < 0)
                      {
                        throw new IllegalArgumentException();
                      }

                    children.set(i, overridingChild);
                    //                    ((DefaultLayout)overriddenChild).applyOverride(overridingChild);
                  }
              }
          }
        else
          {
            this.children.clear();
            this.childrenMapById.clear();

            for (final var overridingChild : override.findChildren().results())
              {
                add(overridingChild);
              }
          }
      }
  }
