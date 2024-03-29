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
package it.tidalwave.northernwind.frontend.media.impl.interpolator;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import it.tidalwave.util.spring.ClassScanner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * An implementation of {@link MetadataInterpolatorFactory} that scans the class path for candidates.
 *
 * @author  Fabrizio Giudici
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

        // FIXME: doesn't check through the whole hierarchy
        final var scanner = new ClassScanner().withIncludeFilter((metadataReader, metadataReaderFactory) ->
          {
            final var interfaceNames = List.of(metadataReader.getClassMetadata().getInterfaceNames());
            final var superClassName = metadataReader.getClassMetadata().getSuperClassName();

            return interfaceNames.contains(MetadataInterpolator.class.getName()) ||
                   ((superClassName != null) && superClassName.equals(MetadataInterpolatorSupport.class.getName()));
          });

        for (final var clazz : scanner.findClasses())
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
