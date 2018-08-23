package act.ws.account;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;

public class Payment {
    public Payment() {}


    public LocalDateTime   date         = null;
    public String          tid          = null;
    public String          name         = null;
    public double          amount       = 0.0;

    public String          type         = null;
    public String          method       = null;
    public String          taxUnit      = null;
    public String          year         = null;

    public String          ptid         = null;
    public String          description  = null;


    /** Retrieves all pending payments for the specified account
     *
     *  <p>Only payments within the last three months are returned. Payments older than three months are ignored.</p>
     *  <p>Any pending payments that are part of a portfolio lock are included in the returned payments.</p>
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number
     *
     *  @returns Payment array of all pending payments within last three months
     *  @throws Exception if an error occurs retrieving the pending payments
     */
    public static Payment[] getPendingPayments(Connection con, String clientId, String accountNumber, String ownerNumber) throws Exception {
        ArrayList<Payment> pending = new ArrayList<Payment>();

        try ( PreparedStatement ps = con.prepareStatement(
                                                  "select c.chngdate as \"date\", c.transid as \"tid\", "
                                                + "       c.anameline1 as \"name\", c.ppamount as \"amount\", "
                                                + "       c.trans_type as \"type\", null as \"taxUnit\", "
                                                + "       c.year, c.vendortid as \"ptid\", "
                                                + "       case when c.trans_type='EC' then 'eCheck' "
                                                + "            else 'Credit Card' "
                                                + "            end as \"method\" "
                                                + "  from credit_card_data c "
                                                + "       left outer join pfoliolockdtl p "
                                                + "            on (p.client_id=c.client_id and p.lock_id = c.lock_id) "
                                                + " where c.client_id=? and (c.can=? or p.can=?) and (c.ownerno=nvl(?,0) or p.ownerno=nvl(?,0)) "
                                                + "   and c.ppstatus in ('AP','RT') "
                                                + "   and (c.paidflag is null or c.paidflag != 'Y') "
                                                + "   and c.chngdate > add_months(sysdate,-3) "
                                                );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, accountNumber);
            ps.setString(4, ownerNumber);
            ps.setString(5, ownerNumber);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    Payment payment = new Payment();
                    payment.date    = rs.getTimestamp("date").toLocalDateTime();
                    payment.tid     = rs.getString("tid");
                    payment.name    = rs.getString("name");
                    payment.amount  = Double.parseDouble(rs.getString("amount"));
                    payment.type    = rs.getString("type");
                    payment.method  = rs.getString("method");
                    payment.taxUnit = rs.getString("taxUnit");
                    payment.year    = rs.getString("year");
                    payment.ptid    = rs.getString("ptid");
                    pending.add(payment);
                }
            }
        }

        return pending.toArray(new Payment[0]);
    }


    /** Retrieves the payment date, amount, and payer name for the most recent payment
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number
     *
     *  @returns Last payment summary (payer, amount, date) for the most recent payment 
     *  @throws Exception if an error occurs retrieving the payment information
     */
    public static Payment getLastPayment(Connection con, String clientId, String accountNumber, String ownerNumber) throws Exception {
        Payment payment = null;
        try ( PreparedStatement ps = con.prepareStatement(
                                                  "select remit_seq, valnum, "
                                                + "       sum(penalty+interest+levy+escrow+attpaid) as \"paymentAmount\", "
                                                + "       min(fido) as \"fido\", max(paiddate) as \"paymentDate\" "
                                                + "  from distribution_arch_view "
                                                + " where  client_id=? and can=? and ownerno=nvl(?,0) "
                                                + "   and (status in  ('AA','PA','SP','PP','RV') or status is null or (client_id=100000000 and status = 'TR')) "
                                                + "   and (taxunit < 8000 or (client_id = 100000000 and taxunit=9911)) "
                                                + " group by remit_seq, valnum "
                                                + " having sum(penalty+interest+levy+escrow+attpaid) > 0 "
                                                + " order by max(paiddate) desc, remit_seq desc, valnum asc "
                                                );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);

            try ( ResultSet rs = ps.executeQuery(); 
                  CallableStatement call = con.prepareCall("call determination_payercsz2( ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,   ?, ?, nvl(?,0) )");
                ) {
                if ( rs.next() ) {
                    payment = new Payment();
                    payment.date    = rs.getTimestamp("paymentDate").toLocalDateTime();
                    payment.amount  = Double.parseDouble(rs.getString("paymentAmount"));

                    String remitSeq = rs.getString("remit_seq");
                    String fido     = rs.getString("fido");

                    /*  The following are the field positions for determination_payercsz2
                     *            :client_id, :can, :remit seq, :fido, 
                     *            :out (ownerName), :out (ownerAddress), 
                     *            :out (add2), :out (add3), :out (add4), :out (city), :out (state), :out (zip), 
                     *            :ownerno
                     */
                    call.registerOutParameter(5,java.sql.Types.VARCHAR);
                    call.registerOutParameter(6,java.sql.Types.VARCHAR);
                    call.registerOutParameter(7,java.sql.Types.VARCHAR);
                    call.registerOutParameter(8,java.sql.Types.VARCHAR);
                    call.registerOutParameter(9,java.sql.Types.VARCHAR);
                    call.registerOutParameter(10,java.sql.Types.VARCHAR);
                    call.registerOutParameter(11,java.sql.Types.VARCHAR);
                    call.registerOutParameter(12,java.sql.Types.VARCHAR);

                    call.setString(1, clientId);
                    call.setString(2, accountNumber);
                    call.setString(3, remitSeq);
                    call.setString(4, fido);
                    call.setString(13, ownerNumber); // Ownerno

                    call.execute();
                    payment.name = call.getString(5);
                }
            }
        }

        return payment;
    }



    /** Retrieves the payment date, amount, and payer name for the most recent payment for the current tax year
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number
     *
     *  @returns Last payment summary (payer, amount, date) for the most recent payment for the current tax year
     *  @throws Exception if an error occurs retrieving the payment information
     */
    public static Payment getLastPaymentForCurrentYear(Connection con, String clientId, String accountNumber, String ownerNumber) throws Exception {
        Payment payment = null;
        try ( PreparedStatement ps = con.prepareStatement(
                                                  "select remit_seq, valnum, year, "
                                                + "       sum(penalty+interest+levy+escrow+attpaid) as \"paymentAmount\", "
                                                + "       min(fido) as \"fido\", max(paiddate) as \"paymentDate\" "
                                                + "  from distribution_arch_view "
                                                + " where  client_id=? and can=? and ownerno=nvl(?,0) "
                                                + "   and year=act_utilities.get_client_prefs(client_id,'INTERNET_CURR_YEAR') "
                                                + "   and (status in  ('AA','PA','SP','PP','RV') or status is null or (client_id=100000000 and status = 'TR')) "
                                                + "   and (taxunit < 8000 or (client_id = 100000000 and taxunit=9911)) "
                                                + " group by remit_seq, valnum, year "
                                                + " having sum(penalty+interest+levy+escrow+attpaid) > 0 "
                                                + " order by max(paiddate) desc, remit_seq desc, valnum asc "
                                                );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, ownerNumber);

            try ( ResultSet rs = ps.executeQuery(); 
                  CallableStatement call = con.prepareCall("call determination_payercsz2( ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,   ?, ?, nvl(?,0) )");
                ) {
                if ( rs.next() ) {
                    payment = new Payment();
                    payment.date    = rs.getTimestamp("paymentDate").toLocalDateTime();
                    payment.amount  = Double.parseDouble(rs.getString("paymentAmount"));

                    String remitSeq = rs.getString("remit_seq");
                    String fido     = rs.getString("fido");

                    /*  The following are the field positions for determination_payercsz2
                     *            :client_id, :can, :remit seq, :fido, 
                     *            :out (ownerName), :out (ownerAddress), 
                     *            :out (add2), :out (add3), :out (add4), :out (city), :out (state), :out (zip), 
                     *            :ownerno
                     */
                    call.registerOutParameter(5,java.sql.Types.VARCHAR);
                    call.registerOutParameter(6,java.sql.Types.VARCHAR);
                    call.registerOutParameter(7,java.sql.Types.VARCHAR);
                    call.registerOutParameter(8,java.sql.Types.VARCHAR);
                    call.registerOutParameter(9,java.sql.Types.VARCHAR);
                    call.registerOutParameter(10,java.sql.Types.VARCHAR);
                    call.registerOutParameter(11,java.sql.Types.VARCHAR);
                    call.registerOutParameter(12,java.sql.Types.VARCHAR);

                    call.setString(1, clientId);
                    call.setString(2, accountNumber);
                    call.setString(3, remitSeq);
                    call.setString(4, fido);
                    call.setString(13, ownerNumber); // Ownerno

                    call.execute();
                    payment.name = call.getString(5);
                }
            }
        }

        return payment;
    }






    /** Retrieves all pending scheduled payments for the specified account
     *
     *  @param con              Database connection
     *  @param clientId         Account client ID
     *  @param accountNumber    Account number
     *  @param ownerNumber      Owner number
     *
     *  @returns Payment array of all pending scheduled payments
     *  @throws Exception if an error occurs retrieving the scheduled payments
     */
    public static Payment[] getScheduledPayments(Connection con, String clientId, String accountNumber, String ownerNumber) throws Exception {
        ArrayList<Payment> scheduled = new ArrayList<Payment>();

        try ( PreparedStatement ps = con.prepareStatement(
                                      " select s.client_id, s.tokenid, s.scheduleid, s.status, s.frequency, s.day1, s.day2, s.startdate, s.enddate, s.lastpayment, s.nextPayment, "
                                    + "         t.token, t.vendor, a.can, a.year, a.amount "
                                    + " from( "
                                    + " select client_id, tokenid, scheduleid, status, frequency, day1, day2, startdate, enddate, lastpayment, "
                                    + "         case     when frequency='ONCE' then startdate "
                                    + "             when frequency='MONTHLY' "
                                    + "                 then case when lastpayment is not null "
                                    + "                         then add_months(lastpayment,1) "
                                    + "                         else case when extract(day from startdate) > day1 "
                                    + "                                 then last_day(startdate)+day1 "
                                    + "                                 else trunc(startdate,'MM')+day1-1 "
                                    + "                             end "
                                    + "                     end "
                                    + "             when frequency='BIMONTHLY' and day2 is not null " // Twice per month 
                                    + "                 then case when lastpayment is not null "
                                    + "                         then case when extract(day from lastpayment) = day1 "
                                    + "                                 then trunc(lastpayment,'MM')+day2-1 "
                                    + "                                 else last_day(lastpayment)+day1 "
                                    + "                             end "
                                    + "                         else case when extract(day from startdate) <= day1 "
                                    + "                                 then trunc(startdate,'MM')+day1-1 "
                                    + "                                 else case when extract(day from startdate) <= day2 "
                                    + "                                         then trunc(startdate,'MM')+day2-1 "
                                    + "                                         else last_day(startdate)+day1 "
                                    + "                                     end "
                                    + "                             end "
                                    + "                     end "
                                    + "             when frequency='BIMONTHLY' and day2 is null " // Once every other month 
                                    + "                 then case when lastpayment is not null "
                                    + "                         then add_months(lastpayment,2) "
                                    + "                         else case when extract(day from startdate) <= day1 "
                                    + "                                 then trunc(startdate,'MM')+day1-1 "
                                    + "                                 else last_day(startdate)+day1 "
                                    + "                              end "
                                    + "                     end "
                                    + "             when frequency='TWOWEEKS' "
                                    + "                 then case when lastpayment is not null "
                                    + "                         then case when to_char(lastpayment,'D') = day1 "
                                    + "                                 then lastpayment+14 "
                                    + "                                 else lastpayment+14+(day1-to_char(lastpayment,'D')) "
                                    + "                             end "
                                    + "                         else case when to_char(startdate,'D') <= day1 "
                                    + "                                 then startdate+(day1-to_char(startdate,'D')) "
                                    + "                                 else startdate+(7-to_char(startdate,'D')+day1) "
                                    + "                             end "
                                    + "                     end "
                                    + "             when frequency='TWOWEEKS' " // 'WEEKLY' 
                                    + "                 then case when lastpayment is not null "
                                    + "                         then case when to_char(lastpayment,'D') = day1 "
                                    + "                                 then lastpayment+7 "
                                    + "                                 else lastpayment+7+(day1-to_char(lastpayment,'D')) "
                                    + "                             end "
                                    + "                         else case when to_char(startdate,'D') <= day1 "
                                    + "                                 then startdate+(day1-to_char(startdate,'D')) "
                                    + "                                 else startdate+(7-to_char(startdate,'D')+day1) "
                                    + "                             end "
                                    + "                     end "
                                    + "             when frequency='QUARTERLY' "
                                    + "                 then case when lastpayment is not null "
                                    + "                         then add_months(lastpayment,3) "
                                    + "                         else case when mod(extract(month from startdate),3) = 0 "
                                    + "                                 then last_day(startdate) "
                                    + "                                 else last_day(add_months(startdate,3-mod(extract(month from startdate),3))) "
                                    + "                             end "
                                    + "                     end "
                                                    // This assumes a fixed payment cycle, Jan 31, Mar 31, May 31, Jul 31 "
                                    + "             when frequency='QUARTERPAY-FIXED-PAYMENT-CYCLE' " // Actual Quarter Pay
                                    + "                 then case when lastpayment is not null "
                                    + "                             then case when extract(month from add_months(lastpayment,1)) <= 3 "
                                    + "                                         then to_date('3/31/' || extract(year from lastpayment),'mm/dd/yyyy') "
                                    + "                                     when extract(month from add_months(lastpayment,1)) <= 5 "
                                    + "                                         then to_date('5/31/' || extract(year from lastpayment),'mm/dd/yyyy') "
                                    + "                                     when extract(month from add_months(lastpayment,1)) <= 7 "
                                    + "                                         then to_date('7/31/' || extract(year from lastpayment),'mm/dd/yyyy') "
                                    + "                                     else to_date('1/31/' || (extract(year from lastpayment)+1),'mm/dd/yyyy') "
                                    + "                                 end "
                                    + "                         else "
                                    + "                                 case when extract(month from startdate) <= 3 "
                                    + "                                         then to_date('3/31/' || extract(year from startdate),'mm/dd/yyyy') "
                                    + "                                     when extract(month from startdate) <= 5 "
                                    + "                                         then to_date('5/31/' || extract(year from startdate),'mm/dd/yyyy') "
                                    + "                                     when extract(month from startdate) <= 7 "
                                    + "                                         then to_date('7/31/' || extract(year from startdate),'mm/dd/yyyy') "
                                    + "                                     else to_date('1/31/' || (extract(year from startdate)+1),'mm/dd/yyyy') "
                                    + "                                 end "
                                    + "                     end "
                                    + "             when frequency='QUARTERPAY' " // Once every other month for four payments 
                                    + "                 then case when lastpayment is null "
                                    + "                         then startdate "
                                    + "                         else add_months(lastpayment,2) "
                                    + "                     end "
                                    + "             else null "
                                    + "         end nextpayment "
                                    + "     from esched_schedule es "
                                    + "     where status not in ('COMPLETE','CANCELED') "
                                    + "       and startdate <= trunc(sysdate)+2 and enddate > sysdate-10 "
                                    + "       and (frequency != 'ONCE' or lastpayment is null) "
                                    + "  ) s join esched_accounts a on (a.client_id=s.client_id and a.scheduleid=s.scheduleid) "
                                    + "      join esched_token t on (t.client_id=s.client_id and s.tokenid=t.tokenid) "
                                    + " where s.status = 'ACTIVE' and t.status = 'ACTIVE' and a.status = 'ACTIVE' "
                                    + "   and s.nextPayment <= s.endDate "
                                    + "   and s.client_id=? "
                                    + "   and a.client_id=? and a.can=? and a.ownerno=nvl(?,0) "
                                    + " order by s.nextPayment "
                                                );
            ) {

            ps.setString(1, clientId);
            ps.setString(2, clientId);
            ps.setString(3, accountNumber);
            ps.setString(4, ownerNumber);

            try ( ResultSet rs = ps.executeQuery(); ) {
                while ( rs.next() ) {
                    Payment payment = new Payment();
                    payment.tid     = rs.getString("scheduleid");
                    payment.date    = rs.getTimestamp("nextPayment").toLocalDateTime();
                    payment.amount  = Double.parseDouble(rs.getString("amount"));
                    payment.year    = rs.getString("year");
                    scheduled.add(payment);
                }
            }
        }

        return scheduled.toArray(new Payment[0]);
    }


}
