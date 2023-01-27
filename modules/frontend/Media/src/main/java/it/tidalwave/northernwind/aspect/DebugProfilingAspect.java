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
package it.tidalwave.northernwind.aspect;

import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Configurable;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Aspect @Slf4j
public class DebugProfilingAspect 
  {
//    @Around("execution(@it.tidalwave.northernwind.aspect.DebugProfiling * *(..))") FIXME: requires aspectj plugin 1.5
    @Around("execution(* it.tidalwave.northernwind.frontend.media.impl.EmbeddedMediaMetadataProvider.getMetadataString(..))")
    public Object advice (@Nonnull final ProceedingJoinPoint pjp)
      throws Throwable
      {
        log.debug("getMetadataString({})", pjp.getArgs());
        final var time = System.currentTimeMillis();
        
        final var result = pjp.proceed();
        
        if (log.isDebugEnabled())
          {
            final var annotation = getAnnotation(pjp, DebugProfiling.class);
            log.debug(">>>> {} in {} msec", annotation.message(), System.currentTimeMillis() - time);
          }

        return result;
      }
    
    // See http://stackoverflow.com/questions/2559255/spring-aop-how-to-get-the-annotations-of-the-adviced-method
    @Nonnull
    private static <T extends Annotation> T getAnnotation (@Nonnull final ProceedingJoinPoint pjp,
                                                           @Nonnull final Class<T> annotationClass)
      throws NoSuchMethodException
      {
        final var methodSignature = (MethodSignature)pjp.getSignature();
        var method = methodSignature.getMethod();
        
        if (method.getDeclaringClass().isInterface()) // FIXME && annotation inheritance -- FIXME also ancestor class
          {
            final var methodName = pjp.getSignature().getName();
            method = pjp.getTarget().getClass().getDeclaredMethod(methodName, method.getParameterTypes());    
          }          
        
        return method.getAnnotation(annotationClass);
      }
  }
