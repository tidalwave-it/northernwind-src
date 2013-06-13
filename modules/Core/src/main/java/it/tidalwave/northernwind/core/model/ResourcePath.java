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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NotThreadSafe @ToString @EqualsAndHashCode
public class ResourcePath implements Cloneable // FIXME: must be Immutable
  {
    private final List<String> segments;

    public ResourcePath()
      {
        segments = new ArrayList<>();
      }

    public ResourcePath (final @Nonnull String path)
      {
        final int start = path.startsWith("/") ? 1 : 0;
        segments = new ArrayList<>(Arrays.asList(path.substring(start).split("/")));

        if (segments.get(0).equals("")) // FIXME
          {
            segments.remove(0);
          }
      }

    @Override @Nonnull
    public ResourcePath clone()
      {
        final ResourcePath clone = new ResourcePath();
        clone.segments.addAll(this.segments);
        return clone;
      }

    @Nonnull
    public String popLeading()
      {
        return segments.remove(0);
      }

    @Nonnull
    public ResourcePath relativeTo (final @Nonnull ResourcePath uri)
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

    @Nonnull
    public ResourcePath prepend (final @Nonnull String ... strings)
      {
        segments.addAll(0, Arrays.asList(strings));
        return this;
      }

    @Nonnull
    public ResourcePath append (final @Nonnull ResourcePath path)
      {
        segments.addAll(path.segments);
        return this;
      }

    @Nonnull
    public ResourcePath append (final @Nonnull String ... strings)
      {
        segments.addAll(Arrays.asList(strings));
        return this;
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



//    public String toString()
//    {
//        throw new RuntimeException();
//    }
  }