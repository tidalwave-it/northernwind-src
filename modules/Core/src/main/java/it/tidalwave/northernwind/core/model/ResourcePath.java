/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model;

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import it.tidalwave.northernwind.util.UrlEncoding;
import lombok.EqualsAndHashCode;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.util.CollectionFunctions.concat;

/***********************************************************************************************************************
 *
 * This class encapsulate a path, that is a sequence of segments separated by a "/", and provides methods to manipulate
 * it.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Immutable @EqualsAndHashCode
public class ResourcePath implements Serializable
  {
    private static final long serialVersionUID = 1L;

    public static final ResourcePath EMPTY = new ResourcePath();

    @Nonnull
    /* package */ final List<String> segments;

    /*******************************************************************************************************************
     *
     * Creates an instance out of a string.
     *
     * @param  path     the path as string
     * @return          the path
     *
     ******************************************************************************************************************/
    @Nonnull
    public static ResourcePath of (@Nonnull final String path)
      {
        return new ResourcePath(path);
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a list of segments.
     *
     * @param  segments     the path as a sequence of segments
     * @return                  the path
     *
     ******************************************************************************************************************/
    @Nonnull
    public static ResourcePath of (@Nonnull final List<String> segments)
      {
        return new ResourcePath(segments);
      }

    /*******************************************************************************************************************
     *
     * Creates an empty path, that is "/".
     *
     ******************************************************************************************************************/
    private ResourcePath()
      {
        this(emptyList());
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a string.
     *
     * @param  path  the path
     *
     ******************************************************************************************************************/
    private ResourcePath (@Nonnull final String path)
      {
        this(("/".equals(path) || "".equals(path)) ? emptyList() : List.of(validated(path).split("/")));
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a collection of segments.
     *
     * @param  segments  the segments
     *
     ******************************************************************************************************************/
    /* package */ ResourcePath (@Nonnull final List<String> segments)
      {
        this.segments = validated(segments);
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
    public ResourcePath relativeTo (@Nonnull final ResourcePath path)
      {
        if (!segments.subList(0, path.segments.size()).equals(path.segments))
          {
            throw new IllegalArgumentException("The path " + path.asString() + " doesn't start with " + asString());
          }

        return ResourcePath.of(segments.subList(path.segments.size(), segments.size()));
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
     * Returns a segment of this path. For instance, if the current object represents "/foo/bar/baz",
     * {@code getSegment(1)} returns "baz"..
     *
     * @param   index   the index of the segment
     * @return  the     segment
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getSegment (@Nonnegative final int index)
      {
        return segments.get(index);
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
        final String trailing = getTrailing();
        return !trailing.contains(".") ? "" : trailing.replaceAll("^.*\\.", "");
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
        return ResourcePath.of(segments.subList(1, segments.size()));
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
        return ResourcePath.of(segments.subList(0, segments.size() - 1));
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the leading segment of this path is the given one.
     *
     * @param  leadingSegment  the expected leading segment
     * @return                 {@code true} if this path starts with the given leading segment
     *
     ******************************************************************************************************************/
    public boolean startsWith (@Nonnull final String leadingSegment)
      {
        return !segments.isEmpty() && getLeading().equals(leadingSegment);
      }

    /*******************************************************************************************************************
     *
     * Returns the count of segments in this path.
     *
     * @return  the count of segments
     *
     ******************************************************************************************************************/
    @Nonnegative
    public int getSegmentCount()
      {
        return segments.size();
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if this paths is empty.
     *
     * @return  {@code true} if the path is empty
     *
     ******************************************************************************************************************/
    @Nonnegative
    public boolean isEmpty()
      {
        return segments.isEmpty();
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given prepended path. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/baz/bax/foo/bar".
     *
     * @param   path    the path to prepend
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath prependedWith (@Nonnull final ResourcePath path)
      {
        return ResourcePath.of(concat(path.segments, this.segments));
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given prepended path. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/baz/bax/foo/bar".
     *
     * @param   path    the path to prepend
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath prependedWith (@Nonnull final String path)
      {
        return prependedWith(ResourcePath.of(path));
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given appended path. For instance, if this object represents "/foo/bar/", and
     * "/baz/bax" is given as argument, the returned clone represents "/foo/bar/baz/bax".
     *
     * @param   path    the path to prepend
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath appendedWith (@Nonnull final ResourcePath path)
      {
        return ResourcePath.of(concat(this.segments, path.segments));
      }

    /*******************************************************************************************************************
     *
     * Returns a clone with the given appended path. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/foo/bar/baz/bax".
     *
     * @param   path    the path to prepend
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath appendedWith (@Nonnull final String path)
      {
        return appendedWith(ResourcePath.of(path));
      }

    /*******************************************************************************************************************
     *
     * Returns a URL-decoded clone.
     *
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath urlDecoded()
      {
        return ResourcePath.of(segments.stream().map(UrlEncoding::decodedUtf8).collect(toList()));
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
        final String string = segments.stream().collect(joining("/", "/", ""));

        // FIXME: this check is probably redundant now that there are safety tests
        if (string.contains("//"))
          {
            throw new RuntimeException("Error in stringification: " + string + " - " + this);
          }

        return string;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        try
          {
            return asString();
          }
        catch (RuntimeException e)
          {
            return segments.toString();
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String validated (@Nonnull final String path)
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
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<String> validated (@Nonnull final List<String> segments)
      {
        for (final String segment : segments)
          {
            if ("".equals(segment))
              {
                throw new IllegalArgumentException("Empty segment in " + segments);
              }

            if (segment.contains("/"))
              {
                throw new IllegalArgumentException("Segments cannot contain a slash: " + segments);
              }
          }

        return segments;
      }
  }
