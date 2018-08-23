<%@ page import="act.ws.search.*" 
%><%@ include file="/epcwid1/configuration.inc" %><%--
--%><%
    act.ws.search.SearchAccount[] matchedAccounts = null;

    String searchType           = nvl(request.getParameter("search-type"));
    String criteria             = nvl(request.getParameter("criteria"));
    String alternateCriteria    = nvl(request.getParameter("altCriteria"));

    act.ws.search.Search accountSearch = null;
    StringBuilder buffer = new StringBuilder();

    try {
        if ( notDefined(criteria) ) {
            if ( notDefined(session.getAttribute("lastSearchCriteria")) ) {
                throw new UnsupportedOperationException();
            }

            searchType          = nvl((String)session.getAttribute("lastSearchType"));
            criteria            = nvl((String)session.getAttribute("lastSearchCriteria"));
            alternateCriteria   = nvl((String)session.getAttribute("lastSearchAlternateCriteria"));
        } else {
            session.setAttribute("lastSearchType",searchType);
            session.setAttribute("lastSearchCriteria",criteria);
            session.setAttribute("lastSearchAlternateCriteria",alternateCriteria);
        }



        /****

        // If tax year is 0 then the current internet tax year will be determined from the client prefs
        if ( "address".equals(searchType) ) {
            accountSearch = new TaxsiteSearch(client_id, taxYear, criteria);
        } else if ( "ownerAddress".equals(searchType) ) { // Takes a (relatively) significant amount of time
            accountSearch = new OwnerAddressSearch(client_id, taxYear, criteria);
        } else if ( "account".equals(searchType) ) {
            accountSearch = new CANSearch(client_id, taxYear, criteria);
        } else if ( "cad".equals(searchType) ) {
            accountSearch = new CADSearch(client_id, taxYear, criteria);
        } else if ( "fido".equals(searchType) ) {
            accountSearch = new FIDOSearch(client_id, taxYear, criteria);
        } else if ( "fidoFuture".equals(searchType) ) {
            accountSearch = new FIDOFutureYearSearch(client_id, taxYear, criteria);
        } else if ( "attorney".equals(searchType) ) {
            accountSearch = new AttorneySearch(client_id, taxYear, criteria);
        } else {
            accountSearch = new OwnerNameSearch(client_id, taxYear, criteria);
        }
        ****/

        if ( "account".equals(searchType) ) {
            accountSearch = CANSearch.initialContext();
        } else if ( "address".equals(searchType) ) {
            accountSearch = TaxsiteSearch.initialContext();
        } else if ( "cad".equals(searchType) ) {
            accountSearch = CADSearch.initialContext();
        } else {
            accountSearch = OwnerNameSearch.initialContext();
        }

        matchedAccounts = accountSearch.setCriteria(clientId, criteria)
                                        .setAltCriteria(alternateCriteria)
                                        .setPhraseSearch()
                                        .allowWildcardSearch(true)
                                        .showRestrictedAccounts(allowRestrictedAccounts)
                                        .setQueryValues(true)
                                        .setMaxRecords(maximumSearchRecords)
                                        .search(dataSource)
                                        ;
        if ( matchedAccounts == null || matchedAccounts.length == 0 ) {
            if ( "debug".equals(request.getQueryString()) ) {
                %><pre style="text-align:left;"><%= accountSearch.trace.toString() %></pre><%
                return;
            }

            %><jsp:forward page="searchResults-NoMatches.jsp"></jsp:forward><%
        }

        // Change sort order to be by Owner
        SearchAccount.sortOnOwner(matchedAccounts);
    } catch (UnsupportedOperationException uoe) {
        // This error occurs when the user hasn't specified any search criteria or refreshes the page
        if ( "debug".equals(request.getQueryString()) ) {
            %><pre style="text-align:left;"><h3> Exception: <%= uoe.toString() %></h3><%= (accountSearch == null ? "" : accountSearch.trace.toString()) %></pre><%
            return;
        }

        %><jsp:forward page="searchResults-NoMatches.jsp"></jsp:forward><%
    } catch (Throwable e) {
        if ( "debug".equals(request.getQueryString()) ) {
            %><pre style="text-align:left;"><h3> Exception: <%= e.toString() %></h3><%= (accountSearch == null ? "" : accountSearch.trace.toString()) %></pre><%
            return;
        }

        %><jsp:forward page="searchResults-NoMatches.jsp"></jsp:forward><%
        return;
    }
