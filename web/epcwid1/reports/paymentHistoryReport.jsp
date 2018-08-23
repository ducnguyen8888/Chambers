<%@ page import="java.util.*,java.text.*,java.math.*,java.time.*,java.time.format.*,act.ws.account.*"
%><%@ include file="/epcwid1/configuration.inc" %><%--

--%><%

PaymentHistory[]    paymentHistory  = (PaymentHistory[]) request.getAttribute("PaymentHistory");
String              accountNumber   = (String) request.getAttribute("accountNumber");


%><!doctype html>
<html lang="en-us">
<head>
    <title>Payment History Report</title>
    <style>
        html { font-family: Arial; font-size: 14px; text-align: center; }
        #header { text-align:center; margin-bottom: 40px; }

        .paymentHistory { text-align:center; margin-bottom: 50px; }
        .paymentHistory sup { color: #A30000 !important; padding: 0px !important; }

        .paymentHistory table { border-collapse: collapse; table-layout: fixed;  margin-right: auto; margin-left: auto; cell-border: 1px; min-width: 550px; }
        .paymentHistory table caption { margin-top: 30px; margin-bottom: 25px; color: black; font-size: 1.2rem; font-weight: bold; text-align: left; padding-left: 12px; }
        .paymentHistory table tr :nth-child(1n) { padding: 0px 12px; }

        .paymentHistory table thead :nth-child(1n) { vertical-align: bottom; font-size: 1.1rem; text-align: left; color: black; }
        .paymentHistory table thead :nth-child(2)  { text-align: right; }

        .paymentHistory table thead tr :nth-child(4) { white-space: nowrap; }

        <% if ( notDefined(accountNumber) ) { %>.paymentHistory table caption { display: none !important; } <% } %>


        /* TBODY border is used to add visual breaks between TBODYs. 
           With multiple TBODYs the shared border between any two will use the settings of the largest width one.
           TBODY border is only displayed when the table uses "border-collapse: collapse;"
        */
        .paymentHistory table tbody { border-top: 10px solid transparent; border-bottom: 25px solid g; }
        .paymentHistory table tbody :nth-child(1n) { vertical-align: top; font-size: 1.0rem; text-align: left; color: darkblue; padding-bottom: 5px; }

        /* Center align date column */
        .paymentHistory table tbody tr :first-child { text-align: center; }

        /* Right align amount column */
        .paymentHistory table tbody tr :nth-child(2) { text-align: right; }

        .paymentHistory table tbody tr:nth-child(3n+4) { border-bottom: 0.8rem solid transparent; }

        @media print {
            html { font-size: 11px !important; }
            #header { display: none !important; }
            h2 { display: none !important; }
            label { display: none !important; }
            .paymentHistory table { min-width: auto !important; }
        }
    </style>
</head>
<body>
    <div id="header">
        <h1> Payment History </h1>
        <div>
            <a href="javascript:window.open('', '_self', ''); window.close();"> Close Window </a>
        </div>
    </div>
    <% 
    if ( paymentHistory == null || paymentHistory.length == 0 ) {
        out.println("<h3 style=\"color:#A30000;\"> No payment history information was found for this account </h3>");
        return;
    }
    %>
    <div class="paymentHistory">
        <div style="clear:both;"></div>
        <%= createReport(accountNumber, paymentHistory) %>
    </div>
</body>
</html>
<%!
    NumberFormat value = new DecimalFormat("$###,###,##0");
    NumberFormat rate  = new DecimalFormat("##0.00000");
    DateTimeFormatter date = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public final NumberFormat money     = NumberFormat.getCurrencyInstance();
    public final String headerFormat    = "<tr><th>%s</th> <th>%s</th> <th>%s</th> <th>%s</th> <th>%s</th></tr>";
    public final String recordFormat    = "<tr><td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td></tr>";


    public String createReport(String accountNumber, PaymentHistory[] paymentHistory) throws Exception {
        StringBuilder   builder         = new StringBuilder();

        builder.append("<table>\n");
        builder.append(String.format("<caption>Account Number: %s</caption>\n", accountNumber));

        builder.append("<thead>\n");
        builder.append(String.format(headerFormat, 
                                    "Receipt Date", 
                                    "Amount", 
                                    "Tax Year", 
                                    "Description", 
                                    "Payer")
                                    );
        builder.append("</thead>\n");

        builder.append("<tbody><tr><td></td></tr>\n");
        for ( PaymentHistory payment : paymentHistory ) {
            builder.append(String.format(recordFormat, 
                                        payment.date.toString(), 
                                        money.format(payment.amount),
                                        payment.yearsApplied,
                                        payment.description,
                                        payment.name
                                        )
                                        );
        }
        builder.append("<tr><td></td></tr></tbody>\n");
        builder.append("</table>\n");

        return builder.toString();
    }
%>