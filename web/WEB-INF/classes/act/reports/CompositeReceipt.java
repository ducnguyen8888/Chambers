package act.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CompositeReceipt extends Report {
    public CompositeReceipt() {
        super();
    }


    public static Report initialContext() {
        return new CompositeReceipt();
    }

    public String generateParameterString(Connection con) throws Exception {
        StringBuilder builder = new StringBuilder();


        try ( PreparedStatement ps=con.prepareStatement(
                       "select "
                    +  "     act_utilities.get_client_prefs(?,'INTERNET_CURR_YEAR') taxYear "
                    +  "  from dual"
                    ); 
              ){
            ps.setString(1, clientId);

            try ( ResultSet rs=ps.executeQuery(); ){    
                rs.next();

                reportName          = "composite_receipt";
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

        builder.append(String.format("&P_YEAR=%s", taxYear));

        builder.append("&P_OPERATOR=WEB-PRINT&P_REPRINT=C&P_TAX_CALC=N");

        return builder.toString();
    }
}
