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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import it.tidalwave.util.spring.ClassScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.*;

/***********************************************************************************************************************
 *
 * An implementation of {@link MetadataInterpolatorFactory} that scannes the class path for candidates.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ScanningMetadataInterpolatorFactory implements MetadataInterpolatorFactory
  {
    @Getter
    private final List<MetadataInterpolator> interpolators = new ArrayList<>();
            
    @PostConstruct
    public void initialize()
      {
        interpolators.clear();
        log.info("Scanning for metadata interpolators...");
        
        final ClassScanner scanner = new ClassScanner().withIncludeFilter(new TypeFilter() 
          {
            // FIXME: doesn't check through the whole hierarchy
            @Override
            public boolean match (final @Nonnull MetadataReader metadataReader, 
                                  final @Nonnull MetadataReaderFactory metadataReaderFactory)
              {
                final List<String> interfaceNames = asList(metadataReader.getClassMetadata().getInterfaceNames());
                final String superClassName = metadataReader.getClassMetadata().getSuperClassName();
                
                return interfaceNames.contains(MetadataInterpolator.class.getName()) ||
                       superClassName.equals(MetadataInterpolatorSupport.class.getName());
              }
          });
        
        for (final Class<?> clazz : scanner.findClasses())
          {
            try 
              {
                interpolators.add((MetadataInterpolator)clazz.newInstance());
                log.info(">>>> added metadata interpolator: {}", clazz);
              } 
            catch (InstantiationException | IllegalAccessException e) 
              {
                log.warn("Couldn't add metadata interpolator: " + clazz, e);
              }
          }
      }
  }
