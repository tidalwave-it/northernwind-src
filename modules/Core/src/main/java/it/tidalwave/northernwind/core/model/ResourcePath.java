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

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    /* package */ final List<String> segments;

    public ResourcePath()
      {
        this(Collections.<String>emptyList());
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private static String x (final @Nonnull String path)
      {
        if (path.startsWith("http:") || path.startsWith("https:"))
          {
            throw new IllegalArgumentException("ResourcePath can't hold a URL");
          }

        final int start = path.startsWith("/") ? 1 : 0;
        return path.substring(start);
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a string.
     *
     * @param  path  the path
     *
     ******************************************************************************************************************/
    public ResourcePath (final @Nonnull String path)
      {
        this(Arrays.asList(x(path).split("/")));
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a collection of strings.
     *
     * @param  path  the path
     *
     ******************************************************************************************************************/
    private ResourcePath (final @Nonnull Collection<String> segments)
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

    /*******************************************************************************************************************
     *
     * Returns a clone path which is relative to the given path. For instance, if this is "/foo/bar/baz" and path is
     * "/foo/bar", the returned clone represents "/baz".
     *
     * @param   path                        the path to which we're computing the relative position
     * @return                              the clone
     * @throws  IllegalArgumentException    if path is not a prefix of this
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath relativeTo (final @Nonnull ResourcePath path)
      {
        if (!segments.subList(0, path.segments.size()).equals(path.segments))
          {
            throw new IllegalArgumentException("The path doesn't start with " + path.asString() + ": " + asString());
          }

        return new ResourcePath(segments.subList(path.segments.size(), segments.size()));
      }

    /*******************************************************************************************************************
     *
     * Returns the leading segment of this path. For instance, if the current object represents "/foo/bar/baz", "foo" is
     * returned.
     *
     * @return  the leading segment of this path
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getLeading()
      {
        return segments.get(0);
      }

    /*******************************************************************************************************************
     *
     * Returns a clone without the leading segment. For instance, if the current object represents "/foo/bar/baz",
     * the returned clone represents "/bar/baz".
     *
     * @return  the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath withoutLeading()
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.remove(0);
        return new ResourcePath(temp);
      }

    /*******************************************************************************************************************
     *
     * Returns the trailing segment of this path. For instance, if the current object represents "/foo/bar/baz",
     * "baz" is returned.
     *
     * @return  the trailing segment of this path
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getTrailing()
      {
        return segments.get(segments.size() - 1);
      }

    /*******************************************************************************************************************
     *
     * Returns a clone without the trailing segment. For instance, if the current object represents "/foo/bar/baz",
     * the returned clone represents "/foo/bar".
     *
     * @return  the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath withoutTrailing()
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.remove(temp.size() - 1);
        return new ResourcePath(temp);
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the leading segment of this path is the given one.
     *
     * @param  segment  the expected leading segment
     * @return          {@code true} if this path starts with the given leading segment
     *
     ******************************************************************************************************************/
    public boolean startsWith (final @Nonnull String segment)
      {
        return !segments.isEmpty() && segments.get(0).equals(segment);
      }

    /*******************************************************************************************************************
     *
     * Returns the file extension of this path. For instance, if this object represents "/foo/bar/baz.jpg", "jpg" is
     * returned.
     *
     * @return  the file extension of this path
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getExtension()
      {
        return segments.get(segments.size() - 1).replaceAll("^.*\\.", "");
      }

    /*******************************************************************************************************************
     *
     * Returns the count of segments in this path.
     *
     * @return  the count of segments
     *
     ******************************************************************************************************************/
    @Nonnegative
    public int getPartsCount() // FIXME: rename to getSegmentsCount
      {
        return segments.size();
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given prepended segments. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/baz/bax/foo/bar".
     *
     * @param   the segments to prepend
     * @return  the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath prependedWith (final @Nonnull String ... segments)
      {
        final List<String> temp = new ArrayList<>(this.segments);
        temp.addAll(0, Arrays.asList(segments));
        return new ResourcePath(temp);
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given appended path. For instance, if this object represents "/foo/bar/", and
     * "/baz/bax" is given as argument, the returned clone represents "/foo/bar/baz/bax".
     *
     * @param   the segments to prepend
     * @return  the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath appendedWith (final @Nonnull ResourcePath path)
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.addAll(path.segments);
        return new ResourcePath(temp);
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given appended segments. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/foo/bar/baz/bax".
     *
     * @param   the segments to prepend
     * @return  the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath appendedWith (final @Nonnull String ... strings)
      {
        final List<String> temp = new ArrayList<>(segments);
        temp.addAll(Arrays.asList(strings));
        return new ResourcePath(temp);
      }

    /*******************************************************************************************************************
     *
     * Returns the string representation of this path. This representation always starts with a leading "/" and has no
     * trailing "/". For empty paths "/" is returned.
     *
     * @return  the string representation
     *
     ******************************************************************************************************************/
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
