/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.profiling.impl;

import javax.annotation.Nonnull;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import it.tidalwave.northernwind.core.model.Request;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * FIXME: move to the Profiling module
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
//@Aspect FIXME: it's not working
@Slf4j
public class ProfilerAspect
  {
    @Around("execution(* it.tidalwave.northernwind.frontend.ui.spi.DefaultSiteViewController.processRequest(..))")
    public Object advice (final @Nonnull ProceedingJoinPoint pjp)
      throws Throwable
      {
        final long time = System.currentTimeMillis();
        final Request request = (Request)pjp.getArgs()[0];
        final Object result = pjp.proceed();
        log.info(">>>> {} completed in {} msec", request, System.currentTimeMillis() - time);
        // FIXME: retrieve the mime type, create statistics (global and by mime type)

        return result;
      }
  }
