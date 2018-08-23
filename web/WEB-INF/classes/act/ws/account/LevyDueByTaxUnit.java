package act.ws.account;

import act.util.Connect;

import java.math.BigDecimal;

import java.sql.CallableStatement;
import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.text.NumberFormat;

import java.time.LocalDate;

import java.util.ArrayList;

public class LevyDueByTaxUnit {
    public LevyDueByTaxUnit() {}

    public  String      clientId        = null;
    public  String      accountNumber   = null;
    public  String      ownerNumber     = null;

    public  String      timeZoneOffset  = null;

    public  String      year            = null;
    public  String      taxUnit         = null;
    public  String      name            = null;

    public  BigDecimal  levyDue         = null;
    public  BigDecimal  additionalDue   = null;
    public  BigDecimal  totalDue        = null;


    protected BigDecimal zeroAmount = new BigDecimal("0.00");
    public BigDecimal add(BigDecimal value, BigDecimal valueToAdd) {
        if ( value == null ) value = zeroAmount;

        if ( valueToAdd != null ) {
            value = value.add(valueToAdd)
                         .setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return value;
    }
    public void add(LevyDueByTaxUnit record) {
        if ( record == null ) return;

        levyDue         = add(levyDue, record.levyDue);
        additionalDue   = add(additionalDue, record.additionalDue);
        totalDue        = add(totalDue, record.totalDue);
    }
    public void resetValues() {
        levyDue         = zeroAmount;
        additionalDue   = zeroAmount;
        totalDue        = zeroAmount;
    }


    public static LevyDueByTaxUnit initialContext() {
        return new LevyDueByTaxUnit();
    }

    public LevyDueByTaxUnit setAccount(String clientId, String accountNumber, String ownerNumber) {
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.ownerNumber = ownerNumber;

        return this;
    }
    public LevyDueByTaxUnit setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = Integer.toString(timeZoneOffset);

        return this;
    }

    public LevyDueByTaxUnit[] retrieve(String dataSource) throws Exception {
        try ( Connection con = Connect.open(dataSource); ) {
            return retrieve(con);
        }
    }
    public LevyDueByTaxUnit[] retrieve(Connection con) throws Exception {
        ArrayList<LevyDueByTaxUnit> records = new ArrayList<LevyDueByTaxUnit>();
        BigDecimal  zeroAmount  = new BigDecimal("0.00");

        // We must exclude the owner table as not all receivable records match up to an owner record
        // Yes, this doesn't bode well for data integrity...
        try ( PreparedStatement ps = con.prepareStatement(
                                              "select distinct receivable.year, receivable.taxunit, jurisdiction.name "
                                            + "   from  receivable "
                                            + "         join jurisdiction on (jurisdiction.client_id=receivable.client_id "
                                            + "                                 and jurisdiction.year=receivable.year "
                                            + "                                 and jurisdiction.taxunit=receivable.taxunit "
                                            + "                                 ) "
                                            + " where receivable.client_id=? and receivable.can=? and receivable.ownerno=nvl(?,0) "
                                            + " and taxunit_balance2(receivable.client_id,receivable.can,receivable.taxunit, "
                                            + "                      receivable.year,receivable.rectype,receivable.ownerno) = 'N' "
                                            + " order by receivable.year desc, jurisdiction.name asc "
                                            );
                CallableStatement levyDue = con.prepareCall(
                                              "call act.ccptaxyearlevy_bytaxunitdate( "
                                            + "         p_clientid=>?, p_can=>?, p_ownerno=>nvl(?,0), "
                                            + "         p_taxyear=>?, p_taxunit=>?, "
                                            + "         p_date=>add_months(last_day(sysdate-(nvl(?,0)/24)),nvl(?,0)), "
                                            + "         o_levy_due=>?, o_levy_def=>?, o_interest_due=>?, "
                                            + "         o_penalty_due=>?, o_attorney_due=>?, o_discount=>?, "
                                            + "         o_court_cost=>?, o_abst_fees=>?, o_other_fees=>? "
                                            + ")"
                                            );
            ) {

            ps.setString(1,clientId);
            ps.setString(2,accountNumber);
            ps.setString(3,ownerNumber);

            levyDue.setString(1,clientId);
            levyDue.setString(2,accountNumber);
            levyDue.setString(3,ownerNumber);

            levyDue.setString(6,timeZoneOffset);
            levyDue.setString(7,null);

            levyDue.registerOutParameter( 8,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter( 9,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(10,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(11,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(12,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(13,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(14,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(15,java.sql.Types.NUMERIC,2);
            levyDue.registerOutParameter(16,java.sql.Types.NUMERIC,2);


            try ( ResultSet rs = ps.executeQuery(); ) {

                while ( rs.next() ) {
                    LevyDueByTaxUnit record = new LevyDueByTaxUnit();

                    record.year = rs.getString("year");
                    record.taxUnit = rs.getString("taxunit");
                    record.name = rs.getString("name");

                    levyDue.setString(4,record.year);
                    levyDue.setString(5,record.taxUnit);

                    // We're only retrieving for one month but we'll leave the
                    // hook for multiple months here in case we need to expand this
                    for ( int month=0; month <= 0; month++ ) {
                        levyDue.setInt(7,month); // Months into the future we want data based on
                        levyDue.execute();

                        record.levyDue = zeroAmount
                                            .add(new BigDecimal(levyDue.getString(8)))          // Levy Due
                                            .add(new BigDecimal(levyDue.getString(13)))         // + Discount
                                            .add(new BigDecimal(levyDue.getString(14)))         // + Court Costs
                                            .add(new BigDecimal(levyDue.getString(15)))         // + Abstract Fees
                                            .add(new BigDecimal(levyDue.getString(16)))         // + Other Fees
                                            .setScale(12, BigDecimal.ROUND_HALF_UP);

                        record.additionalDue = zeroAmount
                                            .add(new BigDecimal(levyDue.getString(10)))         // Interest
                                            .add(new BigDecimal(levyDue.getString(11)))         // + Penalty
                                            .add(new BigDecimal(levyDue.getString(12)))         // + Attorney
                                            .subtract(new BigDecimal(levyDue.getString(13)))    // - Discount
                                            .setScale(2, BigDecimal.ROUND_HALF_UP);

                        record.totalDue = record.levyDue
                                            .add(record.additionalDue)
                                            .setScale(2, BigDecimal.ROUND_HALF_UP);
                    }

                    records.add(record);
                }
            }
        }

        return (LevyDueByTaxUnit[]) records.toArray(new LevyDueByTaxUnit[0]);
    }
}