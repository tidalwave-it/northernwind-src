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
package it.tidalwave.northernwind.frontend.ui.component.rssfeed;

import it.tidalwave.util.Key;
import it.tidalwave.northernwind.frontend.ui.ViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface RssFeedViewController extends ViewController
  {
    public static final Key<String> P_CREATOR = Key.of("creator", String.class);

    public static final Key<String> P_LINK = Key.of("link", String.class);

//    public static final Key<String> P_X = new Key<String>("");
//        <property name="contents"
//            <values>
//                <value>/Blog</value>
//            </values>
//        </property>

//    <property name="copyright">
//        <property name="">
//        <property name="">
//        <property name="">
//        <property name="maxItems">
//        <property name="">

  }
