/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.frontend.impl.ui;

import it.tidalwave.northernwind.core.model.spi.Marshallable;
import it.tidalwave.role.annotation.RoleImplementation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author fritz
 */
@RoleImplementation(ownerClass=DefaultLayout.class) @RequiredArgsConstructor
public class LayoutXmlMarshallable implements Marshallable // TODO: reimplement with JAXB, rename to LayourJaxbMarshallable
  {
    @Nonnull
    private final DefaultLayout layout;
    
    @Override @Nonnull
    public void marshal (final @Nonnull OutputStream os)
      throws IOException
      {
        final PrintWriter pw = new PrintWriter(os);
        pw.println("<layout>");
        marshal(pw, layout, "");
        pw.println("</layout>");
        pw.flush();
      } 
    
    private void marshal (final @Nonnull PrintWriter pw, final @Nonnull DefaultLayout layout, final @Nonnull String leading)
      throws IOException
      {
        pw.printf("%s<component id='%s' type='%s'>\n", leading, layout.getId(), layout.getTypeUri());
        
        for (final DefaultLayout child : layout.getChildren())
          {
            marshal(pw, child, leading + "  ");                
          }
        
        pw.printf("%s</component>\n", leading);
      }
  }
