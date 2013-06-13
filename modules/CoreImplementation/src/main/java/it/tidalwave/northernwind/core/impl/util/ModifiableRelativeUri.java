/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @ToString
public class ModifiableRelativeUri
  {
    private final List<String> parts;

    public ModifiableRelativeUri()
      {
        parts = new ArrayList<>();
      }

    public ModifiableRelativeUri (final @Nonnull String relativeUri)
      {
        final int start = relativeUri.startsWith("/") ? 1 : 0;
        parts = new ArrayList<>(Arrays.asList(relativeUri.substring(start).split("/")));

        if (parts.get(0).equals("")) // FIXME
          {
            parts.remove(0);
          }
      }

    @Nonnull
    public String popLeading()
      {
        return parts.remove(0);
      }

    @Nonnull
    public void popLeading (final @Nonnull ModifiableRelativeUri uri)
      {
        if (!parts.subList(0, uri.parts.size()).equals(uri.parts))
          {
            throw new IllegalArgumentException("The path doesn't start with " + uri.asString() + ": " + asString()
                    + " ZZZ " + uri.parts + " " + parts);
          }

        final List<String> temp = new ArrayList<>(parts.subList(uri.parts.size(), parts.size()));
        parts.clear();
        parts.addAll(temp);
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

    public void append (final @Nonnull ModifiableRelativeUri relativeUri)
      {
        parts.addAll(relativeUri.parts);
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

        return buffer.toString().equals("") ? "/" : buffer.toString(); // FIXME
      }
  }
