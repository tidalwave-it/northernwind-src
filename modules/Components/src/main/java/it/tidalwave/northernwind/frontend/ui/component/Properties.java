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
package it.tidalwave.northernwind.frontend.ui.component;

import java.util.List;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.ResourcePath;
import lombok.NoArgsConstructor;
import static lombok.AccessLevel.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access=PRIVATE)
public final class Properties
  {
    public static final Key<ResourcePath> P_TEMPLATE_PATH = Key.of("templatePath", ResourcePath.class);

    public static final Key<ResourcePath> P_WRAPPER_TEMPLATE_PATH = Key.of("wrapperTemplate", ResourcePath.class);

    public static final Key<List<String>> P_CONTENT_PATHS = new Key<List<String>>("contents") {};

    public static final Key<String> P_CLASS = Key.of("class", String.class);

    public static final Key<String> P_DATE_FORMAT = Key.of("dateFormat", String.class);

    public static final Key<String> P_TIME_ZONE = Key.of("timeZone", String.class);

    /** The level of a content, used to pick the heading. */
    public static final Key<Integer> P_LEVEL = Key.of("level", Integer.class);
  }
