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
package it.tidalwave.northernwind.frontend.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import it.tidalwave.util.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @AllArgsConstructor(access=PRIVATE) @Getter @ToString
public class Request 
  {
    @Nonnull
    private final String relativeUri;
    
    private final Map<String, List<String>> parametersMap = new HashMap<String, List<String>>();
    
    @Nonnull
    public static Request request()
      {
        return new Request("");  
      }
    
    @Nonnull
    public static Request requestFrom (final @Nonnull HttpServletRequest httpServletRequest)
      {
        final String relativeUri = "/" + httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length() + 1);
        return request().withRelativeUri(relativeUri)
                        .withParameterMap(httpServletRequest.getParameterMap()); 
      }
    
    @Nonnull
    public String getParameter (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return getMultiValuedParameter(parameterName).get(0);
      }
    
    @Nonnull
    public List<String> getMultiValuedParameter (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(parametersMap.get(parameterName), parameterName);
      }
    
    @Nonnull
    public Request withRelativeUri (final @Nonnull String relativeUri)
      {
        return new Request(relativeUri);     
      }
    
    private Request withParameterMap (final @Nonnull Map<String, String[]> parameterMap)
      {
        final Request request = new Request(relativeUri);
        
        for (final Entry<String, String[]> entry : parameterMap.entrySet())
          {
            request.parametersMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
          }
        
        return request;
      }
  }
