<%@ page import="java.util.*,java.text.*,java.math.*,java.time.*,act.ws.account.*"
%><%@ include file="/epcwid1/configuration.inc" %><%--

--%><%
LevyDueByTaxUnit[]  levyRecords     = (LevyDueByTaxUnit[]) request.getAttribute("LevyDueByTaxUnit");
String              accountNumber   = (String) request.getAttribute("accountNumber");


%><!doctype html>
<html lang="en-us">
<head>
    <title>Levy Detail Report</title>
    <style>
        html { font-family: Arial; font-size: 14px; text-align: center; }
        #header { text-align:center; margin-bottom: 40px; }

        .levyDue { text-align:center; margin-bottom: 50px; }
        .levyDue sup { color: #A30000 !important; padding: 0px !important; }
        .levyDue .notice { margin-top: 30px; margin-bottom: 15px; color: #A30000; }

        .levyDue table { border-collapse: collapse; table-layout: fixed;  margin-right: auto; margin-left: auto; cell-border: 1px; min-width: 550px; }
        .levyDue table caption { margin-top: 30px; margin-bottom: 25px; color: black; font-size: 1.2rem; font-weight: bold; text-align: left; padding-left: 20px; }
        .levyDue table tr :nth-child(1n) { padding: 0px 8px; }

        .levyDue table thead :nth-child(1n) { vertical-align: bottom; font-size: 1.1rem; text-align: left; color: black; }
        .levyDue table thead tr:first-child th:nth-child(2n+5) { text-align: center; border-bottom: 1px solid #a6a6a6; }

        .levyDue table thead tr:nth-child(2) th:first-child    { text-align: center; padding: 5px 20px 0px; }
        .levyDue table thead tr:nth-child(2) th:nth-child(2)    { min-width: 150px; }
        .levyDue table thead tr:nth-child(2) th:nth-child(1n+3) { text-align: right; }
        .levyDue table thead td:nth-child(3n+4) { width: 15px; }
        .levyDue table thead tr :nth-child(1n+3) { white-space: nowrap; }



        /* TBODY border is used to add visual breaks between TBODYs. 
           With multiple TBODYs the shared border between any two will use the settings of the largest width one.
           TBODY border is only displayed when the table uses "border-collapse: collapse;"
        */
        .levyDue table tbody { border-top: 10px solid transparent; border-bottom: 25px solid g; }
        .levyDue table tbody :nth-child(1n) { vertical-align: top; font-size: 1.0rem; text-align: left; color: darkblue; }

        /* Center Year and add a bit more left-right padding */
        .levyDue table tbody tr :first-child { text-align: center; padding: 0px 20px; }

        /* Prevent jurisdiction column from wrapping */
        .levyDue table tbody tr :nth-child(2) { white-space: nowrap; }

        /* Right aligns all columns after the jusidiction column, these should only be amounts */
        .levyDue table tbody tr :nth-child(1n+3) { text-align: right; }

        .levyDue table tbody tr:nth-child(3n+4) { border-bottom: 0.8rem solid transparent; }
        .levyDue table tbody tr:nth-last-child(-n+3) { border-bottom: none; }


        /* show tax year column on first row only */
        .levyDue table tbody tr:nth-child(2) { background-color: #f7f7f7; }
        .levyDue table tbody tr:nth-child(2) :first-child { color: darkblue; font-weight: bold; }
        .levyDue table tbody tr:nth-child(n+3) :first-child { color: transparent; }

        /* Summary total row settings */
        .levyDue table tbody tr.summaryTotal { border-top: 12px solid transparent; }
        .levyDue table tbody tr.summaryTotal :nth-child(n+2) { color: black; font-size: 14px; font-weight: normal; font-style: italic; }

        /* show year summary only */
        .levyDue #summaryDisplay:checked ~ table tbody tr { display:none; }
        .levyDue #summaryDisplay:checked ~ table tbody tr.summaryTotal th:first-child { color: darkblue; font-weight: bold; }
        .levyDue #summaryDisplay:checked ~ table tr :nth-child(2) { display:none; }

        /* include first/last TR to prevent the TBODY bottom border from being included in the summary row height */
        .levyDue table tbody tr:first-child(-n+2) { display: table-row; }
        .levyDue table tbody tr:nth-last-child(-n+2) { display: table-row !important; }
        .levyDue table tbody tr:last-child { height: 1px; }


        .levyDue table tfoot tr { border-top: 25px solid transparent; }
        .levyDue table tfoot tr :nth-child(1n) { color: #A30000; font-size: 1.1rem; font-weight: bold; font-style: italic; }
        .levyDue table tfoot tr :nth-child(2) { white-space: nowrap; text-align: left; }
        .levyDue table tfoot tr :nth-child(1n+3) { text-align: right; }


        /* toggles report type option */
        .levyDue #summaryDisplay + label, .levyDue #detailDisplay + label  { font-size: 1.1rem; font-weight: bold; }
        .levyDue #summaryDisplay + label:hover span.content, .levyDue #detailDisplay + label:hover span.content { font-size: 1.1rem; font-style: italic; color: red; text-decoration: underline; cursor: pointer; }
        .levyDue #summaryDisplay, .levyDue #detailDisplay { display: none; }
        .levyDue #summaryDisplay + label { display: inline-block; }
        .levyDue #summaryDisplay ~ #detailDisplay + label { display: none; }
        .levyDue #summaryDisplay:checked + label { display: none; }
        .levyDue #summaryDisplay:checked ~ #detailDisplay + label { display: inline-block; }

        .levyDue h2 { clear:both; margin-top: 40px; margin-bottom: 0px; }
        .levyDue #summaryDisplay ~ #header #byYearHeader { display: none; }
        .levyDue #summaryDisplay ~ #header #byJurisdictionHeader { display: inline-block; }

        .levyDue #summaryDisplay:checked ~ #header #byYearHeader { display: inline-block; }
        .levyDue #summaryDisplay:checked ~ #header #byJurisdictionHeader { display: none; }

        .levyDue #summaryDisplay:checked ~ table tbody tr.summaryTotal :nth-child(n+2) { color: darkblue; }

        label .content { color: darkred; text-decoration: underline; }

        @media print {
            html { font-size: 11px !important; }
            #header { display: none !important; }
            h2 { display: none !important; }
            label { display: none !important; }
            .levyDue table { min-width: auto !important; }
        }
    </style>
</head>
<body>
    <div id="header">
        <h1> Levy Due Detail </h1>
        <div>
            <a href="javascript:window.open('', '_self', ''); window.close();"> Close Window </a>
        </div>
    </div>
    <% 
    if ( levyRecords == null || levyRecords.length == 0 ) {
        out.println("<h3 style=\"color:#A30000;\"> No levy due information was found for this account </h3>");
        return;
    }
    %>
    <div class="levyDue">
        <input type="radio" id="summaryDisplay" name="displayType" checked>
        <label for="summaryDisplay">
            <span style="font-size: 1.5rem;">&laquo;</span> <span class="content">Show Levy Due Detail by Year</span>
        </label>
        <input type="radio" id="detailDisplay"  name="displayType">
        <label for="detailDisplay"> 
            <span class="content">Show Levy Due Detail by Jurisdiction</span> <span style="font-size: 1.5rem;">&raquo;</span>
        </label>

        <div style="clear:both;"></div>
        <%= createReport(accountNumber, levyRecords) %>
    </div>
</body>
</html>
<%!

    public final NumberFormat money = NumberFormat.getCurrencyInstance();
    public final String headerFirstFormat    = "<tr><th></th><th></th><th></th><td></td><th colspan=\"2\">by end of<br>%s %s</th></tr>";
    public final String headerSecondFormat   = "<tr><th>%s</th><th>%s</th><th>%s</th><td></td><th><sup>*</sup>%s</th><th>%s</th></tr>";
    public final String jurisdictionFormat   = "<tr><th>%s</th><td>%s</td><td>%s</td><td></td><td>%s</td><td>%s</td></tr>";
    public final String yearlyTotalFormat    = "<tr class=\"summaryTotal\"><th>%s</th><th>%s</th><th>%s</th><td></td><th>%s</th><th>%s</th></tr>";
    public final String summaryTotalFormat   = "<tfoot><tr><th></th><th>%s</th><th>%s</th><td></td><th>%s</th><th>%s</th></tr></tfoot>";

    public String createReport(String accountNumber, LevyDueByTaxUnit[] records) throws Exception {
        StringBuilder   builder         = new StringBuilder();
        LevyDueByTaxUnit   yearlyTotals    = new LevyDueByTaxUnit();
        LevyDueByTaxUnit   summaryTotals   = new LevyDueByTaxUnit();

        builder.append("<div class=\"notice\"><sup>*</sup> Additional Due amount includes Penalties, Interest, and Additional Collection Costs</div>\n");
        builder.append("<table>\n");
        builder.append(String.format("<caption>Account Number: %s</caption>\n", accountNumber));

        builder.append("<thead>\n");
        LocalDate now = LocalDate.now();
        builder.append(String.format(headerFirstFormat,now.getMonth().name(), now.getYear()));
        builder.append(String.format(headerSecondFormat, 
                                    "Year", 
                                    "Jurisdiction", 
                                    "Base Levy Due", 
                                    "Additional Due", 
                                    "Total Due")
                                    );
        builder.append("</thead>\n");

        String currentYear = null;
        for ( LevyDueByTaxUnit record : records ) {
            if ( currentYear == null || ! currentYear.equals(record.year) ) {
                if ( currentYear != null ) {
                    builder.append(String.format(yearlyTotalFormat, 
                                                currentYear,
                                                String.format("Total for %s", currentYear), 
                                                money.format(yearlyTotals.levyDue.doubleValue()), 
                                                money.format(yearlyTotals.additionalDue.doubleValue()), 
                                                money.format(yearlyTotals.totalDue.doubleValue())
                                                )
                                                );
                    summaryTotals.add(yearlyTotals);
                    yearlyTotals.resetValues();

                    builder.append("<tr><td></td></tr></tbody>\n");
                }
                currentYear = record.year;
                builder.append("<tbody><tr><td></td></tr>\n");
            }
            builder.append(String.format(jurisdictionFormat, 
                                        record.year, 
                                        record.name,
                                        money.format(record.levyDue), 
                                        money.format(record.additionalDue), 
                                        money.format(record.totalDue)
                                        )
                                        );
            yearlyTotals.add(record);
        }

        if ( currentYear != null ) {
            builder.append(String.format(yearlyTotalFormat, 
                                        currentYear,
                                        String.format("Total for %s", currentYear), 
                                        money.format(yearlyTotals.levyDue.doubleValue()), 
                                        money.format(yearlyTotals.additionalDue.doubleValue()), 
                                        money.format(yearlyTotals.totalDue.doubleValue())
                                        )
                                        );
            summaryTotals.add(yearlyTotals);
            yearlyTotals.resetValues();

            builder.append("<tr><td></td></tr></tbody>\n");
        }

        builder.append("<tfoot>\n");
        builder.append(String.format(summaryTotalFormat, "Total for All Years", 
                                    money.format(summaryTotals.levyDue.doubleValue()), 
                                    money.format(summaryTotals.additionalDue.doubleValue()), 
                                    money.format(summaryTotals.totalDue.doubleValue())
                                    )
                                    );
        builder.append("</tfoot>\n");
        builder.append("</table>\n");

        return builder.toString();
    }
%>