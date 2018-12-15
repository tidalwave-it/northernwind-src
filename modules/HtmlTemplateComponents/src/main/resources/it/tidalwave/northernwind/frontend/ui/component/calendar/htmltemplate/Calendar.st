    $if(title)$
        <h2>$title$</h2>
    $endif$

    <div class='nw-calendar'>
        <table class='nw-calendar-table'>
            <tbody>
                <tr>
                    <th colspan='4' class='nw-calendar-title'>$year$</th>
                </tr>
                <tr>
                    <th width='25%'>$month1$</th><th width='25%'>$month2$</th><th width='25%'>$month3$</th><th width='25%'>$month4$</th></tr>
                <tr>
                    <td>
                        <ul>
                            $entries1 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries2 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries3 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries4 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                </tr>
                <tr>
                    <th width='25%'>$month5$</th><th width='25%'>$month6$</th><th width='25%'>$month7$</th><th width='25%'>$month8$</th></tr>
                <tr>
                    <td>
                        <ul>
                            $entries5 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries6 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries7 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries8 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                </tr>
                <tr>
                    <th width='25%'>$month9$</th><th width='25%'>$month10$</th><th width='25%'>$month11$</th><th width='25%'>$month12$</th></tr>
                <tr>
                    <td>
                        <ul>
                            $entries9 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries10 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries11 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                    <td>
                        <ul>
                            $entries12 :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                        </ul>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class='nw-calendar-yearselector'>
            $years :{year | $if(year.link)$<a href='$year.link$'>$year.number$</a>$else$$year.number$$endif$}; separator="\n| " $
        </div>
    </div>