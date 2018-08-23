package act.ws.account;

import act.util.Connect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

public class Jurisdiction {
    public Jurisdiction() {}

    public Jurisdiction(String taxUnit, String name) {
        this.taxUnit    = taxUnit;
        this.name       = name;
    }

    /** Tax Unit of the jurisdiction */
    public String   taxUnit         = null;
    /** Name of the jurisdiction */
    public String   name            = null;

    /** Jurisdiction tax rate */
    public double   taxRate         = 0.0;
    /** The levy amount imposed by this Jurisdiction for this account */
    public double   levy            = 0.0;

    /** The account gross value used by this Jurisdiction */
    public int      grossValue      = 0;
    /** The account taxable value used by this Jurisdiction, should be grossValue - exemptionValue */
    public int      taxableValue    = 0;
    /** The account exemption value used by this Jurisdiction */
    public int      exemptionValue  = 0;




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
    public static Jurisdiction initialContext() {
        return new Jurisdiction();
    }

    /** Defines the account to report on
     *  <p>Owner will default to "0" if the ownerNumber parameter is null.</p>
     *  @param clientId the client ID of the account to retrieve
     *  @param accountNumber the account number (CAN) of the account to retrieve
     *  @param ownerNumber the account owner number of the account to retrieve, will default to "0"
     *  @return this object, useful for chaining method calls
     */
    public Jurisdiction setAccount(String clientId, String accountNumber, String ownerNumber) {
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
    public Jurisdiction setYear(String year) {
        this.year               = year;
        return this;
    }



    /** Retrieves taxing jurisdictions for the configured account
     *
     *  @param dataSource the database data source to retrieve from
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public Jurisdiction[] retrieve(String dataSource) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return retrieve(con);
        }
    }

    /** Retrieves taxing jurisdictions for the configured account
     *
     *  @param con              Database connection
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public Jurisdiction[] retrieve(Connection con) throws Exception {
        ArrayList<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();

        try ( PreparedStatement ps = con.prepareStatement(
                                              "select /*+ index( j jurisdiction_pk ) */ "
                                            + "         r.taxunit, j.name, "
                                            + "         j.total_taxrate as \"taxRate\", sum(levy) as \"levy\", "
                                            + "         max(grossVal) as \"grossValue\", max(taxValue) as \"taxableValue\",  "
                                            + "         max(grossVal)-max(taxValue) as \"exemptionValue\" "
                                            + " from receivable r "
                                            + "      join jurisdiction j on (j.client_id=r.client_id and j.year=r.year and j.taxunit=r.taxunit) "
                                            + " where r.client_id=? and r.can=? and r.ownerno=nvl(?,0) "
                                            + "   and r.year=nvl(?,act_utilities.get_client_prefs(r.client_id,'INTERNET_CURR_YEAR')) "
                                            + "   and r.taxunit < 8000 "
                                            + " group by r.taxunit, j.name, j.total_taxrate "
                                            + " order by j.name "
                                            );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);
            ps.setString(4, year);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    Jurisdiction jurisdiction = new Jurisdiction(rs.getString("taxUnit"), rs.getString("name"));
                    jurisdiction.taxRate        = Double.parseDouble(rs.getString("taxRate"));
                    jurisdiction.levy           = Double.parseDouble(rs.getString("levy"));
                    jurisdiction.grossValue     = rs.getInt("grossValue");
                    jurisdiction.taxableValue   = rs.getInt("taxableValue");
                    jurisdiction.exemptionValue = rs.getInt("exemptionValue");

                    jurisdictions.add(jurisdiction);
                }
            }
        }

        return jurisdictions.toArray(new Jurisdiction[0]);
    }


    /** Retrieves taxing jurisdictions for the specified account
     *
     *  @param dataSource       the database data source to retrieve from
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public static Jurisdiction[] getJurisdictions(String dataSource, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return getJurisdictions(con, clientId, year, accountNumber, ownerNumber);
        }
    }
    /** Retrieves taxing jurisdictions for the specified account
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public static Jurisdiction[] getJurisdictions(Connection con, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        return Jurisdiction.initialContext()
                           .setAccount(clientId, accountNumber, ownerNumber)
                           .setYear(year)
                           .retrieve(con);
    }

    /** Retrieves all available jurisdictions for the specified client
     *
     *  @param dataSource       the database data source to retrieve from
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public static Jurisdiction[] getJurisdictions(String dataSource, String clientId, String year) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return getJurisdictions(con, clientId, year);
        }
    }
    /** Retrieves all available jurisdictions for the specified client
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *
     *  @returns String array of jurisdictions, length will be 0 if no jurisdictions
     *  @throws Exception if an error occurs retrieving the jurisdictions
     */
    public static Jurisdiction[] getJurisdictions(Connection con, String clientId, String year) throws Exception {
        ArrayList<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();

        try ( PreparedStatement ps = con.prepareStatement(
                                                      "select /*+ index( jurisdiction_pk ) */ "
                                                    + "       distinct j.taxUnit, j.name "
                                                    + "  from jurisdiction "
                                                    + " where client_id=? "
                                                    + "   and year=nvl(?,act_utilities.get_client_prefs(r.client_id,'INTERNET_CURR_YEAR')) "
                                                    + "   and (taxUnit < 8000 or (taxUnit=9911 and clientId=100000000)) "
                                                    + " order by name"
                                                    );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, year);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    jurisdictions.add(new Jurisdiction(rs.getString("taxUnit"), rs.getString("name")));
                }
            }
        }

        return jurisdictions.toArray(new Jurisdiction[0]);
    }
}
