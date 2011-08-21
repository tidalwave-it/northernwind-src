/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarView;
import it.tidalwave.northernwind.frontend.ui.component.calendar.DefaultCalendarViewController;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * An HtmlTemplate implementation of {@link CalendarView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Calendar/#v1.0", 
              controlledBy=DefaultCalendarViewController.class)
public class HtmlTemplateCalendarView extends HtmlHolder implements CalendarView
  {
    @Getter @Nonnull
    private final Id id;

    public HtmlTemplateCalendarView (final @Nonnull Id id)
      {
        super(id);
        this.id = id;
      }
  }