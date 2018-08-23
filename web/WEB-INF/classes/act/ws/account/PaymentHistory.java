package act.ws.account;

import act.util.Connect;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Hashtable;


/** PaymentHistory class is used to retrieve the payment history for a specific account.
 *
 *  <p>Example Usage (with optional configuration methods):</p>
 *  <pre>
 *      PaymentHistory[] payments = PaymentHistory.initialContext()
 *                                                .setAccount(clientId, accountNumber, ownerNumber)
 *                                                .setPaymentYear("1990")
 *                                                .setMinAppliedYear("1980")
 *                                                .setIgnoreStatuses("DA,IT")
 *                                                .setEscrowStatuses("RF,RD")
 *                                                .initializeTranslationTable()
 *                                                .setStatusTranslation("PA","Payment")
 *                                                .setStatusTranslation("RF","Refunded")
 *                                                .retrieve("jdbc/production");
 *
 *      for ( PaymentHistory payment : payments ) {
 *          out.print(
 *                String.format("%10s  %12s  %-16s  %-12s  %s\n",
 *                               payment.date.toString(), 
 *                               money.format(payment.amount),
 *                               payment.yearsApplied,
 *                               payment.description,
 *                               payment.name
 *                               )
 *                         );
 *      }
 *  </pre>
 */
public class PaymentHistory {
    public PaymentHistory() {}


    /** Provides a convenience method for creating a PaymentHistory object, useful when
     *  chaining method calls to retrieve a payment history.
     *  @return a new PaymentHistory object, useful for chaining method calls
     */
    public static PaymentHistory initialContext() {
        return new PaymentHistory();
    }




    /** The date the payment was accepted by the tax office */
    public LocalDate        date                = null;
    /** The payor name */
    public String           name                = null;
    /** The tax years the payment was applied to */
    public String           yearsApplied        = null;
    /** The payment amount */
    public double           amount              = 0.0;

    /** The payment record status */
    public String           status              = null;
    /** A user friendly description of the status as defined by the status translation table */
    public String           description         = null;


    /** Client ID of the account to report on */
    public    String        clientId            = null;
    /** Account number (CAN) to report on */
    public    String        accountNumber       = null;
    /** Owner number of the account to report on */
    public    String        ownerNumber         = null;

    /** Comma delimited list of record statuses that will be ignored (not returned) */
    protected String        ignoreStatuses      = null;
    /** Comma delimited list of record statuses that will be ignored only if the escrow amount is $0.00 */
    protected String        escrowStatuses      = null;

    /** The minimum payment year to be reported */
    protected String        minPaymentYear      = null;
    /** The minimum tax year a payment is applied to that will be reported */
    protected String        minAppliedYear      = null;



    /** The status description translations */
    protected Hashtable<String,String> statusTranslationTable = new Hashtable<String,String>();

    /** Clears and re-initializes the status description translations used
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory initializeTranslationTable() {
        statusTranslationTable.clear();
        statusTranslationTable.put("PA","Payment");
        statusTranslationTable.put("AA","Payment");
        statusTranslationTable.put("TR","Transfer");
        statusTranslationTable.put("RV","Reversal");
        statusTranslationTable.put("RD","Refunded");
        statusTranslationTable.put("RF","Refund Pending");
        statusTranslationTable.put("SP","Payment");
        statusTranslationTable.put("VO","Reversal");
        statusTranslationTable.put("RX","Reversal");
        statusTranslationTable.put("OH","Refund Pending");
        statusTranslationTable.put("LG","Overpayment");
        return this;
    }


    /** Adds a status translation to the translation table that is used to set the description based on the record status value
     *  <p>Once set a status translation can not be removed. To reset the original values the table must be reinitialized.</p>
     *  @param status the record status to be translated
     *  @param description the translated description to report for the status
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setStatusTranslation(String status, String description) {
        statusTranslationTable.put(status, description);
        return this;
    }


    /** Defines the account to report on
     *  <p>Owner will default to "0" if the ownerNumber parameter is null.</p>
     *  @param clientId the client ID of the account to retrieve
     *  @param accountNumber the account number (CAN) of the account to retrieve
     *  @param ownerNumber the account owner number of the account to retrieve, will default to "0"
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setAccount(String clientId, String accountNumber, String ownerNumber) {
        this.clientId           = clientId;
        this.accountNumber      = accountNumber;
        this.ownerNumber        = ownerNumber;
        return this;
    }

    /** Defines the minimum payment year that is reported
     *  <p>Payments for years prior to the specified year are not reported.</p>
     *  <p>A null value will use the default year from the query.</p>
     *  @param minPaymentYear the minimum payment year that will be reported
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setMinPaymentYear(String minPaymentYear) {
        this.minPaymentYear     = minPaymentYear;
        return this;
    }

    /** Defines the minimum payment apply year that is reported
     *  <p>Years prior to the specified year are not reported, reported payments that are made to any prior years are 
     *  reduced by the amount of the payment applied to the excluded year.</p>
     *  <p>A null value will use the default year from the query.</p>
     *  @param minAppliedYear the minimum payment apply year that will be reported
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setMinAppliedYear(String minAppliedYear) {
        this.minAppliedYear     = minAppliedYear;
        return this;
    }

    /** Defines which status are excluded from being reported.
     *  <p>If null then the default exclusion statuses (DA,IT) are ignored.</p>
     *  <p>Set to blank ("") if all statuses are to be reported.</p>
     *  @param ignoreStatuses comma delimited string of record statuses that are ignored
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setIgnoreStatuses(String ignoreStatuses) {
        this.ignoreStatuses     = ignoreStatuses;
        return this;
    }

    /** Defines which status are excluded from being reported if the escrow amount is $0.00.
     *  <p>If null then the default exclusion statuses (RF,RD) are ignored.</p>
     *  <p>Set to blank ("") if all statuses are to be reported.</p>
     *  @param escrowStatuses comma delimited string of record statuses that are ignored if escrow amount is $0.00
     *  @return this object, useful for chaining method calls
     */
    public PaymentHistory setEscrowStatuses(String escrowStatuses) {
        this.escrowStatuses     = escrowStatuses;
        return this;
    }


