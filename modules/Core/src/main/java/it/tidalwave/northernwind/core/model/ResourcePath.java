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
package it.tidalwave.northernwind.core.model;

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

/***********************************************************************************************************************
 *
 * This class encapsulate a path, that is a sequence of segments separated by a "/", and provides methods to manipulate
 * it.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @EqualsAndHashCode
public class ResourcePath
  {
    @Nonnull
    /* package */ final ImmutableList<String> segments;

    /*******************************************************************************************************************
     *
     * Creates an empty path, that is "/".
     *
     ******************************************************************************************************************/
    public ResourcePath()
      {
        this(ImmutableList.<String>of());
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
        this((path.equals("/") | path.equals("")) ? ImmutableList.<String>of()
                                                  : ImmutableList.<String>copyOf(validated(path).split("/")));
      }

    /*******************************************************************************************************************
     *
     * Creates an instance out of a collection of segments.
     *
     * @param  segments  the segments
     *
     ******************************************************************************************************************/
    /* package */ ResourcePath (final @Nonnull ImmutableList<String> segments)
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
    public ResourcePath relativeTo (final @Nonnull ResourcePath path)
      {
        if (!segments.subList(0, path.segments.size()).equals(path.segments))
          {
            throw new IllegalArgumentException("The path " + path.asString() + " doesn't start with " + asString());
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
        return new ResourcePath(segments.subList(1, segments.size()));
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
        return new ResourcePath(segments.subList(0, segments.size() - 1));
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the leading segment of this path is the given one.
     *
     * @param  leadingSegment  the expected leading segment
     * @return                 {@code true} if this path starts with the given leading segment
     *
     ******************************************************************************************************************/
    public boolean startsWith (final @Nonnull String leadingSegment)
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
     * Returns a clone with the given prepended path. For instance, if this object represents "/foo/bar/", and
     * "baz", "bax" are given as argument, the returned clone represents "/baz/bax/foo/bar".
     *
     * @param   path    the path to prepend
     * @return          the clone
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourcePath prependedWith (final @Nonnull ResourcePath path)
      {
        return new ResourcePath(new ImmutableList.Builder<String>().addAll(path.segments).addAll(segments).build());
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
    public ResourcePath prependedWith (final @Nonnull String path)
      {
        return prependedWith(new ResourcePath(path));
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
    public ResourcePath appendedWith (final @Nonnull ResourcePath path)
      {
        return new ResourcePath(new ImmutableList.Builder<String>().addAll(segments).addAll(path.segments).build());
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
    public ResourcePath appendedWith (final @Nonnull String path)
      {
        return appendedWith(new ResourcePath(path));
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
        final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();

        for (final String segment : segments)
          {
            try
              {
                builder.add(URLDecoder.decode(segment, "UTF-8"));
              }
            catch (UnsupportedEncodingException e)
              {
                throw new RuntimeException(e);
              }
          }

        return new ResourcePath(builder.build());
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

        // FIXME: this check is probably redundant now that there are safety tests
        if (buffer.toString().contains("//"))
          {
            throw new RuntimeException("Error in stringification: " + buffer + " - " + this);
          }

        return buffer.toString();
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
    private static String validated (final @Nonnull String path)
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
    private static ImmutableList<String> validated (final @Nonnull ImmutableList<String> segments)
      {
        for (final String segment : segments)
          {
            if (segment.equals(""))
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
