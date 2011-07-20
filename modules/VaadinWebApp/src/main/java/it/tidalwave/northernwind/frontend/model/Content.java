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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class Content 
  {
    @Nonnull
    private final File path;    
    
    @Nonnull
    public <Type> Type get (final @Nonnull String attribute, final @Nonnull Class<Type> type)
      throws IOException
      {
        log.info("get({}, {})", attribute, type);
        final File file = new File(path, attribute);
        log.info(">>>> reading from {}", file.getAbsolutePath());
        @Cleanup final FileReader fr = new FileReader(file);
        final char[] chars = new char[(int)file.length()];
        fr.read(chars);
        fr.close();
        
        return (Type)new String(chars);
      }
  }
