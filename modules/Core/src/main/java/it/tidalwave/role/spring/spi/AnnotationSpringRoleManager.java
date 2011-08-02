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
package it.tidalwave.role.spring.spi;

import java.lang.reflect.Constructor;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import it.tidalwave.role.annotation.RoleImplementation;
import it.tidalwave.northernwind.frontend.impl.util.ClassScanner;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class AnnotationSpringRoleManager implements RoleManager
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @Getter @ToString @EqualsAndHashCode @Slf4j
    static class ClassAndRole
      {
        @Nonnull
        private final Class<?> ownerClass;
        
        @Nonnull
        private final Class<?> roleClass;
        
        @Nonnull
        public List<ClassAndRole> getSuper()
          {
            final List<ClassAndRole> result = new ArrayList<ClassAndRole>();
            result.add(this);
            
            if (ownerClass.getSuperclass() != null)
              {
                result.addAll(new ClassAndRole(ownerClass.getSuperclass(), roleClass).getSuper());
              }
            
            for (final Class<?> interfaceClass : ownerClass.getInterfaces())
              {
                result.addAll(new ClassAndRole(interfaceClass, roleClass).getSuper());
              }
            
            return result;
          }
      }
    
    private MultiValueMap<ClassAndRole, Class<?>> roleMapByOwnerClass = new LinkedMultiValueMap<ClassAndRole, Class<?>>();
   
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <RoleType> List<? extends RoleType> findRoles (final @Nonnull Object owner, final @Nonnull Class<RoleType> roleClass)
      {
        log.debug("findRoles({}, {})", owner, roleClass);
        
        final Class<?> ownerClass = owner.getClass();
        final List<RoleType> roles = new ArrayList<RoleType>();
        
        for (final Class<? extends RoleType> roleImplementationClass : findRoleImplementationsFor(ownerClass, roleClass))
          {
            for (final Constructor<?> constructor : roleImplementationClass.getDeclaredConstructors())
              {
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                
                if ((parameterTypes.length == 1) && parameterTypes[0].isAssignableFrom(ownerClass))
                  {
                    try 
                      {
                        roles.add((RoleType)constructor.newInstance(owner));
                      }
                    catch (Exception e) 
                      {
                        log.error("", e);
                      }
                  }
              }
          }
        
        log.debug(">>>> returning: {}", roles);
        
        return roles;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private synchronized <RoleType> List<Class<? extends RoleType>> findRoleImplementationsFor (final Class<?> ownerClass, final Class<RoleType> roleClass)
      {
        final ClassAndRole classAndRole = new ClassAndRole(ownerClass, roleClass);
        final List<Class<?>> implementations = roleMapByOwnerClass.get(classAndRole);
        
        if (implementations != null)
          {
            return (List)implementations;
          }
        
        for (final ClassAndRole classAndRole1 : classAndRole.getSuper())
          {
            final List<Class<?>> implementations2 = roleMapByOwnerClass.get(classAndRole1);

            if (implementations2 != null)
              {
                for (final Class<?> implementation : implementations2)
                  {
                    roleMapByOwnerClass.add(classAndRole, implementation);
                  }
                
                return (List)implementations2;
              }
          }
        
        return Collections.emptyList();
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        final ClassScanner classScanner = new ClassScanner();
        classScanner.addIncludeFilter(new AnnotationTypeFilter(RoleImplementation.class));
        
        for (final Class<?> roleImplementationClass : classScanner.findClasses())
          {
            final RoleImplementation role = roleImplementationClass.getAnnotation(RoleImplementation.class);
            final Class<?> ownerClass = role.ownerClass();
            
            for (final Class<?> roleClass : roleImplementationClass.getInterfaces())
              {
                roleMapByOwnerClass.add(new ClassAndRole(ownerClass, roleClass), roleImplementationClass);
              }
          }
        
        logRoles();
      }
        
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public void logRoles()
      {
        log.info("Configured roles:");
        
        for (final Entry<ClassAndRole, List<Class<?>>> entry : roleMapByOwnerClass.entrySet())
          {
            log.info(">>>>{} -> {}", entry.getKey(), entry.getValue());
          }
      }
  }