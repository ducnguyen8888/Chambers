package act.ws.search;

import java.util.*;
import java.sql.*;


/** Executes Owner Name searches */
public class TaxsiteSearch extends Search {
    /** Empty Owner Name search object */
    public TaxsiteSearch() {
        this.searchType = SEARCH_BY_TAXSITE_ADDRESS | STARTS_WITH_SEARCH;
    }


    /** Minimum search criteria. Tax year and account status exclusions will be automatically looked up during the search.
     *  @param clientId the client id to search against
     *  @param criteria the owner name search criteria
     */
    public TaxsiteSearch(String clientId, String criteria) {
        this();
        this.setCriteria(clientId, criteria);
    }

    /** Minimum search criteria. Tax year and account status exclusions will be automatically looked up during the search.
     *  @param clientId the client id to search against
     *  @param criteria the owner name search criteria
     */
    public TaxsiteSearch(String clientId, String year, String criteria) {
        this();
        this.setCriteria(clientId, criteria)
            .setYear(year);
    }

    public static TaxsiteSearch initialContext() {
        return new TaxsiteSearch();
    }

    /** Returns the search SQL, including exclusions and secondary search if specified
     *  @returns Search SQL query
     *  @throws Exception if an error occurs building the SQL string
     */
    public String getQueryString() throws Exception {
        return    "with "
                + "     parameter (clientId, year, excludeStatuses, allowConfidentialAccounts, criteria, altCriteria, maximumRows, street) as "
                + "             (select ?, ?, ?, nvl(?,'N'), upper(?), upper(?), nvl(?,0), upper(?) from dual), "
                + "      preference (taxYear, exclusionStatuses, confidentialName, showConfidentialAddress, confidentialLegalName, streetNumber, streetName) as "
                + "             (select coalesce(year,act_utilities.get_client_prefs(clientId,'INTERNET_CURR_YEAR'),act_utilities.get_client_prefs(clientId,'CURR_YEAR')), "
                + "                     coalesce(excludeStatuses,act_utilities.get_client_prefs(clientId,'ACCT_STATUSES_REMOVE_FROM_WEB'),'--'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_NAME_TO_SHOW'),'UNKNOWN'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_ADDR_TOSHOW'),'N'), "
                + "                     nvl(act_utilities.get_client_prefs(clientId,'CONF_LEGAL_TOSHOW'),'UNKNOWN'), "
                + "                     regexp_substr(street,'([0-9]+)'), regexp_replace(street,'^[0-9]+[ ]+','') "
                + "                from parameter "
                + "             ), "
                + "      excludeAcctStatuses (status) as "
                + "             (select trim(regexp_substr(exclusionStatuses,'[^,]+', 1, level)) ignored "
                + "                from preference "
                + "              connect by regexp_substr(exclusionStatuses, '[^,]+', 1, level) is not null "
                + "             ) "
                + " select /*+ index(o OWNER_CAN_IX) */ "
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
                + "   and (t.pstrname like '%'||parameter.criteria "
                + "        or (streetNumber is not null and t.pnumber=streetNumber and t.pstrname like streetName||'%') "
                + "        ) "
                + "   and (altCriteria is null "
                + "        or o.nameline1 || ' ' || o.nameline2 || ' ' || o.nameline3 || ' '|| o.nameline4 like '%'||parameter.altCriteria||'%' "
                + "        or t.pnumber || ' ' || t.pstrname like '%'||parameter.altCriteria||'%' "
                + "        ) "
                + " order by o.can ";
    }


