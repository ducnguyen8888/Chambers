<%@ page import="java.util.*, java.io.*, act.util.*,java.sql.*,java.lang.reflect.*,act.ws.search.*,java.text.*,java.time.*,java.time.format.*,act.ws.account.*,java.math.*,java.time.*" 
%><%@ include file="/epcwid1/configuration.inc" %><%--
--%><%

    // Error/exception handling isn't completed yet


    String accountNumber = request.getQueryString();


    if ( notDefined(accountNumber) ) {
        %><jsp:forward page="search.jsp"></jsp:forward><%
        return;
    }


    act.ws.account.Account account = null;
    try {
        if ( false ) {
            %><jsp:forward page="account-InvalidAccount.jsp"></jsp:forward><%
            return;
        }

        account = act.ws.account.Account.initialContext()
                                        .setAccount(clientId, null, accountNumber, null)
                                        .allowConfidentialAccounts(allowRestrictedAccounts)
                                        .limitLastPaymentReported(false)
                                        .load(dataSource);
    } catch (UnsupportedOperationException uoe) {
        // This error occurs when the user hasn't specified any search criteria
        %><jsp:forward page="searchResults-NoMatches.jsp"></jsp:forward><%
        return;
    } catch (SQLException sqlException) {
        if ( false ) {
        %><jsp:forward page="account-InvalidAccount.jsp"></jsp:forward><%
        }
        %><%= sqlException.toString() %><%
        return;
    } catch (Exception exception) {
        %><li> Error: <%= exception.toString() %></li><%
        return;
    }
%><%!
NumberFormat money = NumberFormat.getCurrencyInstance();
NumberFormat value = new DecimalFormat("$###,###,##0");
NumberFormat rate  = new DecimalFormat("##0.00000");
DateTimeFormatter date = DateTimeFormatter.ofPattern("MM/dd/yyyy");

%><%@ include file="header.jsp" %>

<style>
</style>
<noscript>
    <style>
    </style>
