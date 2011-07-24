/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author fritz
 */
@RequiredArgsConstructor
public class ComponentXmlMarshaller
  {
    @Nonnull
    private final Component component;
    
    public void marshall (final @Nonnull PrintWriter pw)
      throws IOException
      {
        marshall(pw, component, "");
      } 
    
    private void marshall (final @Nonnull PrintWriter pw, final @Nonnull Component component, final @Nonnull String leading)
      throws IOException
      {
        pw.printf("%s<component id='%s' type='%s'>\n", leading, component.getName(), component.getType());
        
        for (final Component child : component.getSubComponents())
          {
            marshall(pw, child, leading + "  ");                
          }
        
        pw.printf("%s</component>\n", leading);
      }
  }
