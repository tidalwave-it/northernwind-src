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
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.impl.model.DefaultResourceProperties;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.ObjectFactory;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.PropertiesType;
import it.tidalwave.northernwind.frontend.impl.model.io.jaxb.PropertyType;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResourcePropertiesJaxbUnmarshallable implements Unmarshallable<ResourceProperties>
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties unmarshal (final @Nonnull InputStream is, 
                                         final @Nonnull DefaultResourceProperties.PropertyResolver propertyResolver) 
      throws IOException
      {
        try
          {
            final Unmarshaller unmarshaller = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName()).createUnmarshaller();
            final PropertiesType propertiesType = ((JAXBElement<PropertiesType>)unmarshaller.unmarshal(is)).getValue();
            
            if (!"1.0".equals(propertiesType.getVersion()))
              {
                throw new IOException("Unexpected version: " + propertiesType.getVersion());  
              }
            
            return unmarshal(propertiesType, propertyResolver);
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
    private ResourceProperties unmarshal (final @Nonnull PropertiesType propertiesType,
                                          final @Nonnull DefaultResourceProperties.PropertyResolver propertyResolver) 
      {
        final Id id = new Id((propertiesType.getId() != null) ? propertiesType.getId() : "");
        DefaultResourceProperties properties = new DefaultResourceProperties(id, propertyResolver);
       
        for (final PropertyType property : propertiesType.getProperty())
          {
            properties = properties.withProperty(new Key<Object>(property.getName()), property.getValue().get(0));
          }
        
        for (final PropertiesType propertiesType2 : propertiesType.getProperties())
          {
            properties = properties.withProperties(unmarshal(propertiesType2, propertyResolver));
          }
        
        return properties;
      }  
  }
