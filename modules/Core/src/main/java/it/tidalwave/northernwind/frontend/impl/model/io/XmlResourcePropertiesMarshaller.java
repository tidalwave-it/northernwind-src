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
package it.tidalwave.northernwind.frontend.impl.model.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class XmlResourcePropertiesMarshaller implements Marshaller
  {
    @Nonnull
    private final ResourceProperties resourceProperties;
    
    @Override
    public void marshal (final @Nonnull OutputStream os) 
      throws IOException
      {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        marshal(pw, resourceProperties);
        pw.flush();
        
        try 
          {
            os.write(Utilities.dumpXml(sw.toString()));
          }
        catch (Exception e) 
          {
            throw new IOException("", e);
          }
      }
        
    private void marshal (final @Nonnull PrintWriter pw, final @Nonnull ResourceProperties properties) 
      throws IOException
      {
        final Id id = properties.getId();
        
        if (id.stringValue().equals(""))
          {
            pw.printf("<properties version='1.0'>\n");
          }
        else
          {
            pw.printf("<properties id='%s'>\n", id.stringValue());
          }
        
        for (final Key<?> key : properties.getKeys())
          {
            try
              {
                final String value = properties.getProperty(key).toString().replace("&", "&amp;");
                pw.printf("<property name='%s'><value>%s</value></property>\n", key.stringValue(), value);
              }
            catch (NotFoundException e)
              {
                // never occurs  
                log.error("", e);
              }
          }
        
        for (final Id groupId : properties.getGroupIds())
          {
            try 
              {
                marshal(pw, properties.getGroup(groupId));
              }
            catch (NotFoundException e) 
              {
                // never occurs  
                log.error("", e);
              }
          }
        
        pw.println("</properties>");
      }
  }
