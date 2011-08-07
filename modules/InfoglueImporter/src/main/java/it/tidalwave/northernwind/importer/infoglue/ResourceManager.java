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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResourceManager 
  {
    private static final SortedMap<DateTime, List<Resource>> resourceMapByDateTime = new TreeMap<DateTime, List<Resource>>();
    
    private static final List<Resource> media = new ArrayList<Resource>();
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addAndCommitResources() 
      throws Exception
      {        
        for (final List<Resource> resources : resourceMapByDateTime.values())
          {
              // FIXME: first add all of them with the same timestamp, then commit all of them in a single round
            for (final Resource resource : resources)
              {
                resource.addAndCommit();  
              }
          }
        
        for (final Resource resource : media) // FIXME: when they have a timestamp, manage like the others
          {
            resource.addAndCommit();  
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addResource (final Resource resource)
      {
        List<Resource> resources = resourceMapByDateTime.get(resource.getDateTime());
        
        if (resources == null)
          {
            resources = new ArrayList<Resource>();
          }
        
        resources.add(resource);
        resourceMapByDateTime.put(resource.getDateTime(), resources);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addMedia (final Resource resource)
      {
        media.add(resource);
      }
  }
