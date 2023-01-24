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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.util.NotFoundException;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Implements the default inheritance policy for properties. The sequence of loaded file is:
 *
 * <ul>
 * <li>parent default</li>
 * <li>parent localized</li>
 * <li>this level default</li>
 * <li>this level localized</li>
 * <li>override default</li>
 * <li>override localized</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultInheritanceHelper implements InheritanceHelper
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<ResourceFile> getInheritedPropertyFiles (@Nonnull final ResourceFile folder,
                                                         @Nonnull final Locale locale,
                                                         @Nonnull final String propertyFileName)
      {
        log.trace("getInheritedPropertyFiles({}, {})", folder.getPath(), propertyFileName);

        final List<String> suffixes = new ArrayList<>();
        suffixes.add("");
        suffixes.add("_" + locale.getLanguage());
        final List<ResourceFile> files = new ArrayList<>();

        for (final var parentFolder : getHierarchyFolders(folder))
          {
            files.addAll(getFiles("", propertyFileName, parentFolder, suffixes));
          }

        files.addAll(getFiles("Override", propertyFileName, folder, suffixes));

        log.trace(">>>> property file candidates: {}", files);

        return files;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static List<ResourceFile> getFiles (@Nonnull final String prefix,
                                                @Nonnull final String propertyFileName,
                                                @Nonnull final ResourceFile folder,
                                                @Nonnull final List<String> suffixes)
      {
        final List<ResourceFile> files = new ArrayList<>();

        for (final var localeSuffix : suffixes)
          {
            final var fileName = prefix + propertyFileName + localeSuffix + ".xml";
            log.trace(">>>> probing {} ...", folder.getPath().asString() + "/" + fileName);

            try
              {
                files.add(folder.findChildren().withName(fileName).result());
              }
            catch (NotFoundException e)
              {
                // ok. do nothing
              }
          }

        return files;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ResourceFile> getHierarchyFolders (@Nonnull final ResourceFile folder)
      {
        final List<ResourceFile> folders = new ArrayList<>();

        for (var parent = folder; parent.getParent() != null; parent = parent.getParent()) // TODO: refactor with recursion?
          {
            folders.add(parent);
          }

        Collections.reverse(folders);

        return folders;
      }
  }
