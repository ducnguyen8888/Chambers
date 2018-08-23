<%@ page import="java.util.*,act.util.*,act.ws.account.*,java.text.*,java.math.*,java.time.LocalDate" 
%><%@ include file="/epcwid1/configuration.inc" %><%--

--%><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");

    String accountNumber    = null;
    String ownerNumber      = null;



    // IE       REFER not set on meta tag refresh
    // Edge     REFER set on meta tag refresh but it's still the original account detail page URL
    // Chrome   REFER set on meta tag refresh, set to correct page name
    // FireFox  reportedly similar to IE



    try {
        // REFERER should be defined but won't be in IE/Edge after the page 
        // refresh, after which we should have the account number already. 
        if ( notDefined(request.getHeader("REFERER")) && notDefined(session.getAttribute(request.getRequestURI())) ) {
            request.setAttribute("Error-Reason","Neither referer header or account attribute were set");
            throw INVALID_REQUEST;
        }

        accountNumber = (String) session.getAttribute(request.getRequestURI());
        if ( isDefined(accountNumber) ) {
            session.removeAttribute(request.getRequestURI());

            // If we have a REFERER verify that it's what we expect
            if ( isDefined(request.getHeader("REFERER")) ) {
                if ( ! request.getHeader("REFERER").endsWith(request.getRequestURI())
                    && ! request.getHeader("REFERER").endsWith(accountNumber) ) {
                    request.setAttribute("Error-Reason","Referer was not what was expected");
                    throw INVALID_REQUEST;
                }
            }

            LevyDueByTaxUnit[] levyRecords = LevyDueByTaxUnit.initialContext()
                                                    .setAccount(clientId, accountNumber, ownerNumber)
                                                    .setTimeZoneOffset(timeZoneOffset)
                                                    .retrieve(dataSource);

            request.setAttribute("LevyDueByTaxUnit",levyRecords);
            request.setAttribute("accountNumber",accountNumber);
            %><jsp:include page="levyDetailReport.jsp"/><%
            return;
        }


        // This is the initial request, we'll record our information and inform the user
        // Relying on the REFERER only works if we don't open the report window using JavaScript.
        // If the report window is opened via JavaScript then IE does not set the REFERER.
        accountNumber = nvl(request.getHeader("REFERER")).replaceAll("^([^\\?]*)\\?(.*)$","$2");
        if ( notDefined(accountNumber) || accountNumber.indexOf("?") > 0 ) {
            request.setAttribute("Error-Reason","Account number was not specified");
            throw INVALID_REQUEST;
        }

        session.setAttribute(request.getRequestURI(),accountNumber);
    } catch (Exception exception) {
        //response.sendRedirect("notice-failedToCreate.jsp");
        %><%= exception.toString() %><%
        return;
    }
%><!doctype html>
<html lang="en-us">
<head>
    <meta http-equiv="refresh" content="2;url=<%= request.getRequestURI() %>" />
    <title><Levy Detail Report</title>
    <style>
        html {  font-size: 24px; font-style: italic; }
        .centered { position: fixed; top: 30%; left: 50%; transform: translate(-50%, -50%); }
    </style>
</head>
<body>
<div class="centered">Creating your report, Please wait...</div>
</body>
</html>
<%!
%>