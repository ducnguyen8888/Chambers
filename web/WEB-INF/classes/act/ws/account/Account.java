package act.ws.account;

import act.util.Connect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

public class Account implements Comparable<Account> {
    public Account() {}

    public Account(String dataSource, boolean allowConfidentialAccounts,
                   String clientId, String year, 
                   String accountNumber, String ownerNumber) throws Exception {
        this.allowConfidentialAccounts(allowConfidentialAccounts)
            .setAccount(clientId, year, accountNumber, ownerNumber)
            .load(dataSource);
    }
    public Account(Connection con, boolean allowConfidentialAccounts,
                   String clientId, String year, 
                   String accountNumber, String ownerNumber) throws Exception {
        this.allowConfidentialAccounts(allowConfidentialAccounts)
            .setAccount(clientId, year, accountNumber, ownerNumber)
            .load(con);
    }

    public int compareTo(Account compareAccount) {
        if ( accountNumber == null ) {
            return (compareAccount.accountNumber == null ? 0 : -1);
        }
        return (compareAccount.accountNumber == null ? 1 : accountNumber.compareTo(compareAccount.accountNumber));
    }


    public String           clientId                    = null,
                            year                        = null,
                            accountNumber               = null,
                            ownerNumber                 = null;


    public String           nameline1                   = null,
                            nameline2                   = null,
                            nameline3                   = null,
                            nameline4                   = null,

                            city                        = null,
                            state                       = null,
                            zipcode                     = null,

                            pnumber                     = null,
                            pstrname                    = null,

                            legal1                      = null,
                            legal2                      = null,
                            legal3                      = null,
                            legal4                      = null,
                            legal5                      = null,

                            aprdistacc                  = null,
                            acctstatus                  = null;

    public boolean          isConfidential              = false;
    public boolean          hasBalanceDue               = false;

    public double           currentLevy                 = 0.0;
    public double           currentDue                  = 0.0;
    public double           totalDue                    = 0.0;
    public double           priorYearDue                = 0.0;

    public double           pendingPaymentAmount        = 0.0;
    public double           estimatedTotalDue           = 0.0;

    public int              marketValue                 = 0;
    public int              landValue                   = 0;
    public int              improvementValue            = 0;
    public int              cappedValue                 = 0;
    public int              agriculturalValue           = 0;

    public Payment[]        pendingPayments             = null;
    public Payment[]        scheduledPayments           = null;
    public Payment          lastPayment                 = null;

    public Exemption[]      exemptions                  = null;

    public String[]         activeCauses                = null;
    public String[]         judgements                  = null;
    public Jurisdiction[]   jurisdictions               = null;

    public boolean          limitLastPaymentReported    = true;


    /** Returns whether this account is a confidential account base on the special_status field = 'Y'
     *  @returns true if this account is a confidential account, otherwise false
     */
    public boolean isConfidential() { return isConfidential; }


    /** Returns whether this account has a balance due or not
     *  @returns true if this account has a levy balance due, otherwise false
     */
    public boolean hasBalanceDue() { return hasBalanceDue || totalDue > 0; }


    /** Returns a property value as an integer value, returns default value if value isn't an integer value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public String getString(String value, String defaultValue) {
        return (value == null ? defaultValue : value);
    }


    /** Returns a property value as an integer value, returns default value if value isn't an integer value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public int getInt(String property, int defaultValue) {
        try {
            return Integer.parseInt(property);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    
    /** Returns a property value as a long value, returns default value if value isn't a long value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public long getLong(String property, long defaultValue) {
        try {
            return Long.parseLong(property);
        } catch (Exception e) {
        }
        return defaultValue;
    }


    /** Returns a property value as a double value, returns default value if value isn't a double value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public double getDouble(String property, double defaultValue) {
        try {
            return Double.parseDouble(property);
        } catch (Exception e) {
        }
        return defaultValue;
    }


    /** Returns a property value as a boolean true/false value, value must be "true" or "Y" to return true
     * @param property property to return the boolean value of
     * @returns true if property value is "TRUE" or "Y", false otherwise
     */
    public boolean isTrue(String property) {
        return "true".equalsIgnoreCase(property) || "Y".equalsIgnoreCase(property);
    }


