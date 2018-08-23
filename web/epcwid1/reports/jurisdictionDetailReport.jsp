<%@ page import="java.util.*,java.text.*,java.math.*,java.time.*,java.time.format.*,act.ws.account.*"
%><%@ include file="/epcwid1/configuration.inc" %><%--

--%><%

Jurisdiction[]      jurisdictions   = (Jurisdiction[]) request.getAttribute("Jurisdiction");
String              accountNumber   = (String) request.getAttribute("accountNumber");


%><!doctype html>
<html lang="en-us">
<head>
    <title>Jurisdiction Detail Report</title>
    <style>
        html { font-family: Arial; font-size: 14px; text-align: center; }
        #header { text-align:center; margin-bottom: 40px; }

        .jurisdictionDetail { text-align:center; margin-bottom: 50px; }
        .jurisdictionDetail sup { color: #A30000 !important; padding: 0px !important; }

        .jurisdictionDetail table { border-collapse: collapse; table-layout: fixed;  margin-right: auto; margin-left: auto; cell-border: 1px; min-width: 550px; }
        .jurisdictionDetail table caption { margin-top: 30px; margin-bottom: 25px; color: black; font-size: 1.2rem; font-weight: bold; text-align: left; padding-left: 12px; }
        .jurisdictionDetail table tr :nth-child(1n) { padding: 0px 12px; text-align: right; }

        .jurisdictionDetail table thead :nth-child(1n) { vertical-align: bottom; font-size: 1.1rem; color: black; }
        .jurisdictionDetail table thead :first-child  { text-align: left; }

        .jurisdictionDetail table thead tr :nth-child(4) { white-space: nowrap; }

        <% if ( notDefined(accountNumber) ) { %>.jurisdictionDetail table caption { display: none !important; } <% } %>


        /* TBODY border is used to add visual breaks between TBODYs. 
           With multiple TBODYs the shared border between any two will use the settings of the largest width one.
           TBODY border is only displayed when the table uses "border-collapse: collapse;"
        */
        .jurisdictionDetail table tbody { border-top: 10px solid transparent; border-bottom: 25px solid g; }
        .jurisdictionDetail table tbody :nth-child(1n) { vertical-align: top; font-size: 1.0rem; color: darkblue; padding-bottom: 5px; }

        /* Left align Jurisdiction name column */
        .jurisdictionDetail table tbody tr :first-child { text-align: left; }

        .jurisdictionDetail table tbody tr:nth-child(3n+4) { border-bottom: 0.8rem solid transparent; }

        @media print {
            html { font-size: 11px !important; }
            #header { display: none !important; }
            h2 { display: none !important; }
            label { display: none !important; }
            .jurisdictionDetail table { min-width: auto !important; }
        }
    </style>
</head>
<body>
    <div id="header">
        <h1> Jurisdiction Detail </h1>
        <div>
            <a href="javascript:window.open('', '_self', ''); window.close();"> Close Window </a>
        </div>
    </div>
    <% 
    if ( jurisdictions == null || jurisdictions.length == 0 ) {
        out.println("<h3 style=\"color:#A30000;\"> No jurisdiction information was found for this account </h3>");
        return;
    }
    %>
    <div class="jurisdictionDetail">
        <div style="clear:both;"></div>
        <%= createReport(accountNumber, jurisdictions) %>
    </div>
</body>
</html>
<%!
    public final NumberFormat money     = NumberFormat.getCurrencyInstance();
    public final NumberFormat value     = new DecimalFormat("$###,###,##0");
    public final NumberFormat rate      = new DecimalFormat("##0.00000");
    public final DateTimeFormatter date = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public final String headerFormat    = "<tr><th>%s</th> <th>%s</th> <th>%s</th> <th>%s</th> <th>%s</th> <th>%s</th></tr>";
    public final String recordFormat    = "<tr><td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td></tr>";


    public String createReport(String accountNumber, Jurisdiction[] jurisdictions) throws Exception {
        StringBuilder   builder         = new StringBuilder();

        builder.append("<table>\n");
        builder.append(String.format("<caption>Account Number: %s</caption>\n", accountNumber));

        builder.append("<thead>\n");
        builder.append(String.format(headerFormat, 
                                    "Name", 
                                    "Market<br>Value", 
                                    "Exemption", 
                                    "Taxable<br>Value", 
                                    "Tax Rate",
                                    "Levy"
                                    )
                                    );
        builder.append("</thead>\n");

        builder.append("<tbody><tr><td></td></tr>\n");
        for ( Jurisdiction jurisdiction : jurisdictions ) {
            builder.append(String.format(recordFormat, 
                                        jurisdiction.name, 
                                        value.format(jurisdiction.grossValue) ,
                                        value.format(jurisdiction.exemptionValue),
                                        value.format(jurisdiction.taxableValue),
                                        rate.format(jurisdiction.taxRate),
                                        money.format(jurisdiction.levy)
                                        )
                                        );
        }
        builder.append("<tr><td></td></tr></tbody>\n");
        builder.append("</table>\n");

        return builder.toString();
    }
%>