/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Configurable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable
public class ParameterLanguageOverrideLinkPostProcessor implements LinkPostProcessor
  {
    @Inject
    private ParameterLanguageOverrideRequestProcessor plorp;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String postProcess (final @Nonnull String link)
      {
        // Ask to the request processor, not to the LocaleManager, because we need to replicate an explicitly set
        // language in the request, only if present.
        return plorp.getParameterValue().map(s -> postProcess(link, s)).orElse(link);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    public String postProcess (final @Nonnull String link, final @Nonnull String language)
      {
        final String parameterName = plorp.getParameterName();
        final String regexp = "([\\?&])(" + parameterName + "=[a-z,0-9]*)";

        final Matcher matcher = Pattern.compile(regexp).matcher(link);

        if (matcher.find()) // replace a parameter already present
          {
            final StringBuffer buffer = new StringBuffer();
            matcher.appendReplacement(buffer, matcher.group(1) + parameterName + "=" + language);
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

        builder.append(parameterName).append("=").append(language);

        return builder.toString();
      }
  }
