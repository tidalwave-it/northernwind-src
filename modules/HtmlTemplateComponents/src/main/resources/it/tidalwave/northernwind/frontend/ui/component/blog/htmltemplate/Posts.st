$if(title)$
    <h2>$title$</h2>
$endif$

$if(fullPosts)$
<div class="nw-full-posts">
    $fullPosts :{ post|
    <article>
        <div id='$post.id$' class='nw-blog-post'>
            <h3>$post.title$</h3>
            <div class='nw-blog-post-meta'>
                <span class='nw-publishDate'>$post.publishDate$</span>&#160;-

                $if(post.category)$
                <span class='nw-blog-post-category'>Filed under "$post.category$"</span>&#160;-
                $endif$

                $if(post.tags)$
                <span class='nw-blog-post-tags'>Tagged as
                    $post.tags :{ tag| <a class='nw-tag' href='$tag.link$'>$tag.name$</a>}; separator=",\n"$
                </span>&#160;-
                $endif$

                <a href='$post.link$'>Permalink</a>
            </div>
            <div class='nw-blog-post-content'>
                $post.text$
            </div>
        </div>
    </article>
    }$
</div>
$endif$

$if(leadinPosts)$
<div class="nw-leadin-posts">
    $leadinPosts :{ post|
    <article>
        <div id='$post.id$' class='nw-blog-post'>
            <h3>$post.title$</h3>
            <div class='nw-blog-post-meta'>
                <span class='nw-publishDate'>$post.publishDate$</span>&#160;-

                $if(post.category)$
                <span class='nw-blog-post-category'>Filed under "$post.category$"</span>&#160;-
                $endif$

                $if(post.tags)$
                <span class='nw-blog-post-tags'>Tagged as
                    $post.tags :{ tag| <a class='nw-tag' href='$tag.link$'>$tag.name$</a>}; separator=",\n"$
                </span>&#160;-
                $endif$

                <a href='$post.link$'>Permalink</a>
            </div>
            <div class='nw-blog-post-content'>
                $post.text$
            </div>
        </div>
    </article>
    }$
</div>
$endif$

$if(linkedPosts)$
<div class="nw-linked-posts">
    <ul>
        $linkedPosts :{ post| <li><a href='$post.link$'>$post.title$</a></li>}; separator="\n"$
    </ul>
</div>
$endif$
