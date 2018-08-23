package act.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SummaryStatement extends Report {
    public SummaryStatement() {
        super();
    }


    public static Report initialContext() {
        return new SummaryStatement();
    }

    public String generateParameterString(Connection con) throws Exception {
        StringBuilder builder = new StringBuilder();


        try ( PreparedStatement ps=con.prepareStatement(
                       "select to_char(sysdate+(?/24),'YYYYMMDD') asOfDate, "
                    +  "     lower(act_utilities.get_client_prefs(?,'SUMM_STMT_NAME')) reportName, "
                    +  "     upper(act_utilities.get_client_prefs(?,'STMT_O65_MSG_CHECKBOX')) over65Statement, "
                    +  "     act_utilities.get_client_prefs(?,'INTERNET_CURR_YEAR') taxYear "
                    +  "  from dual"
                    ); 
              ){
            ps.setInt   (1, timeZoneOffset);
            ps.setString(2, clientId);
            ps.setString(3, clientId);
            ps.setString(4, clientId);

            try ( ResultSet rs=ps.executeQuery(); ){    
                rs.next();

                asOfDate            = nvl(asOfDate,rs.getString("asOfDate"));
                reportName          = nvl(reportName,rs.getString("reportName"),"delinquent_statement_summary_format2");
                over65Statement     = nvl(over65Statement,rs.getString("over65Statement"));
                taxYear             = nvl(taxYear,rs.getString("taxYear"));
            }
        } catch (Exception exception) {
            executionException = exception;
            throw exception;
        }

        builder.append(String.format("REPORT=%s",reportName));

        builder.append("&P_WEB_CALL='Y'");
        builder.append(String.format("&P_CLIENT_ID=%s&P_CAN=%s&P_OWNERNO=%s",
                                     clientId, accountNumber, (ownerNumber == null ? "0" : ownerNumber)
                                     )
                       );
        builder.append(String.format("&P_ASOFDATE=%s&P_O65_MESSAGE=%s&P_CREATE_NOTES=%s",
                                     asOfDate, over65Statement, tcsNotesFlag
                                     )
                       );


        builder.append(String.format("&P_CURR_YEAR=%s", taxYear));

        builder.append("&P_TITLE=&P_LOW_DOLLAR=0&P_COMMENTS=");
        builder.append("&P_NOTES=&P_ALTERNATE_OWNER=N");
        builder.append("&P_HISTORY_PAGE=N&P_AGENT=N&P_ORDER_BY=8&P_TAXINFO_PAGE=Y");

        return builder.toString();
    }
}
