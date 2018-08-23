<%@ page import="java.util.*,act.util.*" 
%><%@ include file="/epcwid1/configuration.inc" %><%--

--%><%

    String accountNumber = null;

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


            act.reports.Report report = act.reports.DelinquentStatement.initialContext()
                                            .setAccount(clientId, accountNumber)
                                            //.setTCSNotes("Y")
                                            .create(dataSource);
            if ( report.wasSuccessful() ) {
                response.sendRedirect(String.format("%s%s", nvl(reportServerURI), report.getReportURI()));
                return;
            }

            request.setAttribute("Error-Reason","Report not successfully created");
            throw INVALID_REQUEST;
        }

        // This is the initial request, we'll record our information and inform the user
        accountNumber = nvl(request.getHeader("REFERER")).replaceAll("^([^\\?]*)\\?(.*)$","$2");
        if ( notDefined(accountNumber) || accountNumber.indexOf("?") > 0 ) {
            request.setAttribute("Error-Reason","Account number was not specified");
            throw INVALID_REQUEST;
        }

        session.setAttribute(request.getRequestURI(),accountNumber);
    } catch (Exception e) {
        response.sendRedirect("notice-failedToCreate.jsp");
        return;
    }
%><!doctype html>
<html lang="en-us">
<head>
    <meta http-equiv="refresh" content="2;url=<%= request.getRequestURI() %>" />
    <title>Delinquent Statement</title>
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
