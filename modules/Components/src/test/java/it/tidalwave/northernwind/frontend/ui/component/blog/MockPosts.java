/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.blog;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import it.tidalwave.util.Finder;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;
import static it.tidalwave.northernwind.frontend.ui.component.blog.BlogViewController.*;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @RequiredArgsConstructor
public class MockPosts
  {
    @Nonnull
    private final Site site;

    @Nullable
    private final ResourceProperties viewProperties;

    @Getter
    private List<Content> posts;

    @Getter
    private List<ZonedDateTime> dates;

    @Getter
    private List<String> categories;

    @Getter
    private List<String> tags;

    /*******************************************************************************************************************
     *
     * @param       seed        the seed for the pseudo-random sequence
     *
     ******************************************************************************************************************/
    public void createMockData (final int seed)
      {
        categories = Arrays.asList(null, "category1", "category2"); // List.of() doesn't allow null
        tags  = IntStream.rangeClosed(1, 10).mapToObj(i -> "tag" + i).collect(toList());
        dates = createMockDateTimes(100, seed);
        posts = createMockPosts(100, dates, categories, tags, seed);

        // Distribute all the posts to different folders
        final List<String> paths = List.of("/blog", "/blog/folder1", "/blog/folder2");
        final Random rnd = new Random(seed);

        if (viewProperties != null)
          {
            posts.stream()
                 .collect(groupingBy(__ -> paths.get(rnd.nextInt(paths.size()))))
                 .forEach((key, value) ->
                    {
                      final Content blogFolder = site.find(_Content_).withRelativePath(key).optionalResult().get();
                      when(blogFolder.findChildren()).thenReturn((Finder)Finder.ofCloned(value));
                    });

            when(viewProperties.getProperty(eq(P_CONTENT_PATHS))).thenReturn(Optional.of(paths));
          }

        posts.forEach(post -> log.info(">>>> post {}", post));
      }

    /*******************************************************************************************************************
     *
     * Creates the given number of mock dates and times, spanned in the decade 1/1/2018 - 31/12/2028.
     *
     * @param       count       the count of dates
     * @param       seed        the seed for the pseudo-random sequence
     * @return                  the dates
     *
     ******************************************************************************************************************/
    @Nonnull
    public static List<ZonedDateTime> createMockDateTimes (final @Nonnegative int count, final int seed)
      {
        final ZonedDateTime base = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT"));
        final List<ZonedDateTime> dates = new Random(seed).ints(count, 0, 10 * 365 * 24 * 60)
                                                          .mapToObj(base::plusMinutes)
                                                          .collect(toList());
        final ZonedDateTime max = dates.stream().max(ZonedDateTime::compareTo).get();
        final ZonedDateTime min = dates.stream().min(ZonedDateTime::compareTo).get();
        assert Duration.between(min, max).getSeconds() > 9 * 365 * 24 * 60 : "No timespan";
        return dates;
      }

    /*******************************************************************************************************************
     *
     * Create the given number of mock {@link Content} instances representing blog posts.
     * Each one is assigned:
     *
     * <ul>
     * <li>a {@code P_PUBLISHING_DATE} taken from the given collection of dateTimes;</li>
     * <li>a {@code P_TITLE} set as {@code "Title #&lt;num&gt;"}</li>
     * <li>a {@code P_FULL_TEXT} set as {@code "Full text #&lt;num&gt;"}</li>
     * <li>a {@code P_LEADIN_TEXT} set as {@code "Lead in text #&lt;num&gt;"}</li>
     * <li>a {@code P_ID} set as {@code "id#&lt;num&gt;"}</li>
     * <li>a {@code P_IMAGE_ID} set as {@code "imageId#&lt;num&gt;"} to the 10% of posts</li>
     * <li>a {@code P_CATEGORY} taken from the given collection, each one having equals chances of being set.</li>
     * <li>a {@code P_TAGS} taken from the given collection, each one having 50% of chances of being set.</li>
     * <li>a {@code getExposedUri()} set as {@code "post-#&lt;num&gt;"}</li>
     * </ul>
     *
     * A convenient {@code toString()} method is also mocked.
     *
     * All used random sequences are reproducible for the sake of test assertions.
     *
     * @param       count       the required number of posts
     * @param       dateTimes   a collection of datetimes used as the publishing date of each post
     * @param       categories  a collection of categories that are randomly assigned to posts
     * @param       tags        a collection of tags that are randomly assigned to posts
     * @param       seed        the seed for the pseudo-random sequence
     * @return                  the mock posts
     *
     ******************************************************************************************************************/
    @Nonnull
    public static List<Content> createMockPosts (final @Nonnegative int count,
                                                 final @Nonnull List<ZonedDateTime> dateTimes,
                                                 final @Nonnull List<String> categories,
                                                 final @Nonnull List<String> tags,
                                                 final int seed)
      {
        final List<Content> posts = new ArrayList<>();
        final Random categoryRnd = new Random(seed);
        final Random tagRnd      = new Random(seed);
        final Random imageIdRnd  = new Random(seed);

        for (int i = 0; i< count; i++)
          {
            final ZonedDateTime dateTime = dateTimes.get(i);
            final Content post = createMockContent();
            final ResourceProperties properties = createMockProperties();
            when(post.toString()).thenAnswer(invocation -> toString((Content)invocation.getMock()));
            when(post.getProperties()).thenReturn(properties);
            when(post.getExposedUri()).thenReturn(Optional.of(ResourcePath.of(String.format("post-%d", i))));
            when(properties.getProperty(P_PUBLISHING_DATE)).thenReturn(Optional.of(dateTime));
            when(properties.getProperty(P_TITLE)).thenReturn(Optional.of(String.format("Title #%2d", i)));
            when(properties.getProperty(P_ID)).thenReturn(Optional.of(String.format("id#%2d", i)));
            when(properties.getProperty(P_FULL_TEXT)).thenReturn(Optional.of(String.format("Full text #%2d", i)));
            when(properties.getProperty(P_LEADIN_TEXT)).thenReturn(Optional.of(String.format("Lead in text #%2d", i)));

            if (imageIdRnd.nextDouble() > 0.9)
              {
                when(properties.getProperty(P_IMAGE_ID)).thenReturn(Optional.of(String.format("imageId#%2d", i)));
              }

            // Assign category
            final Optional<String> category = Optional.ofNullable(categories.get(categoryRnd.nextInt(categories.size())));
            when(post.getProperties().getProperty(P_CATEGORY)).thenReturn(category);

            // Assign tag
            final List<String> t2 = tags.stream().filter(__ -> tagRnd.nextDouble() > 0.5).collect(toList());

            if (!t2.isEmpty())
              {
                when(properties.getProperty(P_TAGS)).thenReturn(Optional.of(t2));
              }

            posts.add(post);
          }

        return posts;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String toString (final @Nonnull Content post)
      {
        final String title        = post.getProperty(P_TITLE).orElse("???");
        final String exposedUri   = post.getExposedUri().map(ResourcePath::asString).orElse("???");
        final String dateTime     = post.getProperty(P_PUBLISHING_DATE).map(ZonedDateTime::toString).orElse("???");
        final String imageId      = post.getProperty(P_IMAGE_ID).orElse("");
        final String category     = post.getProperty(P_CATEGORY).orElse("");
        final Object tags         = post.getProperty(P_TAGS).orElse(emptyList());
        return String.format("Content(%s - %-10s - %s - %-10s - %-10s - %s)", title, exposedUri, dateTime, imageId, category, tags);
      }
  }