    /**Sets the public fields from the property values.
     * Only base type values are set, int, long, double, boolean, and String
     * @throws Exception if a processing error occurs
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();


        Field [] fields = this.getClass().getDeclaredFields();
        for ( int i=0; i < fields.length; i++ ) {
            if ( (fields[i].getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC ) continue;
            Class  classType  = fields[i].getType();
            String fieldName  = fields[i].getName();
            String fieldValue = null;

            // Field may not be part of result set
            try {
                fieldValue = fields[i].toString();
            } catch (Exception e) {
                continue;
            }


            try {
                if ( classType.equals(java.lang.Integer.TYPE) ) {
                    fieldValue = ""+fields[i].getInt(this);
                } else if ( classType.equals(java.lang.Boolean.TYPE) ) {
                    fieldValue = ""+fields[i].getBoolean(this);
                } else if ( classType.equals(java.lang.Long.TYPE) ) {
                    fieldValue = ""+fields[i].getLong(this);
                } else if ( classType.equals(java.lang.Double.TYPE) ) {
                    fieldValue = ""+fields[i].getDouble(this);
                } else if ( classType.getName().equals("java.lang.String") ) {
                    fieldValue = (String) fields[i].get(this);
                }
            } catch (Exception e) {
                fieldValue = e.toString();
            }


            builder.append(String.format("%15s: %s\n", fieldName, fieldValue));
        }

        return builder.toString();
    }

    public static String nvl(String... values) {
        if ( values == null ) return "";
        for ( String value : values ) {
            if ( value != null ) return value;
        }
        return "";
    }
    public static int nvl(int defaultValue, String... values) {
        if ( values == null ) return defaultValue;
        for ( String value : values ) {
            try { return Integer.parseInt(value); } catch (Exception e) {}
        }
        return defaultValue;
    }
    public static long nvl(long defaultValue, String... values) {
        if ( values == null ) return defaultValue;
        for ( String value : values ) {
            try { return Long.parseLong(value); } catch (Exception e) {}
        }
        return defaultValue;
    }
    public static double nvl(double defaultValue, String... values) {
        if ( values == null ) return defaultValue;
        for ( String value : values ) {
            try { return Double.parseDouble(value); } catch (Exception e) {}
        }
        return defaultValue;
    }

    public boolean hasActiveCauses() { return activeCauses != null && activeCauses.length > 0; }
    public boolean hasJudgements()   { return judgements   != null && judgements.length > 0; }
    public boolean hasExemptions()   { return exemptions   != null && exemptions.length > 0; }
    public boolean hasPendingPayments() { return pendingPayments != null && pendingPayments.length > 0; }
    public boolean hasScheduledPayments() { return scheduledPayments != null && scheduledPayments.length > 0; }



    public static Account initialContext() {
        return new Account();
    }


    public      long                        loadStartTime               = 0;
    public      long                        loadEndTime                 = 0;
    public int duration() {
        return (int) (loadEndTime > loadStartTime ? (loadEndTime - loadStartTime) : 0);
    }

    protected   boolean     loadConfidentialAccounts    = false;
    public Account allowConfidentialAccounts(boolean allowConfidentialAccounts) {
        this.loadConfidentialAccounts = allowConfidentialAccounts;
        return this;
    }
    /** Set the the account that is to be to retrieved */
    public Account setAccount(String clientId, String accountNumber) {
        return setAccount(clientId, null, accountNumber, null);
    }
    /** Set the the account that is to be to retrieved */
    public Account setAccount(String clientId, String year, String accountNumber) {
        return setAccount(clientId, year, accountNumber, null);
    }
    /** Set the the account that is to be to retrieved */
    public Account setAccount(String clientId, String year, String accountNumber, String ownerNumber) {
        this.clientId       = clientId;
        this.year           = year;
        this.accountNumber  = accountNumber;
        this.ownerNumber    = ownerNumber;
        return this;
    }


