/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.importer.infoglue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ContentMap
  {
    private Map<Integer, SortedMap<DateTime, Map<String, String>>> map =
            new HashMap<Integer, SortedMap<DateTime, Map<String, String>>>();

    public void put (final int id,
                     final @Nonnull DateTime dateTime,
                     final @Nonnull String language,
                     final @Nonnull String content)
      {
        getLanguageMap(id, dateTime, true).put(language, content);
      }

    @Nonnull
    public Map<String, String> get (final int id, final @Nonnull DateTime dateTime)
      {
        return getLanguageMap(id, dateTime, false);
      }

    @Nonnull
    private Map<String, String> getLanguageMap (final int id, final @Nonnull DateTime dateTime, final boolean exactDateTime)
      {
        SortedMap<DateTime, Map<String, String>> dateTimeMap = map.get(id);

        if (dateTimeMap == null)
          {
            map.put(id, dateTimeMap = new TreeMap<DateTime, Map<String, String>>());
          }

        Map<String, String> languageMap = dateTimeMap.get(dateTime);

        if (languageMap == null)
          {
            if (exactDateTime)
              {
                dateTimeMap.put(dateTime, languageMap = new HashMap<String, String>());
              }
            else
              {
                for (final Entry<DateTime, Map<String, String>> entry : dateTimeMap.entrySet())
                  {
                    if (entry.getKey().isAfter(dateTime))
                      {
                        break;
                      }

                    languageMap = entry.getValue();
                  }

                if (languageMap == null)
                  {
                    throw new RuntimeException("Cannot find dateTime earlier than: " + dateTime + " - available: " + dateTimeMap.keySet());
                  }
              }
          }

        if (languageMap.isEmpty() && !exactDateTime) // FIXME: drop this log, useless
          {
            log.error("Empty language map for {}: {}", "" + id + " / " + dateTime, dateTimeMap.keySet());
          }

        return languageMap;
      }
  }
