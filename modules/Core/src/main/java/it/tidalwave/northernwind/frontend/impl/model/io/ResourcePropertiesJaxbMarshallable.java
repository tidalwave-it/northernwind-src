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
import javax.inject.Inject;
import java.util.TreeSet;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.ObjectFactory;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.PropertiesType;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.PropertyType;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class ResourcePropertiesJaxbMarshallable implements Marshallable
  {
    @Nonnull
    private final ResourceProperties resourceProperties;
    
    @Inject @Nonnull
    private ObjectFactory objectFactory;

    @Inject @Nonnull
    private Marshaller marshaller;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public ResourcePropertiesJaxbMarshallable (final @Nonnull ResourceProperties resourceProperties) 
      {
        this.resourceProperties = resourceProperties;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void marshal (final @Nonnull OutputStream os) 
      throws IOException
      {
        try 
          {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // FIXME: set in Spring
            marshaller.marshal(objectFactory.createProperties(marshal(resourceProperties)), os);
          }
        catch (Exception e) 
          {
            throw new IOException("", e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private PropertiesType marshal (final @Nonnull ResourceProperties properties) 
      throws IOException
      {
        final PropertiesType propertiesType = objectFactory.createPropertiesType();        
        final Id id = properties.getId();
        
        if (id.stringValue().equals(""))
          {
            propertiesType.setVersion("1.0");
          }
        else
          {
            propertiesType.setId(id.stringValue());
          }
        
        for (final Key<?> key : new TreeSet<Key<?>>(properties.getKeys()))
          {
            try
              {
                final PropertyType propertyType = objectFactory.createPropertyType();
                propertyType.setName(key.stringValue());
                propertyType.getValue().add(properties.getProperty(key).toString());
                propertiesType.getProperty().add(propertyType);
              }
            catch (NotFoundException e)
              {
                // never occurs  
                log.error("", e);
              }
          }
        
        for (final Id groupId : new TreeSet<Id>(properties.getGroupIds()))
          {
            try 
              {
                propertiesType.getProperties().add(marshal(properties.getGroup(groupId)));
              }
            catch (NotFoundException e) 
              {
                // never occurs  
                log.error("", e);
              }
          }
        
        return propertiesType;
     }
  }