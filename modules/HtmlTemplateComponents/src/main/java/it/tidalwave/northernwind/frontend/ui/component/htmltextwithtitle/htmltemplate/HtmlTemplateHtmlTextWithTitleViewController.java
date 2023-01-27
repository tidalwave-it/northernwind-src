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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.htmltemplate;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.model.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.DefaultHtmlTextWithTitleViewController;
import it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle.HtmlTextWithTitleViewController;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link HtmlTextWithTitleViewController} based on HTML templates.</p>
 *
 * <p>The templates for rendering the page can be specified by means of the following properties:</p>
 *
 * <ul>
 * <li>{@code P_TEMPLATE_PATH}: the template for rendering all the component;</li>
 * <li>{@code P_WRAPPER_TEMPLATE_PATH}: an optional template wrapping each single item.</li>
 * </ul>
 *
 * <p>This controller calls render methods to the view by passing {@link Aggregates} to be used with templates:</p>
 *
 * <ul>
 * <li>{@code contents}: the items to be rendered.</li>
 * </ul>
 *
 * <p>Each item of this {@link Aggregate} is composed of the following fields:</p>
 *
 * <ul>
 * <li>{@code title}: the title of the item;</li>
 * <li>{@code text}: the text of the item;</li>
 * <li>{@code level}: the level of the post (used for rendering the title).</li>
 * </ul>
 *
 * @see     DefaultHtmlTextWithTitleViewController
 * @see     HtmlTemplateHtmlTextWithTitleView
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class HtmlTemplateHtmlTextWithTitleViewController extends DefaultHtmlTextWithTitleViewController
  {
    @Nonnull
    private final HtmlTemplateHtmlTextWithTitleView view;

    @Nonnull
    private final SiteNode siteNode;

    /*******************************************************************************************************************
     *
     * Creates a new instance.
     *
     * @param       view        the controlled view
     * @param       siteNode    the associated {@link SiteNode}
     *
     ******************************************************************************************************************/
    public HtmlTemplateHtmlTextWithTitleViewController (@Nonnull final HtmlTemplateHtmlTextWithTitleView view,
                                                        @Nonnull final SiteNode siteNode)
      {
        super(view, siteNode);
        this.siteNode = siteNode;
        this.view = view;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void render (@Nonnull final List<? extends TextWithTitle> txts)
      {
        final var viewProperties = siteNode.getPropertyGroup(view.getId());
        view.render(viewProperties.getProperty(P_WRAPPER_TEMPLATE_PATH),
                    viewProperties.getProperty(P_TEMPLATE_PATH),
                    txts.stream().map(HtmlTemplateHtmlTextWithTitleViewController::toAggregate).collect(toAggregates("contents")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Aggregate toAggregate (@Nonnull final TextWithTitle content)
      {
        return Aggregate.of("title", content.title).with("text", content.text).with("level", content.level);
      }
  }
