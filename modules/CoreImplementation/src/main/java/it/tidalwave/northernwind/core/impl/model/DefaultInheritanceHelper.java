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
import java.util.Locale;
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
                                                         final @Nonnull Locale locale,
                                                         final @Nonnull String propertyFileName)
      {
        log.trace("getInheritedPropertyFiles({}, {})", folder.getPath(), propertyFileName);

        final List<String> suffixes = new ArrayList<>();
        suffixes.add("");
        suffixes.add("_" + locale.getLanguage());
        final List<ResourceFile> files = new ArrayList<>();

        // Sequence of inheritance
        // parent default
        // parent localized
        // this default
        // this localized
        // override default
        // override localized
        for (final ResourceFile parentFolder : getHierarchyFolders(folder))
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
    private List<ResourceFile> getFiles (final @Nonnull String prefix,
                                         final @Nonnull String propertyFileName,
                                         final @Nonnull ResourceFile folder,
                                         final @Nonnull List<String> suffixes)
      {
        final List<ResourceFile> files = new ArrayList<>();

        for (final String localeSuffix : suffixes)
          {
            final String fileName = prefix + propertyFileName + localeSuffix + ".xml";
            log.trace(">>>> probing {} ...", folder.getPath().asString() + "/" + fileName);
            final ResourceFile propertyFile = folder.getChildByName(fileName);

            if (propertyFile != null)
              {
                files.add(propertyFile);
              }
          }

        return files;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<ResourceFile> getHierarchyFolders (final @Nonnull ResourceFile folder)
      {
        final List<ResourceFile> folders = new ArrayList<>();

        for (ResourceFile parent = folder; parent.getParent() != null; parent = parent.getParent()) // TODO: refactor with recursion?
          {
            folders.add(parent);
          }

        Collections.reverse(folders);

        return folders;
      }
  }
