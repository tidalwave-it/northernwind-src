/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.util;

import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

/***********************************************************************************************************************
 *
 * A specialization of {@link TreeMap} that is capable to deal with regular expressions. When a value must be bound to
 * a regular expression, use the method {@link #putRegex(java.lang.String, java.lang.Object)} instead of
 * {@link #put(java.lang.String, java.lang.Object)}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class RegexTreeMap<Type> extends TreeMap<String, Type>
  {
    private final static long serialVersionUID = 576876596539246L;

    @Nonnull
    public static String escape (final @Nonnull String string)
      {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < string.length(); i++)
          {
            final char c = string.charAt(i);

            if ("[\\^$.|?*+()".contains("" + c))
              {
                builder.append('\\');
              }

            builder.append(c);
          }

        return builder.toString();
      }

    @Override
    public Type put (final @Nonnull String string, final Type value)
      {
        return super.put(Pattern.quote(string), value);
      }

    public Type putRegex (final @Nonnull String regex, final Type value)
      {
        return super.put(regex, value);
      }

    @Override
    public Type get (final @Nonnull Object value)
      {
        final String stringValue = (String)value;
        Type result = super.get(Pattern.quote(stringValue)); // first try a direct match that is fast
        int matchLength = 0;

        if (result == null) // otherwise returns the longest match
          {
            for (final Entry<String, Type> entry : super.entrySet())
              {
                final String regex = entry.getKey();

                if (stringValue.matches(regex) && (regex.length() > matchLength))
                  {
                    result = entry.getValue();
                    matchLength = regex.length();
                  }
              }
          }

        return result;
      }
  }
