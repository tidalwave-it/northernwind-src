/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.regex.Matcher;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.RequestContext;
import static org.springframework.core.Ordered.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE)
public class NodePropertyResolverMacroFilter extends MacroFilter
  {
    @Inject @Nonnull
    private Provider<RequestContext> context;

    public NodePropertyResolverMacroFilter()
      {
        super("\\$nodeProperty\\(name='([^']*)'\\)\\$");
      }

    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      {
        try
          {
            final String propertyName = matcher.group(1);
            return context.get().getNodeProperties().getProperty(new Key<String>(propertyName), "");
          }
        catch (IOException e)
          {
            return "ERR";
          }
      }
  }
