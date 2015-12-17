/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
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
package it.tidalwave.northernwind.frontend.filesystem.hg;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import org.joda.time.DateTime;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@AllArgsConstructor(access = AccessLevel.PRIVATE) @NoArgsConstructor(staticName = "fileSystemChangedEvent")
@ToString
public class ResourceFileSystemChangedEventMatcher extends BaseMatcher<ResourceFileSystemChangedEvent>
  {
	@Wither
	private ResourceFileSystemProvider resourceFileSystemProvider;

	@Wither
	private DateTime latestModificationTime;

    @Override
    public boolean matches (final @Nonnull Object item)
      {
        if (! (item instanceof ResourceFileSystemChangedEvent))
		  {
	        return false;
	      }

		final ResourceFileSystemChangedEvent event = (ResourceFileSystemChangedEvent)item;

		if ((resourceFileSystemProvider != null) && (resourceFileSystemProvider != event.getFileSystemProvider()))
		  {
		    return false;
		  }

		if ((latestModificationTime != null) && (!latestModificationTime.equals(event.getLatestModificationTime())))
		  {
		    return false;
		  }

        return true;
      }

    @Override
    public void describeTo (final @Nonnull Description description)
      {
		final ResourceFileSystemChangedEvent event = new ResourceFileSystemChangedEvent(resourceFileSystemProvider,
																						latestModificationTime);
        description.appendText(event.toString());
      }
  }