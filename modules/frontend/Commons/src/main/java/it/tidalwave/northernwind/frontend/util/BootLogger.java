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
package it.tidalwave.northernwind.frontend.util;

import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/***********************************************************************************************************************
 *
 * We can't log to the real thing, since we first need to compute the path of the logging file. Logging to the real
 * thing would instantiate the logging facility before we have a chance to configure it.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public final class BootLogger
  {
    private static final StringBuilder BUILDER = new StringBuilder();
    
    public static synchronized void log (final @Nonnull String string)
      {
        BUILDER.append(string).append("\n");
      }
    
    @Nonnull
    public static String getLogContent()
      {
        return BUILDER.toString();  
      }
  }