/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.role.Unmarshallable;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.core.impl.io.jaxb.ComponentJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.ComponentsJaxb;
import javax.xml.bind.JAXBException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @DciRole(datumType = Layout.class) @ToString @Slf4j
public class LayoutJaxbUnmarshallable implements Unmarshallable
  {
    @Inject
    private ModelFactory modelFactory;

    @Inject
    private Unmarshaller unmarshaller;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public LayoutJaxbUnmarshallable (final @Nonnull Layout layout)
      {
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public Layout unmarshal (final @Nonnull InputStream is)
      throws IOException
      {
        try
          {
            final ComponentsJaxb componentsJaxb = ((JAXBElement<ComponentsJaxb>)unmarshaller.unmarshal(is)).getValue();

            if (!"1.0".equals(componentsJaxb.getVersion()))
              {
                throw new IOException("Unexpected version: " + componentsJaxb.getVersion());
              }

            return unmarshal(componentsJaxb.getComponent());
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
    private Layout unmarshal (final @Nonnull ComponentJaxb componentJaxb)
      {
        Layout layout = modelFactory.createLayout().withId(new Id(componentJaxb.getId()))
                                                   .withType(componentJaxb.getType())
                                                   .build();

        for (final ComponentJaxb childComponentJaxb : componentJaxb.getComponent())
          {
            layout = layout.withChild(unmarshal(childComponentJaxb));
          }

        return layout;
      }
  }
