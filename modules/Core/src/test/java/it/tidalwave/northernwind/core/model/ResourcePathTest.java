/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnull;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static com.google.common.collect.ImmutableList.of;

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
        // when
        final ResourcePath underTest = new ResourcePath();
        // then
        assertThat(underTest.segments, is(not(nullValue())));
        assertThat(underTest.segments.isEmpty(), is(true));
        assertThat(underTest.asString(), is("/"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = NullPointerException.class)
    public void must_reject_null_segments()
      {
        final ResourcePath underTest = new ResourcePath(of("a", null, "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void must_reject_empty_segments()
      {
        final ResourcePath underTest = new ResourcePath(of("a", "", "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void must_reject_segments_containing_slash()
      {
        final ResourcePath underTest = new ResourcePath(of("a", "b", "/c"));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "segmentProvider",
          dependsOnMethods = "must_properly_create_an_empty_path")
    public void must_properly_create_an_empty_path_from_string (final @Nonnull String pathAsString,
                                                                final @Nonnull String expectedAsString,
                                                                final @Nonnull List<String> expectedSegments)
      {
        // when
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // then
        assertThat(underTest.segments, is(not(nullValue())));
        assertThat(underTest.segments, is(expectedSegments));
        assertThat(underTest.asString(), is(expectedAsString));
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
        final ResourcePath underTest = new ResourcePath(invalidPathAsString);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "asStringProvider",
          dependsOnMethods = { "must_properly_create_an_empty_path", "must_properly_create_an_empty_path_from_string" })
    public void must_properly_compute_asString (final @Nonnull ImmutableList<String> segments,
                                                final @Nonnull String expectedAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(segments);
        // when
        final String asString = underTest.asString();
        // then
        assertThat(asString, is(expectedAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "relativePathProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_relative_paths (final @Nonnull String pathAsString,
                                                      final @Nonnull String parentPathAsString,
                                                      final @Nonnull String expectedPathAsString)
      {
        // given
        final ResourcePath parentPath = new ResourcePath(parentPathAsString);
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath relativePath = underTest.relativeTo(parentPath);
        // then
        assertThat(relativePath.asString(), is(expectedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "nonRelativePathProvider",
          dependsOnMethods = "must_properly_compute_asString",
          expectedExceptions = IllegalArgumentException.class)
    public void must_properly_reject_non_relative_paths (final @Nonnull String pathAsString,
                                                         final @Nonnull String parentPathAsString)
      {
        // given
        final ResourcePath parentPath = new ResourcePath(parentPathAsString);
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        underTest.relativeTo(parentPath);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "leadingAndTrailingProvider")
    public void must_properly_compute_leading_segment (final @Nonnull String pathAsString,
                                                       final @Nonnull String expectedLeadingSegment,
                                                       final @Nonnull String expectedTrailingSegment)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final String leading = underTest.getLeading();
        // then
        assertThat(leading, is(expectedLeadingSegment));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "leadingAndTrailingProvider")
    public void must_properly_compute_trailing_segment (final @Nonnull String pathAsString,
                                                        final @Nonnull String expectedLeadingSegment,
                                                        final @Nonnull String expectedTrailingSegment)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final String trailing = underTest.getTrailing();
        // then
        assertThat(trailing, is(expectedTrailingSegment));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "withoutLeadingAndTrailingProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_without_leading (final @Nonnull String pathAsString,
                                                       final @Nonnull String expectedPathWithoutLeadingAsString,
                                                       final @Nonnull String expectedPathWithoutTrailingAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath withoutLeading = underTest.withoutLeading();
        // then
        assertThat(withoutLeading.asString(), is(expectedPathWithoutLeadingAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "withoutLeadingAndTrailingProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_compute_without_trailing (final @Nonnull String path,
                                                        final @Nonnull String expectedPathWithoutLeadingAsString,
                                                        final @Nonnull String expectedPathWithoutTrailingAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(path);
        // when
        final ResourcePath withoutTrailing = underTest.withoutTrailing();
        // then
        assertThat(withoutTrailing.asString(), is(expectedPathWithoutTrailingAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "startsWithProvider")
    public void must_properly_compute_startsWith (final @Nonnull String pathAsString,
                                                  final @Nonnull String leadingSegment,
                                                  final @Nonnull boolean expectedResult)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final boolean startsWith = underTest.startsWith(leadingSegment);
        // then
        assertThat(startsWith, is(expectedResult));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_append_path (final @Nonnull String pathAsString,
                                           final @Nonnull String secondPathAsString,
                                           final @Nonnull String expectedAppendedPathAsString,
                                           final @Nonnull String expectedPrependedPathAsString)
      {
        // given
        final ResourcePath appendingPath = new ResourcePath(secondPathAsString);
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath appendedWith = underTest.appendedWith(appendingPath);
        // then
        assertThat(appendedWith.asString(), is(expectedAppendedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_append_string (final @Nonnull String pathAsString,
                                             final @Nonnull String secondPathAsString,
                                             final @Nonnull String expectedAppendedPathAsString,
                                             final @Nonnull String expectedPrependedPathAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath appendedWith = underTest.appendedWith(secondPathAsString);
        // then
        assertThat(appendedWith.asString(), is(expectedAppendedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_prepend_path (final @Nonnull String pathAsString,
                                            final @Nonnull String secondPathAsString,
                                            final @Nonnull String expectedAppendedPathAsString,
                                            final @Nonnull String expectedPrependedPathAsString)
      {
        // given
        final ResourcePath prependingPath = new ResourcePath(secondPathAsString);
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath prependedWith = underTest.prependedWith(prependingPath);
        // then
        assertThat(prependedWith.asString(), is(expectedPrependedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "appendPrependProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_prepend_string (final @Nonnull String pathAsString,
                                              final @Nonnull String secondPathAsString,
                                              final @Nonnull String expectedAppendedPathAsString,
                                              final @Nonnull String expectedPrependedPathAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final ResourcePath prependedWith = underTest.prependedWith(secondPathAsString);
        // then
        assertThat(prependedWith.asString(), is(expectedPrependedPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "segmentProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_count_segments (final @Nonnull String pathAsString,
                                              final @Nonnull String expectedAsString,
                                              final @Nonnull List<String> expectedSegments)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final int segmentCount = underTest.getSegmentCount();
        // then
        assertThat(segmentCount, is(expectedSegments.size()));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "extensionProvider")
    public void must_properly_compute_extension (final @Nonnull String pathAsString,
                                                 final @Nonnull String expectedExtension)
      {
        // given
        final ResourcePath underTest = new ResourcePath(pathAsString);
        // when
        final String extension = underTest.getExtension();
        // then
        assertThat(extension, is(expectedExtension));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "urlEncodedPathProvider",
          dependsOnMethods = "must_properly_compute_asString")
    public void must_properly_url_decode (final @Nonnull String plainPathAsString,
                                          final @Nonnull String encodedPathAsString)
      {
        // given
        final ResourcePath underTest = new ResourcePath(encodedPathAsString);
        // when
        final ResourcePath urlDecoded = underTest.urlDecoded();
        // then
        assertThat(urlDecoded.asString(), is(plainPathAsString));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] segmentProvider()
      {
        return new Object[][]
          {
          //  path              exp. asString       exp. segments
            { "",               "/",                of()                        },
            { "/",              "/",                of()                        },
            { "/foo",           "/foo",             of("foo")                   },
            { "/foo/bar",       "/foo/bar",         of("foo", "bar")            },
            { "/foo/bar/baz",   "/foo/bar/baz",     of("foo", "bar", "baz")     },

            { "foo",            "/foo",             of("foo")                   },
            { "foo/bar",        "/foo/bar",         of("foo", "bar")            },
            { "foo/bar/baz",    "/foo/bar/baz",     of("foo", "bar", "baz")     }
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
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
    @DataProvider
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
    @DataProvider
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
    @DataProvider
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
    @DataProvider
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
    @DataProvider
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
    @DataProvider
    private Object[][] asStringProvider()
      {
        return new Object[][]
          {
            { of(),                     "/"},
            { of("foo"),                "/foo"},
            { of("foo", "bar"),         "/foo/bar"},
            { of("foo", "bar", "baz"),  "/foo/bar/baz"},
          };
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] appendPrependProvider()
      {
        return new Object[][]
          {
          //  path            param    appended              prepended
            { "/foo",         "",      "/foo",               "/foo"               },
            { "/foo",         "/",     "/foo",               "/foo"               },
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
    @DataProvider
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

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private Object[][] urlEncodedPathProvider()
      {
        return new Object[][]
          {
          //  plain            encoded
            { "/",             "/",                 },
            { "/foo",          "foo"                },
            { "/foo/bar (2)",  "foo/bar+%282%29"    }
          };
      }
  }