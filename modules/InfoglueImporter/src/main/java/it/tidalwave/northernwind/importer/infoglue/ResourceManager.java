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

import it.tidalwave.util.NotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
public class ResourceManager 
  {
    public static final File hgFolder = new File("target/root");      
    
    private static final SortedMap<DateTime, List<AddResourceCommand>> commandMapByDateTime = new TreeMap<DateTime, List<AddResourceCommand>>();
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addAndCommitResources() 
      throws Exception
      {        
        for (final List<AddResourceCommand> resources : commandMapByDateTime.values())
          {
              // FIXME: first add all of them with the same timestamp, then commit all of them in a single round?
            for (final AddResourceCommand resource : resources)
              {
                resource.addAndCommit();  
              }
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
    public static void initialize() 
      throws Exception 
      {
        hgFolder.mkdirs();
        Utilities.exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg init");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void tagConversionCompleted() 
      throws Exception 
      {
        Utilities.exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg tag converted");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static byte[] findRecentContents (final @Nonnull String path) 
      throws NotFoundException
      {
        byte[] result = null;
        
        for (final List<AddResourceCommand> resources : commandMapByDateTime.values())
          {
            for (final AddResourceCommand resource : resources)
              {
                if (resource.getPath().equals(path))
                  {
                    result = resource.getContents();  
                  }
              }
          }
        
        return NotFoundException.throwWhenNull(result, path);
      }
  }
