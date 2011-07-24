/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import it.tidalwave.util.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * @author fritz
 */
@RequiredArgsConstructor @Getter @ToString
public class Component 
  {
    @Nonnull
    private final String name;
    
    @Nonnull
    private final String type;
    
    private final List<Component> subComponents = new ArrayList<Component>();
    
    private final Map<String, Component> subComponentMapByName = new HashMap<String, Component>();
    
    public void add (final @Nonnull Component component)
      {
        subComponents.add(component);
        subComponentMapByName.put(component.getName(), component);
      }
    
    @Nonnull
    public Component findSubComponentByName (final @Nonnull String name)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(subComponentMapByName.get(name), "Can't find " + name);
      }            
  }
