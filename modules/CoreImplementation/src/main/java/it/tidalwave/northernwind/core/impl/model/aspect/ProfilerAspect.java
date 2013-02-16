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
package it.tidalwave.northernwind.core.impl.model.aspect;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
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
@Configurable @Aspect @Slf4j
public class ProfilerAspect
  {
    @Inject
    private BeanFactory factory;

    private StatisticsCollector statisticsCollector;

    @PostConstruct
    public void initialize()
      {
        // FIXME: workaround as the aspect doesn't work in a separate module. When it works, you can use plain @Inject
        try
          {
            statisticsCollector = factory.getBean(StatisticsCollector.class);
          }
        catch (NoSuchBeanDefinitionException e)
          {
          }
      }

    @Around("execution(* it.tidalwave.northernwind.frontend.ui.spi.DefaultSiteViewController.processRequest(..))")
    public Object advice (final @Nonnull ProceedingJoinPoint pjp)
      throws Throwable
      {
        final Request request = (Request)pjp.getArgs()[0];

        if (statisticsCollector != null)
          {
            statisticsCollector.onRequestBegin(request);
          }

        final Object result = pjp.proceed();

        if (statisticsCollector != null)
          {
            statisticsCollector.onRequestEnd(request);
          }

        return result;
      }
  }
