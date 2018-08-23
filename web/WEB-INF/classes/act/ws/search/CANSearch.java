package act.ws.search;

import java.util.*;
import java.sql.*;


/** Executes Owner Name searches */
public class CANSearch extends OwnerNameSearch {
    /** Empty Owner Name search object */
    public CANSearch() {
        this.searchType = SEARCH_BY_CAN | STARTS_WITH_SEARCH; 
    }


    /** Minimum search criteria. Tax year and account status exclusions will be automatically looked up during the search.
     *  @param client_id the client client_id to search against
     *  @param criteria the owner name search criteria
     */
    public CANSearch(String clientId, String criteria) {
        this();
        this.setCriteria(clientId, criteria);
    }

    /** Minimum search criteria. Tax year and account status exclusions will be automatically looked up during the search.
     *  @param client_id the client client_id to search against
     *  @param criteria the owner name search criteria
     */
    public CANSearch(String clientId, String year, String criteria) {
        this();
        this.setCriteria(clientId, criteria)
            .setYear(year);
    }

    public static CANSearch initialContext() {
        return new CANSearch();
    }

    /** Returns the search SQL, including exclusions and secondary search if specified
     *  @returns Search SQL query
     *  @throws Exception if an error occurs building the SQL string
     */
    public String getQueryString() throws Exception {
        return    "with "
                + "     parameter (clientId, year, excludeStatuses, allowConfidentialAccounts, criteria, altCriteria, maximumRows) as "
                + "             (select ?, ?, ?, nvl(?,'N'), upper(?), upper(?), nvl(?,0) from dual), "
                + "      preference (taxYear, exclusionStatuses, confidentialName, showConfidentialAddress, confidentialLegalName) as "
                + "             (select coalesce(year,act_utilities.get_client_prefs(clientId,'INTERNET_CURR_YEAR'),act_utilities.get_client_prefs(clientId,'CURR_YEAR')), "
                + "                     coalesce(excludeStatuses,act_utilities.get_client_prefs(clientId,'ACCT_STATUSES_REMOVE_FROM_WEB'),'--'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_NAME_TO_SHOW'),'UNKNOWN'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_ADDR_TOSHOW'),'N'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_LEGAL_TOSHOW'),'UNKNOWN') "
                + "                from parameter "
                + "             ), "
                + "      excludeAcctStatuses (status) as "
                + "             (select trim(regexp_substr(exclusionStatuses,'[^,]+', 1, level)) ignored "
                + "                from preference "
                + "              connect by regexp_substr(exclusionStatuses, '[^,]+', 1, level) is not null "
                + "             ) "
                + " select /*+ index(o OWNER_CAN_IX) index(t taxdtl_pk) */ "
                + "         o.can, o.ownerno, o.special_status, t.acctstatus, "
                + "         case when o.special_status = 'Y' then  confidentialName else o.nameline1 end as \"nameline1\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.nameline2 end as \"nameline2\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.nameline3 end as \"nameline3\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.nameline4 end as \"nameline4\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.city end as \"city\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.state end as \"state\", "
                + "         case when o.special_status = 'Y' and showConfidentialAddress != 'Y' then null else o.zipcode end as \"zipcode\", "
                + "         t.pnumber, t.pstrname, t.aprdistacc, "
                + "         case when o.special_status = 'Y' then confidentialLegalName else t.legal1 end as \"legal1\", "
                + "         case when o.special_status = 'Y' then null else t.legal2 end as \"legal2\", "
                + "         case when o.special_status = 'Y' then null else t.legal3 end as \"legal3\", "
                + "         case when o.special_status = 'Y' then null else t.legal4 end as \"legal4\", "
                + "         case when o.special_status = 'Y' then null else t.legal5 end as \"legal5\" "
                + "  from taxdtl t join owner o on (o.client_id = t.client_id and o.can = t.can and o.year = t.year), "
                + "       parameter, preference "
                + " where o.client_id = parameter.clientId and o.year=preference.taxYear "
                + "   and (o.web_suppress <> 'Y' or o.web_suppress is null) "
                + "   and (parameter.allowConfidentialAccounts = 'Y' or (o.special_status <> 'Y' or o.special_status is null)) "
                + "   and t.acctstatus not in (select status from excludeAcctStatuses) "
                + "   and (maximumRows=0 or rownum <= maximumRows) "
                + "   and (o.can like parameter.criteria) "
                + "   and (altCriteria is null "
                + "        or o.nameline1 || ' ' || o.nameline2 || ' ' || o.nameline3 || ' '|| o.nameline4 like '%'||parameter.altCriteria||'%' "
                + "        or t.pnumber || ' ' || t.pstrname like '%'||parameter.altCriteria||'%' "
                + "        ) "
                + " order by o.can ";
    }
}
