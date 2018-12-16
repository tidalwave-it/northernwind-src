    $if(title)$
        <h2>$title$</h2>
    $endif$

    <div class='nw-calendar'>
        <table class='nw-calendar-table'>
            <tbody>
                <tr>
                    <th colspan='4' class='nw-calendar-title'>$year$</th>
                </tr>
                $rows : { row |
                <tr>
                    $row:{ m | <th width='25%'>$month.(m)$</th>}; separator="\n"$
                </tr>
                <tr>
                $row:{ m |
                  <td>
                      $if(entries.(m))$
                      <ul>
                          $entries.(m) :{ entry |<li><a href='$entry.link$'$if(entry.class)$ class='$entry.class$'$endif$>$entry.label$</a></li>}; separator="\n"$
                      </ul>
                      $endif$
                  </td>}; separator="\n"$
                </tr>}; separator="\n"$
            </tbody>
        </table>
        <div class='nw-calendar-yearselector'>
            $years :{year | $if(year.link)$<a href='$year.link$'>$year.number$</a>$else$$year.number$$endif$}; separator="\n| " $
        </div>
    </div>