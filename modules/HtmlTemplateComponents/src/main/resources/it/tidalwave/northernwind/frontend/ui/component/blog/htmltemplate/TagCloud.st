$if(title)$
    <h2>$title$</h2>
$endif$

$if(tags)$
    <div class="tagCloud">
        $tags :{ tag| <a href="$tag.link$" class="tagCloudItem rank$tag.rank$" rel="$tag.count$">$tag.name$</a>}; separator=",\n"$
    </div>
$endif$
