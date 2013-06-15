/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import it.tidalwave.northernwind.core.model.ResourceFile;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultInheritanceHelper implements InheritanceHelper
  {
    /*******************************************************************************************************************
    *
    ******************************************************************************************************************/
    @Override @Nonnull
    public List<ResourceFile> getInheritedPropertyFiles (final @Nonnull ResourceFile folder,
                                                         final @Nonnull String propertyFileName)
      {
        log.trace("getInheritedPropertyFiles({}, {})", folder.getPath(), propertyFileName);

        final List<ResourceFile> files = new ArrayList<>();

        for (ResourceFile parent = folder; parent.getParent() != null; parent = parent.getParent()) // TODO: refactor with recursion
          {
            log.trace(">>>> probing {} ...", parent.getPath().asString() + "/" + propertyFileName);
            final ResourceFile propertyFile = parent.getChildByName(propertyFileName);

            if (propertyFile != null)
              {
                files.add(propertyFile);
              }
          }

        Collections.reverse(files);

        final ResourceFile propertyFile = folder.getChildByName("Override" + propertyFileName);

        if (propertyFile != null)
          {
            files.add(propertyFile);
          }

        log.trace(">>>> property file candidates: {}", files);

        return files;
      }
  }
