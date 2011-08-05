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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.io.UnsupportedEncodingException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.impl.util.UriUtilities;
import it.tidalwave.northernwind.core.model.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Request}
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @AllArgsConstructor @Getter @ToString
/* package */ class DefaultRequest implements Request
  {
    @Nonnull
    private final String relativeUri;
    
    @Nonnull
    private final Map<String, List<String>> parametersMap;
        
    @Nonnull
    private final List<Locale> preferredLocales;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getParameter (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return getMultiValuedParameter(parameterName).get(0);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<String> getMultiValuedParameter (final @Nonnull String parameterName)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(parametersMap.get(parameterName), parameterName);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultRequest withRelativeUri (final @Nonnull String relativeUri)
      {
//        try 
//          {
            return new DefaultRequest(relativeUri, parametersMap, preferredLocales);
//            return new DefaultRequest(UriUtilities.urlDecodedPath(relativeUri), parametersMap, preferredLocales);
//          }
//        catch (UnsupportedEncodingException e)
//          {
//            throw new RuntimeException(e);
//          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withParameterMap (final @Nonnull Map<String, String[]> httpParameterMap)
      {
        final Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
        
        for (final Entry<String, String[]> entry : httpParameterMap.entrySet())
          {
            parameterMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
          }
        
        return new DefaultRequest(relativeUri, parameterMap, preferredLocales);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public DefaultRequest withPreferredLocales (final @Nonnull List<Locale> preferredLocales)
      {
        return new DefaultRequest(relativeUri, parametersMap, preferredLocales);  
      }
  }
