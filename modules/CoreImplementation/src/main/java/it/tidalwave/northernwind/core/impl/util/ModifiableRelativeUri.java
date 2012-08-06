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
package it.tidalwave.northernwind.core.impl.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe
public class ModifiableRelativeUri 
  {
    private final List<String> parts;
    
    public ModifiableRelativeUri (final @Nonnull String relativeUri) 
      {
        if (!relativeUri.startsWith("/"))
          {
            throw new IllegalArgumentException("Relative URI must start with /: was " + relativeUri);
          }
        
        parts = new ArrayList<>(Arrays.asList(relativeUri.substring(1).split("/")));
      } 
    
    @Nonnull
    public String popLeading()
      {
        return parts.remove(0);  
      }
    
    @Nonnull
    public String popTrailing()
      {
        return parts.remove(parts.size() - 1);  
      }
    
    public boolean startsWith (final @Nonnull String string)
      {
        return parts.get(0).equals(string);  
      }
    
    @Nonnull
    public String getExtension()
      {   
        return parts.get(parts.size() - 1).replaceAll("^.*\\.", "");
      }
    
    @Nonnegative
    public int getPartsCount()
      {
        return parts.size();  
      }
    
    public void prepend (final @Nonnull String ... strings)
      {
        parts.addAll(0, Arrays.asList(strings));  
      }
    
    public void append (final @Nonnull String ... strings)
      {
        parts.addAll(Arrays.asList(strings));  
      }
    
    @Nonnull
    public String asString()
      {
        final StringBuilder buffer = new StringBuilder();
        
        for (final String s : parts)
          {
            buffer.append("/").append(s);
          }
        
        return buffer.toString();
      }
  }
