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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * FIXME: move to the Editor module
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Aspect @Slf4j
public class EditorAspect 
  {
    @Inject @Nonnull
    private SiteProvider siteProvider;
    
    @Inject @Nonnull
    private RequestHolder requestHolder;
    
    // FIXME: shoulnd't depend on Aloha - 
    // use a fixed path /editor.css and use an @include inside it
    @Getter @Setter
    private String cssRelativeUri = "/css/InlineEditor.css";
//    private String cssRelativeUri = "/alohaeditor/0.21.0/aloha/css/aloha.css";
    
    @Getter @Setter
    private String scriptRelativeUri = "/Fragments/InlineEditor";
    
    @Getter @Setter
    private String editParameterName = "edit";
    
    @Around("execution(* it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController.computeScreenCssSection(..))")
    public Object injectEditorCss (final @Nonnull ProceedingJoinPoint pjp) 
      throws Throwable
      {
        String result = (String)pjp.proceed();
        
        if (isEditing())
          {
            result += String.format(DefaultNodeContainerViewController.LINK_RELSTYLESHEET_MEDIASCREEN_HREF, cssRelativeUri); 
          }
        
        return result;
      }
    
    @Around("execution(* it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController.computeInlinedScriptsSection(..))")
    public Object injectEditorScript (final @Nonnull ProceedingJoinPoint pjp) 
      throws Throwable
      {
        String result = (String)pjp.proceed();
        
        if (isEditing())
          {
            try
              {
                final Content script = siteProvider.getSite().find(Content).withRelativePath(scriptRelativeUri).result();
                result += script.getProperties().getProperty(PROPERTY_TEMPLATE);  
              }        
            catch (NotFoundException | IOException e)
              {
                // ok, no script  
              }      
          }
        
        return result;
      }
    
    private boolean isEditing()
      {
        try 
          {  
            requestHolder.get().getParameter(editParameterName);
            return true;
          } 
        catch (NotFoundException e) 
          {
            return false;
          }
      }
  }
