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
package it.tidalwave.northernwind.frontend.impl.ui;

import it.tidalwave.northernwind.core.model.spi.Marshallable;
import it.tidalwave.role.annotation.RoleImplementation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
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
