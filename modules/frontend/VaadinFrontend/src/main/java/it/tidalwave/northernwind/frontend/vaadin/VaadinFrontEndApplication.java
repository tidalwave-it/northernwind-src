/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.vaadin;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import com.vaadin.Application;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope; // FIXME: can we use javax.inject.Scope?
import it.tidalwave.northernwind.frontend.ui.SiteView;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The Vaadin application for the NorthernWind front end.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Scope(value = "session") @Slf4j
public class VaadinFrontEndApplication extends Application
  {
    private static final long serialVersionUID = 5346346324236215L;

    @Inject
    private SiteView siteView;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void init()
      {
        try
          {
            log.info("Restarting...");
            setMainWindow((Window)siteView);
            setTheme("bluebill");
          }
        catch (Throwable e)
          {
            log.error("", e);
          }
      }
  }