%><%@ include file="header.jsp" %>

<style>
    table.sortable {  margin-top: 10px; margin-left: auto; margin-right: auto; width: 1100px; min-width: 1100px; }
    table.sortable thead th { font-size: 1.3rem; font-weight: bold; font-style: italic; font-family: Arial, Helvetica;  padding: 15px 10px 8px; cursor: pointer; }
    table.sortable td { font-size: 0.9rem; font-family: Arial, Helvetica; padding: 5px 10px; }
    .notice { color: black; font-size: 1.1rem; font-style: normal; margin:10px 0px 20px; font-weight: normal ; }
    @media print {
        * {
            display: none;
        }
        #printableTable {
            display: block;
        }
    }
    table.sortable thead th { position: relative; padding-right: 20px; }
    .headerSortDown:after, .headerSortUp:after {
            content: ' '; 
            position: absolute; right: 3px;
            border: 6px solid transparent;
    }
    .headerSortDown:after { bottom: 6px; border-top-color: black; }
    .headerSortUp:after   { bottom: 20px; border-bottom-color: black; }

    table thead tr th { background-color: #d9d9d9; }
    table tbody tr:nth-child(3n) { background-color: #f1f1f1; }
    table tbody tr { display: relative; }
    table caption { font-size: 1.1rem; margin: 10px 0px 10px; text-align: right; }
    table td div.rowId { border: none; background: transparent; position: absolute; top: 5px; right: -38px; 
        border: 2px solid #e9e9e9; background: #f6f6f6;  width: 30px; border-radius: 15px; text-align: center; }
    table tbody td:last-child { position: relative; }

</style>
<noscript>
<style>
    table.sortable thead th { cursor: default }
    .javaScriptRequired { display: none; }
</style>
</noscript>
<script>
    function __printClassElements(classNameToPrint) {
        var target = window.frames["print_frame"].document.body;
        target.innerHTML = "";

        var printableElements = document.getElementsByClassName(classNameToPrint);
        var contentToPrint = "";
        for ( var i=0; i < printableElements.length; i++ ) {
                contentToPrint += printableElements[i].innerHTML;
        }
        target.innerHTML = contentToPrint;
        window.frames["print_frame"].window.focus();
        window.frames["print_frame"].window.print();
        return false;
    }
    function __printIdElement(elementIdToPrint) {
        var printableElement = document.getElementById(elementIdToPrint);
        if ( printableElement ) {
            window.frames["print_frame"].document.body.innerHTML = printableElement.innerHTML;
            window.frames["print_frame"].window.focus();
            window.frames["print_frame"].window.print();
        }
        return false;
    }
    function printDiv() {
        __printIdElement("printableTable");
        return false;
    }

    var ASCENDING       = 1;
    var DESCENDING      = -1;
    var sortedOnColumn  = null;
    var sortOrder       = null;
    function sortColumns(a,b) {
        if ( a.value < b.value ) return -1;
        if ( a.value > b.value ) return 1;

        if ( a.account < b.account ) return -1;
        if ( a.account > b.account ) return 1;

        return 0;
    }
    function sortTable(columnToSort) {
        var tableRows = [];
        $("table.sortable tbody tr").each(function() { 
            tableRows.push({ row: $(this), 
                             value: $(this)[0].cells[columnToSort].innerText,
                             account: $(this)[0].cells[0].innerText
                             }); 
        });

        $(".headerSortUp,.headerSortDown").removeClass("headerSortUp headerSortDown");
        if ( sortedOnColumn != columnToSort || sortOrder == DESCENDING ) {
            sortedOnColumn = columnToSort;
            sortOrder = ASCENDING;
            tableRows.sort(sortColumns);
            $("table.sortable thead th:nth-child(" + (columnToSort+1) + ")").addClass("headerSortUp");
        } else {
            sortOrder = DESCENDING;
            tableRows.reverse(sortColumns);
            $("table.sortable thead th:nth-child(" + (columnToSort+1) + ")").addClass("headerSortDown");
        }

        $("div.rowId").remove();
        for ( var i=0; i < tableRows.length; i++ ) {
            $("table.sortable tbody").append( tableRows[i].row );
        }
        showResultId();
    }
    function showResultId() {
        var counter=0;
        $("div.rowId").remove();
        if ( $("table.sortable tbody tr").length > 15 ) {
            $("table.sortable tbody tr:nth-child(5n)").each(function() { $("td:last-child",$(this)).append($("<div></div>").addClass("rowId").html(counter+=5)); });
        }
    }
    $(function() {
        $("table.sortable thead").delegate("th", "click", function () { sortTable($(this).index()); });
        showResultId();
        $(".javaScriptRequired").removeClass("javaScriptRequired");
    });
</script>

    <h1> Your Search Results </h1>
    <div>
        <a href="search.jsp">New Search</a>
        <span class="noprint javaScriptRequired" style="padding-left: 30px;"> <a href="javascript:printDiv();">Print Version</a> </span>
    </div>

    <h3> The following are the results of your <%= accountSearch.describeSearchType() %> search </h3>

    <%  if ( matchedAccounts == null || matchedAccounts.length == 0 ) {
            %><div class="warning">
                Your search did not match any records<br>
                Please review your search criteria and try again<br><br>
            </div>
            <%
        } 

        if ( matchedAccounts.length == maximumSearchRecords ) {
            %><div class="warning">
                Your search matched more records than what are shown below<br>
                If you are unable to find your account in the list below please refine your search
            </div><%
        }
    %>
    <div class="notice">
        When first displayed the results below are sorted by Owner Name &amp; Address<br>
        To sort by another value click the column heading having that label
    </div>

    <div style="display: inline-block; text-align: left;min-width: 1100px;xborder:1px solid black;margin-left: auto; margin-right: auto;">
        <div id="printableTable">
            <style>
                table { border-collapse: collapse; position:relative; }
                table th { vertical-align: bottom; text-align: left; }
                table td { vertical-align: top; text-align: left;  }
                @media print {
                    html { font-size: 12px; }
                    table thead th { font-size: 10px; font-weight: bold; padding: 0px 5px; }
                    table tbody td { font-size: 10px; padding: 5px; }
                    table td div.rowId { display: none; }
                    table caption { display: none; }
                    .noprint { display: none; }
                    .warning { display: none; }
                }
            </style>
            <table border="1" class="sortable">
                <caption><% if (matchedAccounts.length == maximumSearchRecords) {
                                %>Your search matched a significant number of accounts<br>
                                    <span style="font-weight: bold; font-style: italic;">Only the first <%= matchedAccounts.length %> accounts matching your search are displayed</span>
                                    <%
                            } else {
                                %>Your search matched <%= matchedAccounts.length %> accounts<%
                            }
                        %>
                </caption>
                <thead>
                    <tr> <th>Account</th> <th>Owner Name/Address</th> <th>Property&nbsp;Address</th> <th>Legal&nbsp;Description</th> <th>CAD&nbsp;Reference</th> </tr>
                </thead>
                <tbody>
                    <%
                    StringBuilder builder = new StringBuilder();
                    for ( SearchAccount account : matchedAccounts ) {
                        builder.setLength(0);
                        builder.append("<tr>");
                        builder.append(String.format("<td><a href='account.jsp?%s'>%s</a></td>", account.can, account.can));
                        builder.append(String.format("<td>%s</td>", account.getOwner()));
                        builder.append(String.format("<td>%s</td>", account.getProperty()));
                        builder.append(String.format("<td>%s</td>", account.getLegal()));
                        builder.append(String.format("<td>%s</td>", account.aprdistacc));
                        builder.append("</tr>");
                        out.println(builder.toString());
                    }
                    %>
                </tbody>
            </table> 
        </div>
    </div>
<% if ( "debug".equals(request.getQueryString()) ) { %>
<pre style="text-align:left;"><%= accountSearch.trace.toString() %></pre>
<% } %>
<iframe title="Print Version" name="print_frame" width="0" height="0" frameborder="0" src="about:blank"></iframe>
<%@ include file="footer.jsp" %>