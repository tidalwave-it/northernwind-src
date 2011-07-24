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
public class Layout 
  {
    @Nonnull
    private final String name;
    
    @Nonnull
    private final String type;
    
    private final List<Layout> children = new ArrayList<Layout>();
    
    private final Map<String, Layout> childrenMapByName = new HashMap<String, Layout>();
    
    public void add (final @Nonnull Layout layout)
      {
        children.add(layout);
        childrenMapByName.put(layout.getName(), layout);
      }
    
    @Nonnull
    public Layout findSubComponentByName (final @Nonnull String name)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(childrenMapByName.get(name), "Can't find " + name);
      }            
  }
