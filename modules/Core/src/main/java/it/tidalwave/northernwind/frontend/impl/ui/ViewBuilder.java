/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.impl.ui;

import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.model.SiteNode;
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
@Slf4j @ToString
/* package */ class ViewBuilder
  {
    @Nonnull
    private final Constructor<?> viewConstructor;
    
    @Nonnull
    private final Constructor<?> viewControllerConstructor;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public ViewBuilder (final @Nonnull Class<?> viewClass, final @Nonnull Class<?> viewControllerClass)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, 
             IllegalArgumentException, IllegalAccessException, SecurityException 
      {
        viewConstructor = viewClass.getConstructor(Id.class);
        viewControllerConstructor = viewControllerClass.getConstructor(viewClass.getInterfaces()[0], Id.class, SiteNode.class);
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
      {
        log.debug("createView({}, {})", id, siteNode);
        
        try
          { 
            // Note that the viewController is not assigned to any object. Indeed, it might be GCed sooner or later.
            // But it's not a problem: if it's not referenced, it will no more useful (in contrast, e.g. a view
            // would bind itself to a controller listener or action in cases when the controller plays a later role). 
            final Object view = viewConstructor.newInstance(id);
            viewControllerConstructor.newInstance(view, id, siteNode);  
            return view;
          }
        catch (Exception e)
          {
            throw new RuntimeException(e);
          }
      }
  }