    /** Retrieves the payment history for the configured account
     *  <p>The account information, and executing settings, must be set prior to calling this method.</p>
     *  @param dataSource the database data source to retrieve from
     *  @return an array of the payment history for the configured account
     */
    public PaymentHistory[] retrieve(String dataSource) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return retrieve(con);
        }
    }

    /** Retrieves the payment history for the configured account
     *  <p>The account information, and executing settings, must be set prior to calling this method.</p>
     *  @param con the database connection to retrieve from
     *  @return an array of the payment history for the configured account
     */
    public PaymentHistory[] retrieve(Connection con) throws Exception {
        ArrayList<PaymentHistory> payments = new ArrayList<PaymentHistory>();

        try ( PreparedStatement ps = con.prepareStatement(
                                                          " with "
                                                        + "     ignoredStatuses as ( "
                                                        + "                  select trim(regexp_substr(statusList,'[^,]+', 1, level)) ignored "
                                                        + "                    from (select nvl(?,'DA,IT') statusList from dual) "
                                                        + "                 connect by regexp_substr(statusList, '[^,]+', 1, level) is not null "
                                                        + "                  ), "
                                                        + "     escrowStatuses as ( "
                                                        + "                  select trim(regexp_substr(statusList,'[^,]+', 1, level)) ignored "
                                                        + "                    from (select nvl(?,'RF,RD') statusList from dual) "
                                                        + "                 connect by regexp_substr(statusList, '[^,]+', 1, level) is not null "
                                                        + "                  ) "
                                                        + "select paiddate, remit_seq, valnum, nvl(status,'PA') status, fido, "
                                                        + "       case when status = 'LG' and taxunit > 7999 then 1 else 0 end, "
                                                        + "       sum(penalty+levy+interest+attpaid+escrow) amount, "
                                                        + "       regexp_replace(listagg(year,',') within group(order by year),'([^,]+)(,\1)+','\1') "
                                                        + "                      as \"yearsApplied\" "
                                                        + "  from distribution_arch_view "
                                                        + "       left join ignoredStatuses on (ignoredStatuses.ignored=nvl(status,'PA')) "
                                                        + "       left join escrowStatuses on (escrowStatuses.ignored=nvl(status,'PA')) "
                                                        + " where client_id=? and can=? and ownerno=nvl(?,0) "
                                                        + "   and paiddate >= to_date('1/1/'||nvl(?,'2000'),'mm/dd/yyyy') "
                                                        + "   and year >= nvl(?,1990) "
                                                        + "   and ignoredStatuses.ignored is null "
                                                        + "   and (escrow != 0 or escrowStatuses.ignored is null) "
                                                        + " group by paiddate, remit_seq, valnum, nvl(status,'PA'), fido, "
                                                        + "          case when status = 'LG' and taxunit > 7999 then 1 else 0 end "
                                                        + " order by paiddate desc, remit_seq desc, valnum desc "
                                                        );
                CallableStatement payerName = con.prepareCall(
                                                        "call determination_payercsz2(p_clientid=>?, p_acctno=>?, p_ownerno=>nvl(?,0), p_remit_seq=>?, p_fido=>?,"
                                                                                + " p_ownername=>?, p_owneraddress=>?, p_add2=>?, p_add3=>?, p_add4=>?,"
                                                                                + " p_city=>?, p_state=>?, p_zipcode=>?)"
                                                        );
            ) {
            payerName.setString(1,clientId);
            payerName.setString(2,accountNumber);
            payerName.setString(3,ownerNumber);

            payerName.setString(4, null);
            payerName.setString(5, null);

            payerName.registerOutParameter(6,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(7,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(8,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(9,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(10,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(11,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(12,java.sql.Types.VARCHAR);
            payerName.registerOutParameter(13,java.sql.Types.VARCHAR);
            
            ps.setString(1,ignoreStatuses);
            ps.setString(2,escrowStatuses);

            ps.setString(3,clientId);
            ps.setString(4,accountNumber);
            ps.setString(5,ownerNumber);
            ps.setString(6,minPaymentYear);
            ps.setString(7,minAppliedYear);


            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    payerName.setString(4, rs.getString("remit_seq"));
                    payerName.setString(5, rs.getString("fido"));
                    payerName.execute();

                    PaymentHistory payment  = new PaymentHistory();
                    payment.date            = rs.getDate("paiddate").toLocalDate();
                    payment.name            = payerName.getString(6);
                    payment.yearsApplied    = rs.getString("yearsApplied").replaceAll("\\b(\\w+)\\b\\s*(?=.*\\b\\1\\b)", "")
                                                                          .replaceAll("(,+)",",").replaceAll("(^,|,$)","");
                    payment.amount          = Double.parseDouble(rs.getString("amount"));
                    payment.status          = rs.getString("status");

                    payment.description     = nvl(statusTranslationTable.get(payment.status),payment.status);

                    payments.add(payment);
                }
            }
        }

        return payments.toArray(new PaymentHistory[0]);
    }


    protected String nvl(String... values) {
        if ( values != null ) {
            for ( String value : values ) {
                if ( value != null ) return value;
            }
        }
        return "";
    }
}
