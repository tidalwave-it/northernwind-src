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
package it.tidalwave.northernwind.frontend.ui.component.blog.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.Template;
import it.tidalwave.northernwind.frontend.ui.component.Template.Aggregates;
import it.tidalwave.northernwind.frontend.ui.component.TemplateHelper;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.htmltemplate.HtmlTemplateHtmlFragmentView;
import it.tidalwave.northernwind.frontend.ui.component.blog.BlogView;

/***********************************************************************************************************************
 *
 * <p>An implementation of {@link BlogView} based on HTML templates. Two templates can be customised, by creating some
 * content with the {@code P_TEMPLATE} property in the following paths:</p>
 *
 * <ul>
 * <li>{@code /Templates/Blog/Posts} for rendering posts;</li>
 * <li>{@code /Templates/Blog/TagCloud} for rendering the tag cloud;</li>
 * </ul>
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Blog/#v1.0",
              controlledBy=HtmlTemplateBlogViewController.class)
public class HtmlTemplateBlogView extends HtmlTemplateHtmlFragmentView implements BlogView
  {
    public static final String TEMPLATES_BLOG_POSTS = "/Templates/Blog/Posts";

    public static final String TEMPLATES_BLOG_TAG_CLOUD = "/Templates/Blog/TagCloud";

    private final TemplateHelper templateHelper;

//    private final Template postTemplate;

    private final Template postsTemplate;

    private final Template tagCloudTemplate;

    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     *
     * @param       site            the site
     * @param       id              the id of this view
     *
     ******************************************************************************************************************/
    public HtmlTemplateBlogView (final @Nonnull Site site, final @Nonnull Id id)
      {
        super(id);
        templateHelper   = new TemplateHelper(this, site);
//        postTemplate     = templateHelper.getTemplate("/Templates/Blog/Post", "Post.st");
        postsTemplate    = templateHelper.getTemplate(TEMPLATES_BLOG_POSTS, "Posts.st");
        tagCloudTemplate = templateHelper.getTemplate(TEMPLATES_BLOG_TAG_CLOUD, "TagCloud.st");
//        postsTemplate.include("/singlePost", postTemplate);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void setTitle (final @Nonnull String title)
      {
        postsTemplate.addAttribute("title", title);
        tagCloudTemplate.addAttribute("title", title);
      }

    /*******************************************************************************************************************
     *
     * Renders the blog contents. See {@link HtmlTemplateBlogViewController} for more information.
     *
     * @see         HtmlTemplateBlogViewController
     * @param       fullPosts       the posts to be rendered in full
     * @param       leadinPosts     the posts to be rendered as lead-in text
     * @param       linkedPosts     the posts to be rendered as links
     *
     ******************************************************************************************************************/
    public void renderPosts (final @Nonnull Aggregates fullPosts,
                             final @Nonnull Aggregates leadinPosts,
                             final @Nonnull Aggregates linkedPosts)
      {
        addComponent(new HtmlHolder(postsTemplate.render(fullPosts, leadinPosts, linkedPosts)));
      }

    /*******************************************************************************************************************
     *
     * Renders the tag cloud. See {@link HtmlTemplateBlogViewController} for more information.
     *
     * @see         HtmlTemplateBlogViewController
     * @param       tags            the tags to render in the cloud
     *
     ******************************************************************************************************************/
    public void renderTagCloud (final @Nonnull Aggregates tags)
      {
        addComponent(new HtmlHolder(tagCloudTemplate.render(tags)));
      }
  }
