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
package it.tidalwave.northernwind.importer.infoglue;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ContentMap 
  {
    private Map<Integer, SortedMap<DateTime, Map<String, String>>> map = new HashMap<Integer, SortedMap<DateTime, Map<String, String>>>();
    
    public void put (final int id, final @Nonnull DateTime dateTime, final @Nonnull String language, final @Nonnull String content)
      {
        getLanguageMap(id, dateTime).put(language, content);
      }
    
    @Nonnull
    public Map<String, String> get (final int id, final @Nonnull DateTime dateTime)
      {
        return getLanguageMap(id, dateTime);
      }
    
    @Nonnull
    private Map<String, String> getLanguageMap (final int id, final @Nonnull DateTime dateTime)
      {
        SortedMap<DateTime, Map<String, String>> dateTimeMap = map.get(id);
        
        if (dateTimeMap == null)
          { 
            map.put(id, dateTimeMap = new TreeMap<DateTime, Map<String, String>>());  
          }
        
        Map<String, String> languageMap = dateTimeMap.get(dateTime);
        
        if (languageMap == null)
          {
            dateTimeMap.put(dateTime, languageMap = new HashMap<String, String>());
          }
        
        return languageMap;
      }
  }