</noscript>
<script>
</script>

    <h1> Account Detail </h1>
    <div>
        <a href="search.jsp">New Search</a>
    </div>
    <div style="clear:both;"></div>

    <style>
    xhtml { font-size: 16px; }
    .accountDetail { vertical-align: top; text-align: left; font-size: 13px; xborder: 1px dotted green; width: 850px; margin-left: auto; margin-right:auto; margin-top: 50px; }
    .accountDetail
        .tileColumn { width: 350px; padding: 10px; xborder: 1px solid grey; background-color: white; display: block; float:left; margin-right: 20px; margin-left: 20px; }


    .accountDetail
        .tileGroup  { 
                        border: 1px solid grey; border-radius: 4px; margin: 5px; padding: 10px; background-color: #f7f7f7; 
                    }
    .accountDetail
        .tileGroup.highlight { 
                        background-color: #d9d9d9;
                    }
    .accountDetail
        .tile       { width: 100%; xborder: 1px solid green; min-height: 10px;
                        margin-top: 10px;
                    }
    .accountDetail
        .tile:first-child {
                        margin-top: 0px;
                    }
    .accountDetail
        .tile:last-child { 
                        xcolor: red;
                    }
    .accountDetail
        .tile label {
                        display: block; width: 100%; xborder: 1px solid red; text-align: left; font-weight: bold; font-size: 1.1rem; margin-bottom: 4px;
                        xbackground: grey; padding: 0px 0px; 
                    }
    .accountDetail
        .tile .tileContent {
                        width: 100%; text-align: left; margin-bottom: 10px; font-size: 1.0rem;
                    }
    .accountDetail
        .tile .tileContent div {
                        margin-bottom: 5px; 
                    }
    .accountDetail
        .tile .tileContent:last-child {
                        margin-bottom: 0px; xborder: 1px solid green;
                    }

    .accountDetail
        .tile .tileContent button { 
                        color: #A30000; padding: 5px; cursor: pointer; font-size: 1.2rem; 
                    }
    .accountDetail
        .tile .tileContent button:hover { 
                        font-weight: bold; font-style: italic; letter-spacing: 0.05em; 
                    }

    .accountDetail
        .tile .tileContent.multiColumn {
                        display: table; table-layout: fixed; margin: 2px 0px;
                    }
    .accountDetail
        .tile .tileContent.multiColumn div {
                        display: table-cell; vertical-align: top; border:
        }
    .accountDetail
        .tile .tileContent.multiColumn label {
                        display: table-cell; width: auto; background: transparent; vertical-align: top; border:
        }
    .accountDetail
        .tile .tileContent.multiColumn div > div {
                        display: block; text-align: left;
        }
    .accountDetail
        .tile .tileContent.multiColumn div.amount {
                        text-align: right; 
        }
        .tile .tileContent.highlight div.amount {
                        font-size: 1.2em; font-style: normal;
        }

    .accountDetail
        .tile .tileContent.highlight {
                        font-weight: bold; font-style: italic; margin: 8px 0px;
                    }
    .accountDetail
        .tile .tileContent .footnote { margin: 0em 0.5em; text-indent: -0.7em; font-size: 0.9em; }

    .accountDetail div.noOverflow { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }


    <% if ( false ) { %> .webPayments { display: none; } <% } %>
    <% if ( account.pendingPayments.length == 0 ) { %> .pendingPaymentAmounts { display: none; } <% } %>
    <% if ( account.scheduledPayments.length == 0 ) { %> .scheduledPayments { display: none; } <% } %>


    .accountDetail
    .clear { background: transparent; border: none; }
    .accountDetail sup { color: #A30000; }
    .accountDetail .sup { vertical-align: top; display: inline-block; width: 10px; color: #A30000; font-size: 1.1rem; }

    .accountDetail { text-align: center; }
    .accountDetail
    .notice { background: transparent; border: none; padding: 15px; text-align: center; color: #030303; font-size: 1.1rem; border: 2px ridge grey; display: inline-block; clear:both; font-weight: bold; margin-left: auto; margin-right: auto; margin-bottom: 20px; }

    .accountDetail .tile.special .tileContent { color: #A30000; }
    .accountDetail .tile.special .tileContent a { color: #A30000; text-decoration: none; }
    .accountDetail .tile.special .tileContent a:hover { font-style: italic; }


    .accountDetail .sidePanel { padding: 0px; position: absolute; z-index: 1; border: 1px solid grey; top: 15px; right: -200px; width: 215px; height: 95%; background-color: #f7f7f7; border-radius: 6px; }
    .accountDetail .sidePanel .tile label { padding-left: 2px; border-bottom: 2px solid black; }
    .accountDetail .sidePanel .tile .tileContent a {
                        text-decoration: none; font-size: 1.1rem; color: #1a1a1a; margin: 10px 10px; font-family: Arial; display: block; 
                        text-decoration: underline; color: darkred; 
        }
    .accountDetail .sidePanel .tile .tileContent a:hover {
                        font-style: italic; text-decoration: underline; color: #A30000; color: red;
        }

    </style>
<div class="accountDetail" style="position: relative;">

    <div class="notice">
        Unless otherwise noted all data refers to tax information for <%= account.year %>
        <br>
        All amounts due include penalty, interest, and attorney fees when applicable
    </div>


    <div class="" style="position: relative;">
        <div class="tileColumn">
            <!--
            <div class="tileGroup clear">
                <div class="tile special">
                    <div class="tileContent noOverflow multiColumn highlight">
                        <label style="width: 30%;"> Data Source </label> <div class="amount"> <%= dataSource %> </div>
                    </div>
                    <div class="tileContent noOverflow multiColumn highlight">
                        <label style="width: 30%;"> Client ID </label> <div class="amount"> <%= clientId %> </div>
                    </div>
                    <div class="tileContent noOverflow multiColumn">
                        <label></label>
                        <div class="amount">
                        <a target="_blank" rel="noopener" href="https://actweb.acttax.com/act_webdev/elpaso/showdetail2.jsp?can=<%= account.accountNumber %>">Production URL Link</a>
                        </div>
                    </div>
                </div>
            </div>
            -->
            <div class="tileGroup clear">
                <div class="tile">
                    <div class="tileContent noOverflow multiColumn highlight">
                        <label style="width: 30%;"> Account </label> <div class="amount"> <%= account.accountNumber %> </div>
                    </div>
                </div>
            </div>

            <div class="tileGroup clear">
                <div class="tile">
                    <div class="tileContent multiColumn">
                        <label> CAD Reference </label> <div class="amount"> <%= account.aprdistacc %></div>
                    </div>
                </div>
            </div>

            <div class="tileGroup clear">
                <div class="tile">
                    <label> Owner </label>
                    <div class="tileContent">
                        <%= getOwner(account) %>
                    </div>
                </div>
            </div>

            <div class="tileGroup clear">
                <div class="tile">
                    <label> Property </label>
                    <div class="tileContent">
                        <%= getProperty(account) %>
                    </div>
                </div>
            </div>

            <div class="tileGroup clear">
                <div class="tile">
                    <label> Legal Description </label>
                    <div class="tileContent">
                        <%= getLegal(account) %>
                    </div>
                </div>
            </div>
        </div>

        <div class="tileColumn" style="xmargin-top:10px;">
            <div class="logicGroup webPayments">
                <div class="tileGroup highlight">
                    <div class="tile">
                        <div class="tileContent"> 
                            <button style="width:100%"> Make a Payment </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tileGroup">
                <div class="tile">
                    <div class="tileContent multiColumn"> 
                        <div> Current Year Levy </div>
                        <div class="amount"> <%= money.format(account.currentLevy) %> </div>
                    </div>
                </div>
                <div class="tile">
                    <div class="tileContent multiColumn"> 
                        <div> Current Year Due </div>
                        <div class="amount"> <%= money.format(account.currentDue) %> </div>
                    </div>
                    <div class="tileContent multiColumn"> 
                        <div> Prior Year Due </div>
                        <div class="amount"> <%= money.format(account.priorYearDue) %> </div>
                    </div>
                </div>
                <div class="tile">
                    <div class="tileContent multiColumn highlight"> 
                        <div> Total Amount Due </div>
                        <div class="amount"> <%= money.format(account.totalDue) %> </div>
                    </div>
                </div>
                <div class="logicGroup pendingPayments webPayments">
                    <hr>
                    <div class="tile">
                        <div class="tileContent multiColumn highlight"> 
                            <div style="width: 65%;font-size:1.0rem;"> Pending Internet Payments </div>
                            <div class="amount"> <%= money.format(account.pendingPaymentAmount) %> </div>
                        </div>
                    </div>
                    <div class="logicGroup pendingPaymentAmounts">
                        <div class="tile">
                            <% for ( Payment pending : account.pendingPayments ) { %>
                            <div class="tileContent multiColumn"> 
                                <div> <%= pending.method %> </div>
                                <div> <%= date.format(pending.date) %> </div>
                                <div class="amount"> <%= money.format(pending.amount) %> </div>
                            </div>
                            <% } %>
                        </div>
                        <hr>
                        <div class="tile">
                            <div class="tileContent multiColumn highlight"> 
                                <div> Estimated Due<span class="sup">*</span> </div>
                                <div class="amount"> <%= money.format(account.estimatedTotalDue) %> </div>
                            </div>
                        </div>
                        <div class="tile">
                            <div class="tileContent">
                                <div class="footnote"><sup>*</sup> Estimated due amount is based on your total due balance minus any pending payment amounts</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tileGroup logicGroup scheduledPayments webPayments">
                <div class="tile">
                    <label> Scheduled Payments<span class="sup">*</span> </label>
                    <div class="tileContent">
                        <div class="" style="margin-bottom: 10px;font-size: 0.9rem;">This account has pending scheduled payments </div>
                    </div>
                     <% for ( Payment scheduled : account.scheduledPayments ) { %>
                    <div class="tileContent multiColumn">
                        <div> <%= scheduled.tid %> </div>
                        <div> <%= date.format(scheduled.date) %> </div>
                        <div class="amount"> <%= money.format(scheduled.amount) %> </div>
                    </div>
                    <% } %>
                    <div class="" style="text-align: left;margin-top: 10px;"><a href="#">Manage Scheduled Payments</a></div>
                    <div class="tile">
                        <div class="tileContent">
                            <div class="footnote"><sup>*</sup> The schedule payment amount is the maximum payment amount. The actual payment amount will not exceed the amount owed. The payment date shown is the estimated next payment date.</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tileGroup clear">
                <div class="tile">
                    <label> Last Payment </label>
                    <% if ( account.lastPayment == null ) { %>
                        <div class="tileContent multiColumn">
                            <div> None </div>
                        </div>
                    <% } else { %>
                        <div class="tileContent multiColumn">
                            <div> <%= date.format(account.lastPayment.date) %> </div>
                            <div class="amount"> <%= money.format(account.lastPayment.amount) %> </div>
                        </div>
                        <div class="tileContent">
                            <div class="noOverflow"> <%= account.lastPayment.name %> </div>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
        <div style="clear:both;"></div>


        <div class="tileColumn">
            <div class="tileGroup clear">
                <div class="tile">
                    <div class="tileContent">
                        <label style="width: 100%; font-weight: bold;margin-bottom: 10px;"> Property Valuations </label>
                    </div>
                    <div class="tileContent multiColumn">
                        <div>Land</div>
                        <div class="tileValue amount"> <%= value.format(account.landValue) %> </div>
                    </div>
                    <div class="tileContent multiColumn">
                        <div>Improvement</div>
                        <div class="tileValue amount"> <%= value.format(account.improvementValue) %> </div>
                    </div>
                    <div class="tileContent multiColumn">
                        <div>Market</div>
                        <div class="tileValue amount"> <%= value.format(account.marketValue) %> </div>
                    </div>
                    <div>&nbsp;</div>
                    <div class="tileContent multiColumn">
                        <div>Capped</div>
                        <div class="tileValue amount"> <%= value.format(account.cappedValue) %> </div>
                    </div>
                    <div class="tileContent multiColumn">
                        <div>Agricultural</div>
                        <div class="tileValue amount"> <%= value.format(account.agriculturalValue) %> </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="tileColumn">
            <div class="tileGroup clear">
                <div class="tile">
                    <label> Exemptions </label>
                    <div class="tileContent multiColumn">
                        <% if ( account.exemptions.length == 0 ) { %>
                            <div> None </div>
                        <% } else for ( Exemption exemption : account.exemptions ) { %>
                            <div> <%= exemption.description %> </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
        <div class="tileColumn">
            <div class="tileGroup clear">
                <div class="tile">
                    <div class="tileContent multiColumn">
                        <div>
                            <label> Active Lawsuits </label>
                            <div class="tileContent">
                            <% if ( account.activeCauses.length == 0 ) { %>
                                <div> None </div>
                            <% } else for ( String cause : account.activeCauses ) { %>
                                <div> <%= cause %> </div>
                            <% } %>
                            </div>
                        </div>
                        <div>
                            <label> Bankruptcies </label>
                            <div class="tileContent">
                            <% if ( account.judgements.length == 0 ) { %>
                                <div> None </div>
                            <% } else for ( String cause : account.judgements ) { %>
                                <div> <%= cause %> </div>
                            <% } %>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div style="clear:both;"></div>


        <div style="clear:both;"></div>
        <div style="clear:both;"></div>
        <div class="sidePanel" style="">

            <div class="tileGroup clear" >
                <div class="tile">
                    <div class="tileContent">
                        <label  style=""> Detail Reports </label>
                        <% if ( account.jurisdictions != null && account.jurisdictions.length > 0 ) { %>
                        <div> <a target="_blank" rel="noopener" href="reports/jurisdictionDetail.jsp"> Taxing Jurisdictions </a> </div>
                        <% } %>
                        <% if ( account.totalDue > 0 ) { %>
                        <div> <a target="_blank" rel="noopener" href="reports/levyDetail.jsp">Levy Due Detail</a> </div>
                        <% } %>
                        <div> <a target="_blank" rel="noopener" href="reports/paymentHistory.jsp">Payment History</a> </div>
                        <!-- <div>&nbsp;</div> -->
                        <!-- <div> <a href="#"> Address Correction Request </a> </div> -->
                    </div>
                </div>
            </div>

            <div style="clear:both;"></div>
            <div style="clear:both;"></div>
            <div class="tileGroup clear">
                <div class="tile">
                    <div class="tileContent">
                        <label  style=""> Statements &amp; Receipts </label>
                        <div> <a target="_blank" rel="noopener" href="reports/currentStatement.jsp"> Current Statement </a> </div>
                        <div> <a target="_blank" rel="noopener" href="reports/summaryStatement.jsp"> Summary Statement </a> </div>
                        <div> <a target="_blank" rel="noopener" href="reports/delinquentStatement.jsp"> Delinquent Statement </a> </div>
                        <div> <a target="_blank" rel="noopener" href="reports/compositeStatement.jsp"> Composite Receipt </a> </div>
                    </div>
                </div>
            </div>

        </div>
        <div style="clear:both;"></div>
        <div style="clear:both;"></div>
    </div>



</div>

<%@ include file="footer.jsp" %>
<%!
    public String getOwner(Account account) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(String.format("%s<br>",account.nameline1));

        if ( isDefined(account.nameline2) ) {
            buffer.append(String.format("%s<br>",account.nameline2));
        }
        if ( isDefined(account.nameline3) ) {
            buffer.append(String.format("%s<br>",account.nameline3));
        }
        if ( isDefined(account.nameline4) ) {
            buffer.append(String.format("%s<br>",account.nameline4));
        }

        if ( isDefined(account.city) ) buffer.append(account.city);
        if ( isDefined(account.city) && isDefined(account.state) ) buffer.append(", ");
        if ( isDefined(account.state) ) buffer.append(account.state);
        if ( isDefined(account.city) || isDefined(account.state) ) buffer.append("  ");
        if ( isDefined(account.zipcode) ) buffer.append(account.zipcode);

        return buffer.toString();
    }
    public String getProperty(Account account) {
        return String.format("%s %s",nvl(account.pnumber),nvl(account.pstrname)).trim();
    }

    public String getLegal(Account account) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(String.format("%s<br>",account.legal1));
        for ( String legal : new String[] { account.legal2, account.legal3, account.legal4, account.legal5 } ) {
            if ( isDefined(legal) ) {
                buffer.append(String.format("%s<br>",legal));
            }
        }

        return buffer.toString();
    }
%>
