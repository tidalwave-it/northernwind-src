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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.calendar.CalendarView;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.htmltemplate.HtmlTemplateHtmlFragmentView;
import static java.util.Collections.singletonList;
import static it.tidalwave.northernwind.util.CollectionFunctions.concat;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link CalendarView} based on HTML templates.</p>
 *
 * @see     HtmlTemplateCalendarViewController
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Calendar/#v1.0",
              controlledBy=HtmlTemplateCalendarViewController.class)
public class HtmlTemplateCalendarView extends HtmlTemplateHtmlFragmentView implements CalendarView
  {
    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public HtmlTemplateCalendarView (final @Nonnull Id id, final @Nonnull Site site)
      {
        super(id);
        this.site = site;
      }

    /*******************************************************************************************************************
     *
     * Renders the diary contents. See {@link HtmlTemplateCalendarViewController} for more information.
     *
     * @see         HtmlTemplateCalendarViewController     *
     * @param       title           the optional title
     * @param       templatePath    an optional template (otherwise a default one is used)
     * @param       monthNames      the name of months in the required {@code Language}
     * @param       year            the current year
     * @param       years           the available years
     * @param       entries         the items of the current year
     *
     ******************************************************************************************************************/
    public void render (final @Nonnull Optional<String> title,
                        final @Nonnull Optional<ResourcePath> templatePath,
                        final @Nonnull String[] monthNames,
                        final @Nonnull String year,
                        final @Nonnull Aggregates years,
                        final @Nonnull List<Aggregates> entries)
      {
        final Template template = site.getTemplate(getClass(), templatePath, "Calendar.st");
        title.ifPresent(t -> template.addAttribute("title", t));
        template.addAttribute("year", year);
        IntStream.rangeClosed(1, monthNames.length).forEach(i -> template.addAttribute("month" + i, monthNames[i - 1]));
        addComponent(new HtmlHolder(template.render(concat(singletonList(years), entries))));
      }
  }
