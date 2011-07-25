/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
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
              // FIXME: first add all of them, then commit all of them
            for (final Resource resource : resources)
              {
                resource.addAndCommit();  
              }
          }
        
        for (final Resource resource : media)
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
