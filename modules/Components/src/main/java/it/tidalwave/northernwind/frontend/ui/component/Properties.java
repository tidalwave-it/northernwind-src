/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component;

import java.util.List;
import it.tidalwave.util.Key;
import lombok.NoArgsConstructor;
import static lombok.AccessLevel.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access=PRIVATE)
public final class Properties
  {
    public static final Key<String> PROPERTY_TITLE = new Key<>("title");

    public static final Key<String> PROPERTY_ID = new Key<>("id");

    public static final Key<String> PROPERTY_DESCRIPTION = new Key<>("description");

    public static final Key<String> PROPERTY_FULL_TEXT = new Key<>("fullText");

    public static final Key<String> PROPERTY_TEMPLATE_PATH = new Key<>("templatePath");

    public static final Key<String> PROPERTY_TEMPLATE = new Key<>("template");

    public static final Key<String> PROPERTY_WRAPPER_TEMPLATE_RESOURCE = new Key<>("wrapperTemplate");

    public static final Key<String> PROPERTY_CREATION_DATE = new Key<>("creationDateTime");// FIXME: those should be Key<ZonedDateTime>

    public static final Key<String> PROPERTY_PUBLISHING_DATE = new Key<>("publishingDateTime");// FIXME: those should be Key<ZonedDateTime>

    public static final Key<String> PROPERTY_LATEST_MODIFICATION_DATE = new Key<>("latestModificationDateTime");// FIXME: those should be Key<ZonedDateTime>

    public static final Key<String> PROPERTY_CLASS = new Key<>("class");

    public static final Key<String> PROPERTY_DATE_FORMAT = new Key<>("dateFormat");

    public static final Key<String> PROPERTY_TIME_ZONE = new Key<>("timeZone");

    public static final Key<List<String>> PROPERTY_CONTENTS = new Key<>("contents");

    // FIXME: should be Key<List<String>>
    public static final Key<String> PROPERTY_TAGS = new Key<>("tags");
  }
