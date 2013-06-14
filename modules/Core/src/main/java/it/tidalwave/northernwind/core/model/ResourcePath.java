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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @ToString @EqualsAndHashCode
public class ResourcePath
  {
    private final List<String> segments;

    public ResourcePath()
      {
        this(Collections.<String>emptyList());
      }

    private static String x (final @Nonnull String path)
      {
        if (path.startsWith("http:") || path.startsWith("https:"))
          {
            throw new IllegalArgumentException("ResourcePath can't hold a URL");
          }

        final int start = path.startsWith("/") ? 1 : 0;
        return path.substring(start);
      }

    public ResourcePath (final @Nonnull String path)
      {
        this(Arrays.asList(x(path).split("/")));
      }

    private ResourcePath (final @Nonnull List<String> segments)
      {
        this.segments = new ArrayList<>(segments);

        if (this.segments.size() > 0 && this.segments.get(0).equals("")) // FIXME
          {
            this.segments.remove(0);
          }

        for (final String segment : this.segments)
          {
            if ("".equals(segment))
              {
                throw new IllegalArgumentException("Empty element in " + this);
              }
          }
      }

    @Nonnull
    public ResourcePath relativeTo (final @Nonnull ResourcePath uri)
      {
        if (!segments.subList(0, uri.segments.size()).equals(uri.segments))
          {
            throw new IllegalArgumentException("The path doesn't start with " + uri.asString() + ": " + asString());
          }

        return new ResourcePath(segments.subList(uri.segments.size(), segments.size()));
      }

    @Nonnull
    public String getLeading()
      {
        return segments.get(0);
      }

    @Nonnull
    public ResourcePath withoutLeading()
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.remove(0);
        return new ResourcePath(temp);
      }

    @Nonnull
    public String getTrailing()
      {
        return segments.get(segments.size() - 1);
      }

    @Nonnull
    public ResourcePath withoutTrailing()
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.remove(temp.size() - 1);
        return new ResourcePath(temp);
      }

    public boolean startsWith (final @Nonnull String string)
      {
        return !segments.isEmpty() && segments.get(0).equals(string);
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
    public ResourcePath prependedWith (final @Nonnull String ... strings)
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.addAll(0, Arrays.asList(strings));
        return new ResourcePath(temp);
      }

    @Nonnull
    public ResourcePath with (final @Nonnull ResourcePath path)
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.addAll(path.segments);
        return new ResourcePath(temp);
      }

    @Nonnull
    public ResourcePath with (final @Nonnull String ... strings)
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.addAll(Arrays.asList(strings));
        return new ResourcePath(temp);
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

        if (buffer.toString().contains("//"))
          {
            throw new RuntimeException("Error in stringification: " + buffer + " - " + this);
          }

        return buffer.toString();
      }
  }
