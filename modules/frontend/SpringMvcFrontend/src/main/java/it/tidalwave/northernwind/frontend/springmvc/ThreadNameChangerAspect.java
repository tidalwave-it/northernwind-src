/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Aspect
public class ThreadNameChangerAspect
  {
    private int counter;

    @Around("execution(* it.tidalwave.northernwind.frontend.springmvc.SpringMvcRestController.get(..))")
    public Object advice (@Nonnull final ProceedingJoinPoint pjp)
      throws Throwable
      {
        final var thread = Thread.currentThread();
        final var saveName = thread.getName();

        try
          {
            final var request = (HttpServletRequest)pjp.getArgs()[0];
            thread.setName(String.format("%s-%d", request.getRemoteAddr(), counter++));
            return pjp.proceed();
          }
        finally
          {
            thread.setName(saveName);
          }
      }
  }
