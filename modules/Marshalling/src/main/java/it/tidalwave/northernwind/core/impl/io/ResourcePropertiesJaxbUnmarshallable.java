/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.role.Unmarshallable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.impl.io.jaxb.PropertiesJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.PropertyJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.ValuesJaxb;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@DciRole(datum = ResourceProperties.class) @Configurable
public class ResourcePropertiesJaxbUnmarshallable implements Unmarshallable
  {
    @Nonnull
    private final ResourceProperties resourceProperties;
    
    @Inject @Nonnull
    private Unmarshaller unmarshaller;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public ResourcePropertiesJaxbUnmarshallable (final @Nonnull ResourceProperties resourceProperties) 
      {
        this.resourceProperties = resourceProperties;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties unmarshal (final @Nonnull InputStream is) 
      throws IOException
      {
        try
          {
            final PropertiesJaxb propertiesJaxb = ((JAXBElement<PropertiesJaxb>)unmarshaller.unmarshal(is)).getValue();
            
            if (!"1.0".equals(propertiesJaxb.getVersion()))
              {
                throw new IOException("Unexpected version: " + propertiesJaxb.getVersion());  
              }
            
            return unmarshal(propertiesJaxb);
          }
        catch (JAXBException e)
          {
            throw new IOException("", e);
          }         
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceProperties unmarshal (final @Nonnull PropertiesJaxb propertiesJaxb) 
      {
        final Id id = new Id((propertiesJaxb.getId() != null) ? propertiesJaxb.getId() : "");
        ResourceProperties properties = resourceProperties.withId(id); // FIXME: use ModelFactory
       
        for (final PropertyJaxb propertyJaxb : propertiesJaxb.getProperty())
          {
            final ValuesJaxb values = propertyJaxb.getValues();
            properties = properties.withProperty(new Key<>(propertyJaxb.getName()), 
                                                (values != null) ? values.getValue() : propertyJaxb.getValue());
          }
        
        for (final PropertiesJaxb propertiesJaxb2 : propertiesJaxb.getProperties())
          {
            properties = properties.withProperties(unmarshal(propertiesJaxb2));
          }
        
        return properties;
      }  
  }