    /** Sets whether the last payment retrieved will be limited to payments for
     *  the current tax year or for any year
     *  @param limitLastPaymentReported true if looking at last payment for current tax year only,
     *                          false if payment is for any tax year
     */
    public Account limitLastPaymentReported(boolean limitLastPaymentReported) {
        this.limitLastPaymentReported   = limitLastPaymentReported;
        return this;
    }


    public Account load(String dataSource) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            load(con);
        }
        return this;
    }
    public Account load(Connection con) throws Exception {
        loadStartTime = System.currentTimeMillis();

        try ( PreparedStatement ps = con.prepareStatement(
                          " select /*+ index(o OWNER_CAN_IX) index(t taxdtl_pk) */ "
                        + " o.client_id as \"clientId\", o.year, o.can as \"accountNumber\", o.ownerno as \"ownerNumber\", "
                        + " o.special_status as \"isConfidential\", t.acctstatus, "

                        + " case when o.special_status = 'Y' "
                        + "        then nvl(act_utilities.get_client_prefs(o.client_id,'CONF_NAME_TO_SHOW'),'UNKNOWN') "
                        + "        else o.nameline1 "
                        + " end as \"nameline1\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.nameline2 end as \"nameline2\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.nameline3 end as \"nameline3\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.nameline4 end as \"nameline4\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.city end as \"city\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.state end as \"state\", "
                        + " case when o.special_status = 'Y' and pref.showAddress != 'Y' then null else o.zipcode end as \"zipcode\", "

                        + " t.pnumber, t.pstrname, t.aprdistacc, "

                        + " case when o.special_status = 'Y' and pref.showLegal != 'Y' then null else t.legal1 end as \"legal1\", "
                        + " case when o.special_status = 'Y' and pref.showLegal != 'Y' then null else t.legal2 end as \"legal2\", "
                        + " case when o.special_status = 'Y' and pref.showLegal != 'Y' then null else t.legal3 end as \"legal3\", "
                        + " case when o.special_status = 'Y' and pref.showLegal != 'Y' then null else t.legal4 end as \"legal4\", "
                        + " case when o.special_status = 'Y' and pref.showLegal != 'Y' then null else t.legal5 end as \"legal5\", "

                        + " levy(p_client_id=>o.client_id, p_can=>o.can, p_ownerno=>o.ownerno, p_year=>o.year,p_rectype=>'TL') as \"currentLevy\", "
                        + " website.levydue(o.client_id,o.can,o.ownerno,o.year) as \"currentDue\", "
                        + " website.levydue(o.client_id,o.can,o.ownerno)-website.levydue(o.client_id,o.can,o.ownerno,o.year+1) as \"totalDue\" "

                        + " from owner o "
                        + "      join taxdtl t on (t.client_id=o.client_id and t.year=o.year and t.can=o.can), "
                        + "      (select nvl(act_utilities.get_client_prefs(?,'CONF_ADDR_TOSHOW'),'N') showAddress, "
                        + "              nvl(act_utilities.get_client_prefs(?,'CONF_LEGAL_TOSHOW'),'N') showLegal, "
                        + "              ','||replace(trim(nvl(act_utilities.get_client_prefs(?,'ACCT_STATUSES_REMOVE_FROM_WEB'),'')), chr(32), '')||',' excludeStatuses "
                        + "         from dual) pref "

                        + " where o.client_id=? and o.can=? and o.ownerno=nvl(?,0) "
                        + " and o.year = nvl(?,act_utilities.get_client_prefs(o.client_id,'INTERNET_CURR_YEAR')) "
                        + " and (o.web_suppress <> 'Y' or o.web_suppress is null) "
                        + " and (o.client_id = 94000000 or o.special_status <> 'Y' or o.special_status is null or o.special_status = ?) "
                        + " and instr(pref.excludeStatuses,','||t.acctstatus||',') = 0 "
                        );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, clientId);
            ps.setString(3, clientId);

            ps.setString(4, clientId);
            ps.setString(5, accountNumber);
            ps.setString(6, ownerNumber);

            ps.setString(7, year);

            ps.setString(8, (loadConfidentialAccounts ? "Y" : "N"));

            try ( ResultSet rs = ps.executeQuery(); ) {
                rs.next();

                year            = rs.getString("year");
                ownerNumber     = rs.getString("ownerNumber");

                nameline1       = nvl(rs.getString("nameline1"));
                nameline2       = nvl(rs.getString("nameline2"));
                nameline3       = nvl(rs.getString("nameline3"));
                nameline4       = nvl(rs.getString("nameline4"));
                city            = nvl(rs.getString("city"));
                state           = nvl(rs.getString("state"));
                zipcode         = nvl(rs.getString("zipcode"));

                pnumber         = nvl(rs.getString("pnumber"));
                pstrname        = nvl(rs.getString("pstrname"));

                legal1          = nvl(rs.getString("legal1"));
                legal2          = nvl(rs.getString("legal2"));
                legal3          = nvl(rs.getString("legal3"));
                legal4          = nvl(rs.getString("legal4"));
                legal5          = nvl(rs.getString("legal5"));

                aprdistacc      = nvl(rs.getString("aprdistacc"));

                acctstatus      = nvl(rs.getString("acctstatus"));
                isConfidential  = "Y".equals(rs.getString("isConfidential"));

                currentLevy     = nvl(0.0,rs.getString("currentLevy"));
                currentDue      = nvl(0.0,rs.getString("currentDue"));
                totalDue        = nvl(0.0,rs.getString("totalDue"));

                if ( totalDue > currentDue ) {
                    priorYearDue = new BigDecimal(totalDue-currentDue)
                                        .setScale(12, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                }

                loadValuations(con);

                activeCauses        = getActiveCauses(con, clientId, year, accountNumber, ownerNumber);
                judgements          = getJudgements(con, clientId, year, accountNumber, ownerNumber);

                jurisdictions       = Jurisdiction.initialContext()
                                                  .setAccount(clientId, accountNumber, ownerNumber)
                                                  .setYear(year)
                                                  .retrieve(con);

                exemptions          = Exemption.getExemptions(con, clientId, year, accountNumber, ownerNumber);

                lastPayment         = (limitLastPaymentReported ? Payment.getLastPaymentForCurrentYear(con, clientId, accountNumber, ownerNumber)
                                                                : Payment.getLastPayment(con, clientId, accountNumber, ownerNumber)
                                                                );
                pendingPayments     = Payment.getPendingPayments(con, clientId, accountNumber, ownerNumber);
                scheduledPayments   = Payment.getScheduledPayments(con, clientId, accountNumber, ownerNumber);


                estimatedTotalDue = totalDue;
                pendingPaymentAmount = 0.0;
                if ( pendingPayments.length > 0 ) {
                    BigDecimal pending = new BigDecimal("0.00");
                    for ( Payment payment : pendingPayments ) {
                        pending = pending.add(new BigDecimal(payment.amount));
                    }
                    pendingPaymentAmount = pending
                                            .setScale(12, BigDecimal.ROUND_HALF_UP)
                                            .doubleValue();
                    estimatedTotalDue = (new BigDecimal(totalDue))
                                            .subtract(pending)
                                            .setScale(12, BigDecimal.ROUND_HALF_UP)
                                            .doubleValue();
                    estimatedTotalDue = Math.max(estimatedTotalDue,0.0);
                }
            }
        } catch (Exception exception) {
            throw exception;
        } finally {
            loadEndTime = System.currentTimeMillis();
        }

        return this;
    }




    /** Retrieves active causes (legaldtl.legalStatus values 'A' and 'L') for the specified account
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *
     *  @returns String array of legal causes, length will be 0 if no active causes
     *  @throws Exception if an error occurs retrieving the active causes
     */
    public static String[] getActiveCauses(Connection con, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        ArrayList<String> activeCauses = new ArrayList<String>();

        try ( PreparedStatement ps = con.prepareStatement(
                                                      "select distinct causeno as \"causeNumber\" "
                                                    + "  from legaldtl "
                                                    + " where client_id=? and can=? and ownerno=nvl(?,0) "
                                                    + "   and year=nvl(?,act_utilities.get_client_prefs(client_id,'INTERNET_CURR_YEAR')) "
                                                    + "   and causeno > ' ' and legalStatus in ('A','L') "
                                                    + " order by causeno"
                                                    );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);
            ps.setString(4, year);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    activeCauses.add(rs.getString("causeNumber"));
                }
            }
        }

        return activeCauses.toArray(new String[0]);
    }


    /** Retrieves judgements, or bankruptcies, (legaldtl.legalStatus values 'J') for the specified account
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param year             Tax year, defaults to current internet tax year if null specified
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number, defaults to 0 if null specified
     *
     *  @returns String array of judgements, length will be 0 if no judgements
     *  @throws Exception if an error occurs retrieving the judgements
     */
    public static String[] getJudgements(Connection con, String clientId, String year, String accountNumber, String ownerNumber) throws Exception {
        ArrayList<String> judgements = new ArrayList<String>();

        try ( PreparedStatement ps = con.prepareStatement(
                                                      "select distinct causeno as \"judgementNumber\" "
                                                    + "  from legaldtl "
                                                    + " where client_id=? and can=? and ownerno=nvl(?,0) "
                                                    + "   and year=nvl(?,act_utilities.get_client_prefs(client_id,'INTERNET_CURR_YEAR')) "
                                                    + "   and causeno > ' ' and legalStatus in ('J') "
                                                    + " order by causeno"
                                                    );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);
            ps.setString(4, year);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    judgements.add(rs.getString("judgementNumber"));
                }
            }
        }

        return judgements.toArray(new String[0]);
    }


    protected void loadValuations(Connection con) throws Exception {
        try ( PreparedStatement ps = con.prepareStatement(
                                                      "select nvl(holand,0)+nvl(agmkt,0)+nvl(nqland,0) as \"landValue\", "
                                                    + "       nvl(hoimp,0)+nvl(agimp,0)+nvl(nqimp,0) as \"improvementValue\", "
                                                    + "       nvl(homcap,0) as \"cappedValue\", nvl(aguse,0) as \"agriculturalValue\", "
                                                    + "       marketValue "
                                                    + "  from  valdtl "
                                                    + "        join (select client_id, year, can, nvl(max(grossval),0) marketValue "
                                                    + "                from receivable "
                                                    + "               where rectype='TL' and ownerno=nvl(?,0) "
                                                    + "               group by client_id, year, can "
                                                    + "               ) r on (r.client_id=valdtl.client_id and r.year=valdtl.year and r.can=valdtl.can) "
                                                    + " where valdtl.client_id=? and valdtl.can=? "
                                                    + "   and valdtl.year=nvl(?,act_utilities.get_client_prefs(valdtl.client_id,'INTERNET_CURR_YEAR')) "
                                                    );
            ) {

            ps.setString(1, ownerNumber);
            ps.setString(2, clientId);
            ps.setString(3, accountNumber);
            ps.setString(4, year);

            try ( ResultSet rs = ps.executeQuery(); ) {
                // Some properties don't have VALDTL records
                if ( rs.next() ) {
                    marketValue         = rs.getInt("marketValue");
                    landValue           = rs.getInt("landValue");
                    improvementValue    = rs.getInt("improvementValue");
                    cappedValue         = rs.getInt("cappedValue");
                    agriculturalValue   = rs.getInt("agriculturalValue");
                } else {
                    marketValue = landValue = improvementValue = cappedValue = agriculturalValue = 0;
                }
            } catch (Exception e) {
                throw new Exception(
                          String.format("(%s)(%s)(%s)(%s): %s",
                                        clientId, year, accountNumber, ownerNumber,
                                        e.toString())
                          );
            }
        }

        return;
    }



}

