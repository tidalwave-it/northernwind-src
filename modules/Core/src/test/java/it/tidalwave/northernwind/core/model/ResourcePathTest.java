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

import java.util.List;
import javax.annotation.Nonnull;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import java.util.Collections;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.testng.annotations.DataProvider;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ResourcePathTest
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_create_an_empty_path()
      {
        final ResourcePath fixture = new ResourcePath();

        assertThat(fixture.segments, is(not(nullValue())));
        assertThat(fixture.segments.isEmpty(), is(true));
        assertThat(fixture.asString(), is("/"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void must_reject_null_segments()
      {
        new ResourcePath(asList("a", null, "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void must_reject_empty_segments()
      {
        new ResourcePath(asList("a", "", "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void must_reject_segments_containing_slash()
      {
        new ResourcePath(asList("a", "b", "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "segmentProvider",
          dependsOnMethods = "must_properly_create_an_empty_path")
    public void must_properly_create_an_empty_path_from_string (final @Nonnull String path,
                                                                final @Nonnull String expectedAsString,
                                                                final @Nonnull List<String> expectedSegments)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.segments, is(not(nullValue())));
        assertThat(fixture.segments, is(expectedSegments));
        assertThat(fixture.asString(), is(expectedAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "invalidPathsProvider",
          dependsOnMethods = "must_properly_create_an_empty_path",
          expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "\\QResourcePath can't hold a URL\\E")
    public void must_reject_invalid_paths (final @Nonnull String invalidPathAsString)
      {
        new ResourcePath(invalidPathAsString);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "asStringProvider",
          dependsOnMethods = { "must_properly_create_an_empty_path", "must_properly_create_an_empty_path_from_string" })
    public void must_properly_compute_asString (final @Nonnull List<String> segments,
                                                final @Nonnull String expectedAsString)
      {
        final ResourcePath fixture = new ResourcePath(segments);

        assertThat(fixture.asString(), is(expectedAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "relativePathProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_relative_paths (final @Nonnull String path,
                                                      final @Nonnull String referencePath,
                                                      final @Nonnull String expectedPathAsString)
      {
        final ResourcePath fixture = new ResourcePath(path);
        final ResourcePath r = new ResourcePath(referencePath);
        final ResourcePath relativePath = fixture.relativeTo(r);

        assertThat(relativePath.asString(), is(expectedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "nonRelativePathProvider",
          dependsOnMethods = "must_properly_compute_asString",
          expectedExceptions = IllegalArgumentException.class)
    public void must_properly_reject_non_relative_paths (final @Nonnull String path,
                                                         final @Nonnull String referencePath)
      {
        final ResourcePath fixture = new ResourcePath(path);
        final ResourcePath r = new ResourcePath(referencePath);
        fixture.relativeTo(r);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "leadingAndTrailingProvider")
    public void must_properly_compute_leading_segment (final @Nonnull String path,
                                                       final @Nonnull String expectedLeadingSegment,
                                                       final @Nonnull String expectedTrailingSegment)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.getLeading(), is(expectedLeadingSegment));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "leadingAndTrailingProvider")
    public void must_properly_compute_trailing_segment (final @Nonnull String path,
                                                       final @Nonnull String expectedLeadingSegment,
                                                       final @Nonnull String expectedTrailingSegment)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.getTrailing(), is(expectedTrailingSegment));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "withoutLeadingAndTrailingProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_without_leading (final @Nonnull String path,
                                                       final @Nonnull String expectedWitoutLeadingPath,
                                                       final @Nonnull String expectedWithoutTrailingPath)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.withoutLeading().asString(), is(expectedWitoutLeadingPath));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "withoutLeadingAndTrailingProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_without_trailing (final @Nonnull String path,
                                                        final @Nonnull String expectedWitoutLeadingPath,
                                                        final @Nonnull String expectedWithoutTrailingPath)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.withoutTrailing().asString(), is(expectedWithoutTrailingPath));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "startsWithProvider")
    public void must_properly_compute_startsWith (final @Nonnull String path,
                                                  final @Nonnull String leadingSegment,
                                                  final @Nonnull boolean expectedResult)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.startsWith(leadingSegment), is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendDataProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_append1 (final @Nonnull String path,
                                       final @Nonnull String appendingPathAsString,
                                       final @Nonnull String expectedAppendedPathAsString,
                                       final @Nonnull String expectedPrependedPathAsString)
      {
        final ResourcePath fixture = new ResourcePath(path);
        final ResourcePath appendingPath = new ResourcePath(appendingPathAsString);

        assertThat(fixture.appendedWith(appendingPath).asString(), is(expectedAppendedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_append2 (final @Nonnull String path,
                                       final @Nonnull String appendingPathAsString,
                                       final @Nonnull String expectedAppendedPathAsString,
                                       final @Nonnull String expectedPrependedPathAsString)
      {
        final ResourcePath fixture = new ResourcePath(path);
        final String[] segments = appendingPathAsString.split("/");

        assertThat(fixture.appendedWith(segments).asString(), is(expectedAppendedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_prepend (final @Nonnull String path,
                                       final @Nonnull String appendingPathAsString,
                                       final @Nonnull String expectedAppendedPathAsString,
                                       final @Nonnull String expectedPrependedPathAsString)
      {
        final ResourcePath fixture = new ResourcePath(path);
        final String[] segments = appendingPathAsString.split("/");

        assertThat(fixture.prependedWith(segments).asString(), is(expectedPrependedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "segmentProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_count_segments (final @Nonnull String path,
                                              final @Nonnull String expectedAsString,
                                              final @Nonnull List<String> expectedSegments)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.getSegmentCount(), is(expectedSegments.size()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "extensionProvider")
    public void must_properly_compute_extension (final @Nonnull String path,
                                                 final @Nonnull String expectedExtension)
      {
        final ResourcePath fixture = new ResourcePath(path);

        assertThat(fixture.getExtension(), is(expectedExtension));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "segmentProvider")
    private Object[][] segmentProvider()
      {
        return new Object[][]
          {
          //  path              exp. asString       exp. segments
            { "/foo",           "/foo",             asList("foo")               },
            { "/foo/bar",       "/foo/bar",         asList("foo", "bar")        },
            { "/foo/bar/baz",   "/foo/bar/baz",     asList("foo", "bar", "baz") },

            { "foo",           "/foo",              asList("foo")               },
            { "foo/bar",       "/foo/bar",          asList("foo", "bar")        },
            { "foo/bar/baz",   "/foo/bar/baz",      asList("foo", "bar", "baz") }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "invalidPathsProvider")
    private Object[][] invalidPathsProvider()
      {
        return new Object[][]
          {
            { "http://acme.com"  },
            { "https://acme.com" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "relativePathProvider")
    private Object[][] relativePathProvider()
      {
        return new Object[][]
          {
          //  path            parent      relative
            { "/foo/bar/baz", "/foo/bar", "/baz" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "nonRelativePathProvider")
    private Object[][] nonRelativePathProvider()
      {
        return new Object[][]
          {
          //  path            parent
            { "/foo/bar/baz", "/foo/bar2" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "leadingAndTrailingProvider")
    private Object[][] leadingAndTrailingProvider()
      {
        return new Object[][]
          {
          // path             leading   trailing
//            { "/",            "", "" }
            { "/foo",         "foo",    "foo" },
            { "/foo/bar",     "foo",    "bar" },
            { "/foo/bar/baz", "foo",    "baz" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "withoutLeadingAndTrailingProvider")
    private Object[][] withoutLeadingAndTrailingProvider()
      {
        return new Object[][]
          {
          // path             w/o leading   w/o trailing
//            { "/",            "", "" }
            { "/foo",         "/",          "/" },
            { "/foo/bar",     "/bar",       "/foo" },
            { "/foo/bar/baz", "/bar/baz",   "/foo/bar" }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "startsWithProvider")
    private Object[][] startsWithProvider()
      {
        return new Object[][]
          {
            { "/foo",         "foo",  true },
            { "/foo/bar",     "foo",  true },
            { "/foo/bar/baz", "foo",  true },

            { "/foo",         "foot", false },
            { "/foo/bar",     "foot", false },
            { "/foo/bar/baz", "foot", false },

            { "/foo",         "",     false }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "asStringProvider")
    private Object[][] asStringProvider()
      {
        return new Object[][]
          {
            { Collections.<String>emptyList(),  "/"},
            { asList("foo"),                    "/foo"},
            { asList("foo", "bar"),             "/foo/bar"},
            { asList("foo", "bar", "baz"),      "/foo/bar/baz"},
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "appendPrependProvider")
    private Object[][] appendPrependProvider()
      {
        return new Object[][]
          {
          //  path            param    appended              prepended
            { "/foo",         "a",     "/foo/a",             "/a/foo"             },
            { "/foo",         "a/b",   "/foo/a/b",           "/a/b/foo"           },
            { "/foo/bar",     "a",     "/foo/bar/a",         "/a/foo/bar"         },
            { "/foo/bar",     "a/b",   "/foo/bar/a/b",       "/a/b/foo/bar"       },
            { "/foo/bar/baz", "a",     "/foo/bar/baz/a",     "/a/foo/bar/baz"     },
            { "/foo/bar/baz", "a/b",   "/foo/bar/baz/a/b",   "/a/b/foo/bar/baz"   },
            { "/foo/bar/baz", "a/b/c", "/foo/bar/baz/a/b/c", "/a/b/c/foo/bar/baz" },
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider(name = "extensionProvider")
    private Object[][] extensionProvider()
      {
        return new Object[][]
          {
          //  path                  extension
            { "/foo",               "",       },
            { "/foo.jpg",           "jpg",    },
            { "/foo.gif",           "gif",    },
            { "/foo.gif.jpg",       "jpg",    },
            { "/foo/bar.jpg",       "jpg",    },
            { "/foo/bar.gif",       "gif",    },
            { "/foo/bar.jpg.gif",   "gif",    },
          };
      }
  }