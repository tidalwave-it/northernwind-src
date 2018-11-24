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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.ViewFactory.ViewAndController;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A builder which creates a View - ViewController pair.
 *
 * @stereotype  Factory
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(exclude = "beanFactory")
/* package */ class ViewBuilder
  {
    @Inject
    private BeanFactory beanFactory;

    @Nonnull
    /* package */ final Constructor<?> viewConstructor;

    @Nonnull
    /* package */ final Constructor<?> viewControllerConstructor;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public ViewBuilder (final @Nonnull Class<?> viewClass, final @Nonnull Class<?> viewControllerClass)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
             IllegalArgumentException, IllegalAccessException, SecurityException
      {
        viewConstructor = viewClass.getConstructors()[0];
        viewControllerConstructor = viewControllerClass.getConstructors()[0];
      }

    /*******************************************************************************************************************
     *
     * Creates a new View - ViewController pair. They are first instantiated, and then dependency injection by means
     * of constructor parameters occur. Injected fields are: the id, the {@link SiteNode}, any service declared in
     * the Spring context, including {@link Site}; furthermore, a reference of the View is injected in the Controller.
     *
     * @param   id        the view id
     * @param   siteNode  the {@link SiteNode} the view will be built for
     * @return            the created view
     *
     ******************************************************************************************************************/
    @Nonnull
    public ViewAndController createViewAndController (final @Nonnull Id id, final @Nonnull SiteNode siteNode)
      throws HttpStatusException
      {
        log.debug("createViewAndController({}, {})", id, siteNode);

        try
          {
            final Object view = viewConstructor.newInstance(computeConstructorArguments(viewConstructor, id, siteNode));
            final Object controller = viewControllerConstructor.newInstance(
                    computeConstructorArguments(viewControllerConstructor, id, siteNode, view));
            return new ViewAndController(view, controller);
          }
        catch (InvocationTargetException e)
          {
            if (e.getCause() instanceof BeanCreationException) // FIXME: cumbersome
              {
                if (e.getCause().getCause() instanceof HttpStatusException)
                  {
                    throw (HttpStatusException)e.getCause().getCause();
                  }
              }

            throw new RuntimeException(e);
          }
        catch (Exception e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     * Computes the argument values for calling the given constructor. They are taken from the current
     * {@link BeanFactory}, with {@code instanceArgs} eventually overriding them.
     *
     * @param  constructor      the constructor
     * @param  overridingArgs   the overriding arguments
     * @return                  the arguments to pass to the constructor
     *
     ******************************************************************************************************************/
    @Nonnull
    private Object[] computeConstructorArguments (final @Nonnull Constructor<?> constructor,
                                                  final @Nonnull Object ... overridingArgs)
      throws NotFoundException
      {
        final List<Object> result = new ArrayList<>();

        x: for (final Class<?> argumentType : constructor.getParameterTypes())
          {
            for (final Object overridingArg : overridingArgs)
              {
                if (argumentType.isAssignableFrom(overridingArg.getClass()))
                  {
                    result.add(overridingArg);
                    continue x;
                  }
              }

            if (Site.class.isAssignableFrom(argumentType))
              {
                result.add(beanFactory.getBean(SiteProvider.class).getSite());
              }
            else
              {
                result.add(beanFactory.getBean(argumentType));
              }
          }

        return result.toArray();
      }
  }