    /** Executes the search query and returns the results.
     *  @param conn the database connection to execute the search on
     *  @returns SearchAccount[] an array of search results
     *  @throws Exception if an error occurs preparing, executing or retrieving results for the search
     */
    protected SearchAccount[] executeSearch(Connection con) throws Exception {
        int               recordsRetrieved = 0;
        String            lastAccountRetrieved = null;

        ArrayList         results    = new ArrayList();

        // Resets our timings
        time_duration = time_retrieve = time_execute = time_prepare = 0;

        // Removes any previous search results
        accounts = null;

        long start = (new java.util.Date()).getTime();
        try {
            trace.setLength(0);
            trace.append((new java.util.Date()).toString() + ": Account Search - Taxsite Address\n");
            if ( notDefined(clientId) ) throw INVALID_SEARCH_DATA_CLIENT_ID;

            trace.append("Verifying Search Type: " + describeSearchType() + "   Criteria: (" + criteria + ")\n");
            if ( isDefined(altCriteria) ) trace.append("Alt Search Criteria: (" + altCriteria + ")\n");

            // Verify our search criteria information
            if ( notDefined(criteria) ) throw INVALID_SEARCH_CRITERIA;

            trace.append("Verifying search criteria matches specified search limitations\n");
            if ( (searchType & NUMERIC_ONLY_SEARCH) != 0 && ! criteria.equals(criteria.replaceAll("[^0-9]","")) )  throw INVALID_NUMERIC_SEARCH_CRITERIA;


            // Adjust our criteria to conform to what is needed for the query
            trace.append("Adjusting criteria\n");
            criteria = adjustCriteria(searchType,criteria);
            trace.append("\tAdjusted Search Criteria: (" + criteria + ")\n");

            // Prepare query and set the query parameters
            trace.append("Preparing query\n");
            query = getQueryString();
            try ( PreparedStatement ps = con.prepareStatement(query); ) {
                trace.append("Setting Parameters:\n");
                trace.append("clientId (" + clientId + ")  year: (" + year + ")  max records: " + maxRecords + "\n");
                trace.append("Excluded Statuses (" + exclusions + ")  Confidential Accounts: (" + showRestrictedAccounts + ")\n");
                trace.append("Search Criteria: (" + criteria + ")\n");
                trace.append("Alternate Criteria: (" + altCriteria + ")\n");

                ps.setString(1, clientId);
                ps.setString(2, year);
                ps.setString(3, exclusions);
                ps.setString(4, (showRestrictedAccounts ? "Y" : "N"));
                ps.setString(5, criteria.replaceAll(" +"," "));
                ps.setString(6, altCriteria);
                ps.setInt   (7, maxRecords);
                ps.setString(8, criteria.replaceAll("[%_]"," ").replaceAll(" +"," ").trim());

                if ( setQueryValues ) {
                    try {
                        query = replaceQueryParameterField(query,clientId);
                        query = replaceQueryParameterField(query,year);
                        query = replaceQueryParameterField(query,exclusions);
                        query = replaceQueryParameterField(query,(showRestrictedAccounts ? "Y" : "N"));
                        query = replaceQueryParameterField(query,criteria.replaceAll(" +"," "));
                        query = replaceQueryParameterField(query,altCriteria);
                        query = replaceQueryParameterField(query,maxRecords);
                        query = replaceQueryParameterField(query,criteria.replaceAll("[%_]"," ").replaceAll("([ ]{2,})[^ ]+"," ").trim());
                    } catch (Exception e) {
                    }
                }
                trace.append("\nQuery:\n" + getPrintableQueryString() + "\n\n");


                time_prepare = (new java.util.Date()).getTime() - start;

                // Execute search query
                start = (new java.util.Date()).getTime();
                trace.append((new java.util.Date()).toString() + ": Execute query\n");
                try ( ResultSet rs = ps.executeQuery(); ) {
                    trace.append((new java.util.Date()).toString() + ": Retrieve results\n");
                    time_execute = (new java.util.Date()).getTime() - start;

                    // Retrieve search query results
                    start = (new java.util.Date()).getTime();
                    recordsRetrieved = 0;
                    while ( rs.next() ) {
                        recordsRetrieved++;
                        lastAccountRetrieved = rs.getString("can");

                        results.add(new SearchAccount(rs));
                    }
                }
            }
            accounts = (SearchAccount []) results.toArray(new SearchAccount[results.size()]);

            time_retrieve = (new java.util.Date()).getTime() - start;
            trace.append((new java.util.Date()).toString() + ": Search complete. Records found: " + results.size() + "\n");
        } catch (Exception e) {
            logException(e,"Search failure");
            trace.append((new java.util.Date()).toString() + ": Exception: " + e.toString() + "\n");
            if ( recordsRetrieved > 0 ) {
                trace.append("Records retrieved: " + recordsRetrieved + "   Last CAN: " + lastAccountRetrieved + "\n");
            }
            throw extend(e,"Search failed");
        } finally {
            time_duration = time_dbConnection + time_prepare + time_execute + time_retrieve;
            trace.append((new java.util.Date()).toString() + ": Search duration: " + time_duration + "\n");
        }

        return accounts;
    }
}
