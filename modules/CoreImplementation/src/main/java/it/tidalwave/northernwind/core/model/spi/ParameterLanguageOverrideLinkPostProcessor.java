/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class ParameterLanguageOverrideLinkPostProcessor implements LinkPostProcessor
  {
    @Inject
    private ParameterLanguageOverrideRequestProcessor parameterLanguageOverrideRequestProcessor;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String postProcess (final @Nonnull String link)
      {
        try
          {
            final String parameterValue = parameterLanguageOverrideRequestProcessor.getParameterValue();
            return postProcess(link, parameterValue);
          }
        catch (NotFoundException e)
          {
            return link;
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public String postProcess (final @Nonnull String link, final @Nonnull String parameterValue)
      {
        final String parameterName = parameterLanguageOverrideRequestProcessor.getParameterName();
        final String regexp = "([\\?&])(" + parameterName + "=[a-z,0-9]*)";

        final Matcher matcher = Pattern.compile(regexp).matcher(link);

        if (matcher.find()) // replace a parameter already present
          {
            final StringBuffer buffer = new StringBuffer();
            matcher.appendReplacement(buffer, matcher.group(1) + parameterName + "=" + parameterValue);
            matcher.appendTail(buffer);

            return buffer.toString();
          }

        final StringBuilder builder = new StringBuilder(link);

        if (link.contains("?"))
          {
            builder.append("&");
          }
        else
          {
            if (!builder.toString().endsWith("/") && !builder.toString().contains(".")) // FIXME: check . only in trailing
              {
                builder.append("/");
              }

            builder.append("?");
          }

        builder.append(parameterName).append("=").append(parameterValue);

        return builder.toString();
      }
  }
