/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
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
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
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
@Configurable @Slf4j @ToString
/* package */ class ViewBuilder
  {
    @Nonnull
    private final Constructor<?> viewConstructor;
    
    @Nonnull
    private final Constructor<?> viewControllerConstructor;
    
    @Inject @Nonnull
    private BeanFactory beanFactory;

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
     * Creates a new View - ViewController pair.
     *
     * @param   id        the view id
     * @param   siteNode  the {@link SiteNode} the view will be built for
     * @return            the created view
     *
     ******************************************************************************************************************/
    @Nonnull
    public Object createView (final @Nonnull Id id, final @Nonnull SiteNode siteNode) 
      throws HttpStatusException
      {
        log.debug("createView({}, {})", id, siteNode);
        
        try
          { 
            // Note that the viewController is not assigned to any object. Indeed, it might be GCed sooner or later.
            // But it's not a problem: if it's not referenced, it will be no more useful (in contrast, e.g. a view
            // would bind itself to a controller listener or action in cases when the controller plays a later role). 
            final Object view = viewConstructor.newInstance(computeConstructorArguments(viewConstructor, id, siteNode));
            viewControllerConstructor.newInstance(computeConstructorArguments(viewControllerConstructor, id, siteNode, view));  
            return view;
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
      {
        final List<Object> result = new ArrayList<Object>();
        
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
            
            result.add(beanFactory.getBean(argumentType));
          }
        
        return result.toArray();
      }
  }