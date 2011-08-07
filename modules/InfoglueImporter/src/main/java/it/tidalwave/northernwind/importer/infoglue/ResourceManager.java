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
    private static final SortedMap<DateTime, List<AddResourceCommand>> commandMapByDateTime = new TreeMap<DateTime, List<AddResourceCommand>>();
    
    private static final List<AddResourceCommand> media = new ArrayList<AddResourceCommand>();
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addAndCommitResources() 
      throws Exception
      {        
        for (final List<AddResourceCommand> resources : commandMapByDateTime.values())
          {
              // FIXME: first add all of them with the same timestamp, then commit all of them in a single round
            for (final AddResourceCommand resource : resources)
              {
                resource.addAndCommit();  
              }
          }
        
        for (final AddResourceCommand resource : media) // FIXME: when they have a timestamp, manage like the others
          {
            resource.addAndCommit();  
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addCommand (final AddResourceCommand command)
      {
        List<AddResourceCommand> commands = commandMapByDateTime.get(command.getDateTime());
        
        if (commands == null)
          {
            commands = new ArrayList<AddResourceCommand>();
            commandMapByDateTime.put(command.getDateTime(), commands);
          }
        
        commands.add(command);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addMedia (final AddResourceCommand resource)
      {
        media.add(resource);
      }
  }
