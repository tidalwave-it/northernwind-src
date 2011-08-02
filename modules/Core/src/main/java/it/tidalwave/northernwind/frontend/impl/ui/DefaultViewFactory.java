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
import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.util.spring.ClassScanner;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ViewFactory}.
 * 
 * @stereotype  Factory
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString
public class DefaultViewFactory implements ViewFactory
  {
    private final Map<String, ViewBuilder> viewBuilderMapByTypeUri = new TreeMap<String, ViewBuilder>();
    
    @Getter @Setter
    private boolean logConfigurationEnabled = false;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Object createView (final @Nonnull String typeUri, 
                              final @Nonnull Id id, 
                              final @Nonnull SiteNode siteNode)
      throws NotFoundException
      {        
        final ViewBuilder viewBuilder = NotFoundException.throwWhenNull(viewBuilderMapByTypeUri.get(typeUri),
                                                                        "Cannot find " + typeUri + ": available: " + viewBuilderMapByTypeUri.keySet());
        return viewBuilder.createView(id, siteNode);
      }
     
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct 
    /* package */ void initialize() // FIXME: gets called twice
      throws IOException, 
             NoSuchMethodException, InvocationTargetException, InstantiationException, 
             IllegalArgumentException, IllegalAccessException, SecurityException 
      {
        final ClassScanner classScanner = new ClassScanner();
        classScanner.addIncludeFilter(new AnnotationTypeFilter(ViewMetadata.class));
        
        for (final Class<?> viewClass : classScanner.findClasses())
          {
            final ViewMetadata viewMetadata = viewClass.getAnnotation(ViewMetadata.class);
            final String typeUri = viewMetadata.typeUri();
            final ViewBuilder viewBuilder = new ViewBuilder(viewClass, viewMetadata.controlledBy());
            viewBuilderMapByTypeUri.put(typeUri, viewBuilder);
          }
        
        if (logConfigurationEnabled)
          {
            logConfiguration();
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void logConfiguration()
      {
        log.info("View definitions:");
        
        for (final ViewBuilder viewDefinition : viewBuilderMapByTypeUri.values())
          {
            log.info(">>>> {}", viewDefinition);
          }
      }
  }
