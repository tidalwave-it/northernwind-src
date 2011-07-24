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

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor @Slf4j @ToString
/* package */ class ViewBuilder
  {
    @Nonnull
    private final String typeUri;  

    @Nonnull
    private final Class<?> viewClass;  

    @Nonnull
    private final Class<?> viewControllerClass;  
    
    /*******************************************************************************************************************
     *
     * Validates the configured classes.
     * 
     * @throws  Exception   if the view and/or controller classes are not valid
     *
     ******************************************************************************************************************/
    public void validate() 
      throws NoSuchMethodException
      {
        getConstructor();
      }
    
    /*******************************************************************************************************************
     *
     * Creates a new View - ViewController pair.
     *
     * @param   id        the instance id
     * @param   siteNode  the {@link SiteNode} the view will be built for
     * @return            the created view
     *
     ******************************************************************************************************************/
    @Nonnull
    public Object createView (final @Nonnull String id, final @Nonnull SiteNode siteNode)
      {
        log.debug("createView({}, {})", id, siteNode);
        
        try
          { 
            final Object view = viewClass.getConstructor(String.class).newInstance(id);
            // FIXME: the viewController is not assigned, will be GCed!
            // FIXME: Attach to the view, even though it doesn't need it? Or use a WeakIdentityMap indexed by the View?
            final Object viewController = getConstructor().newInstance(view, id, siteNode);  
            return view;
          }
        catch (Exception e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Constructor<?> getConstructor() 
      throws NoSuchMethodException, SecurityException 
      {
        return viewControllerClass.getConstructor(viewClass.getInterfaces()[0], String.class, SiteNode.class);
      }
  }
