package act.ws.account;

import act.util.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;


public class Exemption {
    public Exemption() {}

    public String   code            = null;
    public String   description     = null;



    /** Client ID of the account to report on */
    public    String        clientId            = null;
    /** Account number (CAN) to report on */
    public    String        accountNumber       = null;
    /** Owner number of the account to report on */
    public    String        ownerNumber         = null;

    /** Tax year to report on */
    public    String        year                = null;


    /** Provides a convenience method for creating a Jurisdiction object, useful when
     *  chaining method calls to retrieve an accounts jurisdiction information.
     *  @return a new Jurisdiction object, useful for chaining method calls
     */
    public static Exemption initialContext() {
        return new Exemption();
    }

    /** Defines the account to report on
     *  <p>Owner will default to "0" if the ownerNumber parameter is null.</p>
     *  @param clientId the client ID of the account to retrieve
     *  @param accountNumber the account number (CAN) of the account to retrieve
     *  @param ownerNumber the account owner number of the account to retrieve, will default to "0"
     *  @return this object, useful for chaining method calls
     */
    public Exemption setAccount(String clientId, String accountNumber, String ownerNumber) {
        this.clientId           = clientId;
        this.accountNumber      = accountNumber;
        this.ownerNumber        = ownerNumber;
        return this;
    }
    /** Defines the year to report on
     *  <p>Year will default to the current INTERNET_CURR_YEAR if the parameter is null.</p>
     *  @param year the tax year to report on
     *  @return this object, useful for chaining method calls
     */
    public Exemption setYear(String year) {
        this.year               = year;
        return this;
    }




    /** Retrieves the exemptions for the configured account
     *
     *  @param dataSource the database data source to retrieve from
     *
     *  @returns String array of exemptions, length will be 0 if no exemptions
     *  @throws Exception if an error occurs retrieving the exemptions
     */
    public Exemption[] retrieve(String dataSource) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return retrieve(con);
        }
    }

    /** Retrieves exemptions for the configured account
     *
     *  @param con              Database connection
     *
     *  @returns String array of exemptions, length will be 0 if no exemptions
     *  @throws Exception if an error occurs retrieving the exemptions
     */
    public Exemption[] retrieve(Connection con) throws Exception {
        ArrayList<Exemption> exemptions = new ArrayList<Exemption>();
    
        try ( PreparedStatement ps = con.prepareStatement(
                                                 "select distinct s.excode as \"code\", nvl(g.description,s.excode) as \"description\" "
                                               + "  from specexem s left outer join global_codeset g on (g.code = s.excode) "
                                               + " where s.client_id=? and s.can=? and s.ownerno=nvl(?,0) "
                                               + "   and s.year=nvl(?,act_utilities.get_client_prefs(s.client_id,'INTERNET_CURR_YEAR')) "
                                               + "   and s.excode not in ('CAP','OSP') " // Don't included CAPPED and OPEN SPACE 
                                               + "   and g.type_code = 'EXCODE' and (g.obsolete_date is null or g.obsolete_date > sysdate) " // PRC 185071
                                               + "   and (s.excode != ('DEF') " // PRC 155981
                                               + "        or (s.excode = ('DEF') "
                                               + "            and (sysdate < nvl(s.end_date,sysdate+1) "
                                               + "                 or (act_utilities.get_client_prefs(s.client_id,'DEF_END_DATE_INCLUDES_180') = 'N' "
                                               + "                     and sysdate < s.end_date+180 "
                                               + "                     ) "
                                               + "                 ) "
                                               + "            ) "
                                               + "        ) "
                                               + " order by s.excode "
                                                );
            ) {
    
            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);
            ps.setString(4, year);
    
            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    Exemption exemption     = new Exemption();
                    exemption.code          = rs.getString("code");
                    exemption.description   = rs.getString("description");
                    exemptions.add(exemption);
                }
            }
        }
    
        return exemptions.toArray(new Exemption[0]);
    }





    /** Retrieves exemptions for the specified account
     *
     *  @param dataSource       the database data source to retrieve from
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *
     *  @returns Exemption array of all exemptions
     *  @throws Exception if an error occurs retrieving the exemptions
     */
    public static Exemption[] getExemptions(String dataSource, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return getExemptions(con, clientId, year, accountNumber, ownerNumber);
        }
    }


    /** Retrieves exemptions for the specified account
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *
     *  @returns Exemption array of all exemptions
     *  @throws Exception if an error occurs retrieving the exemptions
     */
    public static Exemption[] getExemptions(Connection con, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        return Exemption.initialContext()
                           .setAccount(clientId, accountNumber, ownerNumber)
                           .setYear(year)
                           .retrieve(con);
    }


}

