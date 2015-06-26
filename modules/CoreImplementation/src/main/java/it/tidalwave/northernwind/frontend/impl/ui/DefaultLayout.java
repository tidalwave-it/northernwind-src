/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.Parameters;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.ui.LayoutFinder;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
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
    private final As asSupport = new AsSupport(this);

    // FIXME: make it Immutable

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    static class CloneVisitor extends VisitorSupport<Layout, DefaultLayout>
      {
        private DefaultLayout rootLayout;

        private Stack<DefaultLayout> layouts = new Stack<>();

        @Override
        public void preVisit (final @Nonnull Layout layout)
          {
            final DefaultLayout clone = new DefaultLayout(((DefaultLayout)layout).id,
                                                          ((DefaultLayout)layout).typeUri);

            if (rootLayout == null)
              {
                rootLayout = clone;
              }
            else
              {
                layouts.peek().add(clone);
              }

            layouts.push(clone);
          }

        @Override
        public void postVisit (final @Nonnull Layout layout)
          {
            layouts.pop();
          }

        @Override @Nonnull
        public DefaultLayout getValue()
          {
            return rootLayout;
          }
      };

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
    public DefaultLayout (final @Nonnull Id id, final @Nonnull String typeUri)
      {
        Parameters.checkNonNull(id, "id");
        Parameters.checkNonNull(typeUri, "typeUri");
        this.id = id;
        this.typeUri = typeUri;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public DefaultLayout (final @Nonnull Builder builder)
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
        try
          {
            return accept(new CloneVisitor());
          }
        catch (NotFoundException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout withOverride (final @Nonnull Layout override)
      {
        final DefaultLayout result = clone();
        result.applyOverride(((DefaultLayout)override).clone());
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout withChild (final @Nonnull Layout layout)
      {
        final DefaultLayout clone = clone();
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
    public void add (final @Nonnull Layout layout) // FIXME: drop this - used only by the CloneVisitor and Infoglue converter
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
    public ViewAndController createViewAndController (final @Nonnull SiteNode siteNode)
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
    public <Type> Type accept (final @Nonnull Visitor<Layout, Type> visitor)
      throws NotFoundException
      {
        visitor.preVisit(this);
        visitor.visit(this);

        for (final Layout child : children)
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
    private void applyOverride (final @Nonnull Layout override)
      {
        final boolean sameType = this.getTypeUri().equals(override.getTypeUri());
        this.typeUri = override.getTypeUri(); // FIXME: don't like this approach, as it requires typeUri non final

        // Complex rule, but it's to keep compatibility with Infoglue.
        if (sameType)
          {
            for (final Layout overridingChild : override.findChildren().results())
              {
                final Layout overriddenChild = childrenMapById.get(overridingChild.getId());

                if (overriddenChild == null)
                  {
                    add(overridingChild);
                  }
                else
                  {
                    childrenMapById.put(overridingChild.getId(), overridingChild);
                    final int i = children.indexOf(overriddenChild);

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

            for (final Layout overridingChild : override.findChildren().results())
              {
                add(overridingChild);
              }
          }
      }
  }
