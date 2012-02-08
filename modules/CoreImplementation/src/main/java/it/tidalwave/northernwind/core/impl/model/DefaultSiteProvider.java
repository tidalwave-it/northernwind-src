/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.openide.util.NbBundle;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultSiteProvider implements SiteProvider
  {
    @Inject @Nonnull
    private ApplicationContext applicationContext;
    
    @Getter @Setter @Nonnull
    private String documentPath = "content/document";

    @Getter @Setter @Nonnull
    private String mediaPath = "content/media";

    @Getter @Setter @Nonnull
    private String libraryPath = "content/library";

    @Getter @Setter @Nonnull
    private String nodePath = "structure";

    @Getter @Setter
    private boolean logConfigurationEnabled = false;
    
    @Getter @Setter @Nonnull
    private String localesAsString;
            
    @Getter @Setter @Nonnull
    private String ignoredFoldersAsString = "";
    
    @CheckForNull
    private DefaultSite site;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Site getSite() 
      throws NotFoundException 
      {
        initialize();
        return NotFoundException.throwWhenNull(site, "no site available");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void reset()
      throws NotFoundException, IOException 
      {
        log.info("reset()");
        String contextPath = "/";
        
        try
          {
            contextPath = applicationContext.getBean(ServletContext.class).getContextPath();
          }
        catch (NoSuchBeanDefinitionException e)
          {
            log.warn("Running in a non-web environment, set contextPath = {}", contextPath);
          }  
        
        // TODO: use ModelFactory
        site = new DefaultSite(contextPath, documentPath, mediaPath, libraryPath, nodePath, logConfigurationEnabled, localesAsString, ignoredFoldersAsString);
        try 
          {
            site.initialize();
          }
        catch (PropertyVetoException e)
          {
            throw new RuntimeException(e);
          } 
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getVersionString()
      {
        return NbBundle.getMessage(DefaultSiteProvider.class, "NorthernWind.version");
      }
    
    private boolean initialized = false;
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    /* package */ void initialize()
      {
        if (initialized)
          {
            return;  
          }
        
        initialized = true;
        log.info("initialize()");
                
        try 
          {
            reset();
          }
        catch (NotFoundException e)
          {
            log.error("During initialization", e);
          }
        catch (IOException e)
          {
            log.error("During initialization", e);
          }
      }
  }
