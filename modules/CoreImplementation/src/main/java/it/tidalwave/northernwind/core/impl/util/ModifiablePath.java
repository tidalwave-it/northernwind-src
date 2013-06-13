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
public class ModifiablePath
  {
    private final List<String> segments;

    public ModifiablePath()
      {
        segments = new ArrayList<>();
      }

    public ModifiablePath (final @Nonnull String path)
      {
        final int start = path.startsWith("/") ? 1 : 0;
        segments = new ArrayList<>(Arrays.asList(path.substring(start).split("/")));

        if (segments.get(0).equals("")) // FIXME
          {
            segments.remove(0);
          }
      }

    @Nonnull
    public String popLeading()
      {
        return segments.remove(0);
      }

    @Nonnull
    public ModifiablePath relativeTo (final @Nonnull ModifiablePath uri)
      {
        if (!segments.subList(0, uri.segments.size()).equals(uri.segments))
          {
            throw new IllegalArgumentException("The path doesn't start with " + uri.asString() + ": " + asString());
          }

        final List<String> temp = new ArrayList<>(segments.subList(uri.segments.size(), segments.size()));
        segments.clear();
        segments.addAll(temp);

        return this;
      }

    @Nonnull
    public String popTrailing()
      {
        return segments.remove(segments.size() - 1);
      }

    public boolean startsWith (final @Nonnull String string)
      {
        return segments.get(0).equals(string);
      }

    @Nonnull
    public String getExtension()
      {
        return segments.get(segments.size() - 1).replaceAll("^.*\\.", "");
      }

    @Nonnegative
    public int getPartsCount()
      {
        return segments.size();
      }

    public void prepend (final @Nonnull String ... strings)
      {
        segments.addAll(0, Arrays.asList(strings));
      }

    public void append (final @Nonnull ModifiablePath path)
      {
        segments.addAll(path.segments);
      }

    public void append (final @Nonnull String ... strings)
      {
        segments.addAll(Arrays.asList(strings));
      }

    @Nonnull
    public String asString()
      {
        final StringBuilder buffer = new StringBuilder("/");
        String separator = "";

        for (final String segment : segments)
          {
            buffer.append(separator).append(segment);
            separator = "/";
          }

        return buffer.toString();
      }
  }
