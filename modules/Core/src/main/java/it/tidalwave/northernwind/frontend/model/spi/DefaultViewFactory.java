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
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.impl.util.ClassScanner;
import it.tidalwave.northernwind.frontend.model.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
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
    private final Map<String, ViewBuilder> viewBuilderMapByName = new TreeMap<String, ViewBuilder>();
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Object createView (final @Nonnull String viewName, 
                              final @Nonnull String instanceName, 
                              final @Nonnull String contentRelativeUri)
      throws NotFoundException
      {        
        final ViewBuilder viewBuilder = NotFoundException.throwWhenNull(viewBuilderMapByName.get(viewName),
                                                                        "Cannot find " + viewName + ": available: " + viewBuilderMapByName.keySet());
        return viewBuilder.createView(instanceName, contentRelativeUri);
      }
     
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct 
    private void initialize() // FIXME: gets called twice
      throws IOException
      {
        final ClassScanner classScanner = new ClassScanner();
        classScanner.addIncludeFilter(new AnnotationTypeFilter(ViewMetadata.class));
        
        for (final Class<?> viewClass : classScanner.findClasses())
          {
            final ViewMetadata viewMetadata = viewClass.getAnnotation(ViewMetadata.class);
            final String name = viewMetadata.name();
            viewBuilderMapByName.put(name, new ViewBuilder(name, viewClass, viewMetadata.controlledBy()));
          }
        
        logViewDefinitions();
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void logViewDefinitions()
      {
        log.info("View definitions:");
        
        for (final ViewBuilder viewDefinition : viewBuilderMapByName.values())
          {
            log.info(">>>> {}", viewDefinition);
          }
      }
  }
