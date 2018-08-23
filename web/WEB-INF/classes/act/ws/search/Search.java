package act.ws.search;

import act.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.sql.*;


/** Base account search class to specify a standard interface and provide a common location for shared fields and methods used 
 *  by all search classes.
 *
 *  Word or Phrase Search
 *  There are two primary ways of searching, word search or phrase search. Whether a search is a word or phrase search is controlled
 *  by the WORD_SEARCH bit.
 *  
 *  Word search matches individual words within the search string. Word order is maintained but there may be 0 or more characters
 *  between the search words.
 *  
 *  A word search for "CITY STATION" would match any value where the word "CITY" was followed by the word "STATION", such as:
 *  
 *  	"CITY STATION" and "CITY FIRE STATION" and "CITYS CHILD STATION" and "CITYSTATION"
 *  
 *  but would not match "CITY STAT ION" or "STATION CITY". 
 *  
 *  A word search replaces all spaces with "%" for matching, The search "CITY STATION" becomes "CITY%STATION".
 *  
 *  Word search may be set using the setWordSearch().
 *  
 *  
 *  Phrase search matches the full search string as a whole. Any spaces between words of the search string must match spaces in the search field.
 *  
 *  A phrase search for "CITY STATION" would match
 *  
 *  	"CITY STATION" and "CITY STATION REVIEW"
 *  
 *  but not "CITYSTATION" or "CITY FIRE STATION".
 *  
 *  A phrase search maintains all spaces for matching, The search "CITY STATION" remains "CITY STATION".
 *  
 *  Phrase search is the default search setting. Methods setPhraseSearch() and setExactSearch() may be used to reset phrase search if it changes.
 *  
 *
 *  Method Summary:
 *  	setWordSearch()			- Sets search to match individual criteria words within the search field
 *  
 *  	setPhraseSearch()		- Set search to match entire criteria as a single phrase within the search field, same as setExactSearch()
 *  
 *      setExactSearch()        - Set search to match entire criteria as a single phrase within the search field, same as setPhraseSearch()
 *
 *  
 *  Wildcards with Search String
 *  All necessary search wildcards are automatically added by the search execution. By default any search wildcards (% or _) are
 *  removed from the search criteria before the search is executed. To enable the user to specify search wildcards the 
 *  ALLOW_WILDCARD_SEARCH bit must be set. 
 *  
 *
 *  Method Summary:
 *  	allowWildcardSearch(boolean allowWildcards)	    - Flags whether wildcard searches are allowed or not
 *  
 *  	isWildcardSearch()      - Returns whether wild card search characters are allowed or not.
 *  
 *  
 *  Search Phrase position: Starts With, Ends With, and Within/Contains
 *  By default the search phrase is searched for anywhere within the search field. To change the search to search at the start
 *  of the search field or end of the search field is controlled by the STARTS_WITH_SEARCH and ENDS_WITH_SEARCH bits. The
 *  setStartsWithSearch() method changes the search to look for the search phrase at the beginning of the search field. The
 *  setEndsWithSearch() method changes the search to look for the phrase at the end of the search field. If both 
 *  STARTS_WITH_SEARCH and ENDS_WITH_SEARCH are set then the search phrase must match the search field exactly. The
 *  setWithinSearch() method disables both STARTS_WITH_SEARCH and ENDS_WITH_SEARCH and returns the search to it's normal
 *  "within" search functionality. setContainsSearch() is the same as setWithinSearch().
 *  
 *
 *  Method Summary:
 *  	setStartsWithSearch()       - Search phrase matches starting at the beginning of the search field
 *  
 *  	setEndsWithSearch()         - Search phrase matches ending at the end of the search field
 *  
 *  	setWithinSearch()           - Search phrase matches anywhere within the search field, same as setContainsSearch()
 *  
 *      setContainsSearch()         - Search phrase matches anywhere within the search field, same as setWordSearch()
 *  
 *  
 *  Amount Due amounts and flag
 *  The default search does not include any amount due information. Options exists that will return a flag indicating whether an
 *  account has an outstanding balance or the actual outstanding amount due.
 *  
 *  Method Summary:
 *  	includeBalanceStatus(boolean includeStatus)     - Flags whether the search should also identify whether an account has an outstanding
 *                                                        balance or not. The actual amount due isn't identified, only whether there is an
 *                                                        outstanding balance or not.
 *
 *  	includeAmountDue(boolean includeAmount)         - Flags whether the search should retrieve the account outstanding balance amount. 
 *                                                        The actual amount due is calculated and returned. This should not be used at the same
 *                                                        time as the includeBlanaceStatus option. This option will set the flag based on whether
 *                                                        an amount due is returned or not, including the balance status separately will only
 *                                                        cause an unnecessary delay in retrieving the search results.
 *
 *
 *  Use of the account materialized view
 *  An option exists that will utilize the account materialized view to determine the outstanding amount due for each account instead of
 *  calculating it. This should be faster but may not be accurate as the view is updated only once a day, if that. In development and test
 *  it appears that the view is not regularly updated so the actual results will not match. It is provided as it should be significantly faster
 *  than having to caluclate the actual amount for each account. Use with caution and only for results that don't require an absolutly correct
 *  amount due, such as the portfolio search and account list - locking account amounts should not use this though as it can lead to short payments
 *  and over payments.
 *
 *  	useMaterializedView(boolean useView)            - Flags whether the amount due for each account is retrieved from the materialized view
 *                                                        or not. Setting to false will cause the includeAmountDue(), if set, to calculate the
 *                                                        current amount due.
 **/
public abstract class Search extends Thread {


    // Remove after conversion
    long client_id = 0;
    int  altSearchType = 0;
    





    /** Returns the search query string for the class search type
     *  @return the SQL search query string
     */
    abstract public String getQueryString() throws Exception;

    /** Executes the search results and returns the results.
     *  @param con the database connection to execute the search on
     *  @return array of search results
     */
    abstract protected SearchAccount[] executeSearch(Connection con) throws Exception;




    /** The exception that occurring during the search execution, if any */
    protected Throwable searchException = null;

    /** Returns whether a search exception occurred
     *  @return TRUE if an exception occurred
     */
    public boolean hadSearchError() { return searchException != null; }

    /** Returns any search exception that occurred
     *  @return Throwable Any exception that occurred during the search
     */
    public Throwable getSearchError() { return searchException; }


    /** The datasource to use for searches, used when running this as a thread */
    protected String datasource = null;


    /** Starts the search execution as a separate thread
     *  @param datasource the database datasource name to execute the search on
     */
    public Search start(String datasource) {
        this.datasource = datasource;
        start();
        return this;
    }

    public void run() {
        try {
            searchException = null;
            search(datasource);
        } catch (Exception e) {
            searchException = e;
        }
    }






    /** Flag to denote whether the amount due should be pulled from the materialized view
     *  table or not. If not then the view will be calculated.
     */
    protected boolean useMaterializedView = false;

    /** Returns whether the amount due calculation will use the materialized view or not
     *  @return true if materialized view will be used to retrieve the account amount due
     */
    public boolean useMaterializedView() { return useMaterializedView; }

    /** Sets whether the amount due should retrieve the value from the materialized view
     *  instead of calculating it.
     *  @param useView TRUE if the amount due should be retrieved from the materialized view, FALSE will calculate the amount due
     */
    public Search  useMaterializedView(boolean useView) { this.useMaterializedView = useView; return this; }





    /** Byte mask to isolate search type from the search flags */
    public    static final int SEARCH_TYPE_MASK                        =   31;

    /** Search modifier flag to denote search criteria is restricted to numeric values only */
    public    static final int NUMERIC_ONLY_SEARCH                     =    1;  // Qualifier bit 
    /** Search modifier flag to denote search criteria is a fuzzy search. All spaces within the criteria are treated as wildcards, matching 0 or more of any characters in the search */
    public    static final int WORD_SEARCH                             =    2;  // Qualifier bit 
    /** Search modifier flag to denote that search criteria is to be the start of the searched field */
    public    static final int STARTS_WITH_SEARCH                      =    4;  // Qualifier bit 
    /** Search modifier flag to denote that search criteria is to be at the end of the searched field */
    public    static final int ENDS_WITH_SEARCH                        =    8;  // Qualifier bit 

    /** Search modifier flag to denote both STARTS_WITH_SEARCH and ENDS_WITH_SEARCH */
    public    static final int EXACT_SEARCH                            =    STARTS_WITH_SEARCH | ENDS_WITH_SEARCH;

    /** Search modifier flag to denote that the search criteria may contain wild card characters */
    public    static final int ALLOW_WILDCARD_SEARCH                   =   16;


    /** Search identifier signifying that no search is specified */
    public    static final int NO_SEARCH_REQUEST                       =    0;  // Denotes that no search criteria was specified
    /** Search identifier for owner name searches. Owner name is the owner.nameline1 column. */
    public    static final int SEARCH_BY_OWNER_NAME                    =   32;

    /** Search identifier for owner address searches. Owner address is the owner.nameline2, owner.nameline3, owner.nameline4 columns. */
    public    static final int SEARCH_BY_OWNER_ADDRESS                 =   64;

    /** Search identifier for property address searches. Property address is the taxdtl.pnumber and taxdtl.pstrname columns. */
    public    static final int SEARCH_BY_TAXSITE_ADDRESS               =  128;

    /** Search identifier for fiduciary searches for the current year. Fiduciary searches match accounts against the fidorequest table, FIDO is the fidorequest.fido column. */
    public    static final int SEARCH_BY_FIDO_CURRENT_YEAR             =  256   | NUMERIC_ONLY_SEARCH;
    /** Search identifier for fiduciary searches for future tax years. Fiduciary searches match accounts against the fidorequest table, FIDO is the fidorequest.fido column. */
    public    static final int SEARCH_BY_FIDO_FUTURE_YEAR              =  512   | NUMERIC_ONLY_SEARCH;

    /** Search identifier for CAN searches. CAN searches match accounts against the owner.can column. */
    public    static final int SEARCH_BY_CAN                           = 1024;
    /** Search identifier for CAD searches. CAD searches match accounts against the taxdtl.aprdistacc column. */
    public    static final int SEARCH_BY_CAD                           = 2048;
    /** Search identifier for ATTORNEY searches. ATTORNEY searches match accounts against the jurisdiction.attorney_id column, linked through the receivable table. */
    public    static final int SEARCH_BY_ATTORNEY                      = 4096   | NUMERIC_ONLY_SEARCH;
    /** Search identifier for LIEN searches. LIEN searches match accounts against the lien_info.lien_number or lien_info.lien_id columns. */
    public    static final int SEARCH_BY_LIEN                          = 8192;

    /** Alternate search identifier for owner name/address searches. Used only as alternate search to filter primary search results. */
    public    static final int ALTSEARCH_BY_OWNER                      = 16384;


    /** Returns a textual description of the set primary search type.
     * 	@return text description of the primary search type
     */
    public String describeSearchType() { return describeSearchType(searchType); }
    /** Returns a textual description of the specified search type.
     *  @param searchType the search type constant to describe
     *  @return text description of the search type
     */
    public String describeSearchType(int searchType) {
        switch (searchType & ~SEARCH_TYPE_MASK) {
            case NO_SEARCH_REQUEST                   : return "No search request";
            case SEARCH_BY_OWNER_NAME                : return "Owner Name";
            case SEARCH_BY_OWNER_ADDRESS             : return "Owner Address";
            case SEARCH_BY_TAXSITE_ADDRESS           : return "Taxsite Address";
            case SEARCH_BY_FIDO_CURRENT_YEAR         : return "FIDO Current Year";
            case SEARCH_BY_FIDO_FUTURE_YEAR          : return "FIDO Future Year";
            case SEARCH_BY_CAN                       : return "CAN";
            case SEARCH_BY_CAD                       : return "CAD";
            case SEARCH_BY_ATTORNEY                  : return "Attorney";
            case SEARCH_BY_LIEN                      : return "Lien";
            case ALTSEARCH_BY_OWNER                  : return "Owner Name/Address";

            default                                  : return "Unknown search type: " + searchType;
        }
    }

    /** Changes the search type to be an exact phrase search. 
     *  Any blanks in the criteria will be matched directly to spaces in the search field. This is the same as setPhraseSearch().
     */
    public Search setExactSearch() { searchType &= ~WORD_SEARCH; return this; }
    /** Changes the search type to be an exact phrase search. 
     *  Any blanks in the criteria will be matched directly to spaces in the search field. This is the same as setExactSearch().
     */
    public Search setPhraseSearch() { searchType &= ~WORD_SEARCH; return this; }


    /** Changes the search type to be a word order search. 
     *  Any blanks in the criteria will be changed to match 0 or more spaces in the search field, using the search wildcard '%'.
     *  Criteria word order is still preserved. This is the same as setContainsSearch().
     */
    public Search setWordSearch() { searchType |= WORD_SEARCH; return this; }


    /** Sets the search to match the criteria starting at the beginning of the search field */
    public Search setStartsWithSearch() { searchType |= STARTS_WITH_SEARCH; return this; }

    /** Sets the search to match the criteria ending at the end of the search field */
    public Search setEndsWithSearch()  { searchType |= ENDS_WITH_SEARCH; return this; }

    /** Sets the search to match the criteria anywhere within the search field, criteria word order is still preserved */
    public Search setWithinSearch() { searchType &= ~(STARTS_WITH_SEARCH | ENDS_WITH_SEARCH); return this; }

    /** Sets the search to match the criteria anywhere within the search field, same as setWithinSearch().
     */
    public Search setContainsSearch() { searchType &= ~(STARTS_WITH_SEARCH | ENDS_WITH_SEARCH); return this; }




    /** Returns whether wildcards are allowed or not */
    public boolean isWildcardSearch() { return (searchType & ALLOW_WILDCARD_SEARCH) == ALLOW_WILDCARD_SEARCH; }

    /** Sets whether wild card searches are allowed or not */
    public Search allowWildcardSearch(boolean allowWildcards) {
        if ( allowWildcards ) {
            searchType |= ALLOW_WILDCARD_SEARCH;
        } else {
            searchType &= ~ALLOW_WILDCARD_SEARCH; 
        }

        return this;
    }



    /** Returns a user friendly textual description of the set primary search type.
     * 	@return text description of the primary search type
     */
    public String userSearchType() { return userSearchType(searchType); }
    /** Returns a user friendly textual description of the specified search type.
     *  @param searchType the search type constant to describe
     *  @return text description of the search type
     */
    public String userSearchType(int searchType) {
        switch (searchType) {
            case NO_SEARCH_REQUEST                   : return "No search request";
            case SEARCH_BY_OWNER_NAME                : return "Owner Name";
            case SEARCH_BY_OWNER_ADDRESS             : return "Owner Address";
            case SEARCH_BY_TAXSITE_ADDRESS           : return "Property Address";
            case SEARCH_BY_FIDO_CURRENT_YEAR         : return "FIDO Current Year";
            case SEARCH_BY_FIDO_FUTURE_YEAR          : return "FIDO Future Year";
            case SEARCH_BY_CAN                       : return "Account Number";
            case SEARCH_BY_CAD                       : return "CAD Reference";
            case SEARCH_BY_ATTORNEY                  : return "Attorney";
            case SEARCH_BY_LIEN                      : return "Lien";

            case ALTSEARCH_BY_OWNER                  : return "Owner Name/Address";

            default                                  : return "Unknown search type: " + searchType;
        }
    }

    /** Exception thrown when the search type is invalid */
    public final Exception INVALID_SEARCH_TYPE             = new UnsupportedOperationException("Invalid search type specified");
    /** Exception thrown when the search criteria is invalid */
    public final Exception INVALID_SEARCH_CRITERIA         = new UnsupportedOperationException("Invalid search criteria specified");
    /** Exception thrown when the search type is numeric but the search criteria is not numeric */
    public final Exception INVALID_NUMERIC_SEARCH_CRITERIA = new UnsupportedOperationException("Invalid numeric search criteria specified");
    /** Exception thrown when the alternate search type is invalid */
    public final Exception INVALID_ALTSEARCH_TYPE          = new UnsupportedOperationException("Invalid secondary search type specified");
    /** Exception thrown when the alternate search criteria is invalid */
    public final Exception INVALID_ALTSEARCH_CRITERIA      = new UnsupportedOperationException("Invalid secondary search criteria specified");
    /** Exception thrown when the alternate search type is numeric but the alternate search criteria is not numeric */
    public final Exception INVALID_NUMERIC_ALTSEARCH_CRITERIA = new UnsupportedOperationException("Invalid numeric secondary search criteria specified");
    /** Exception thrown when the client_id invalid */
    public final Exception INVALID_SEARCH_DATA_CLIENT_ID   = new UnsupportedOperationException("Invalid client ID specified");
    /** Exception thrown when the tax year is invalid */
    public final Exception INVALID_SEARCH_DATA_YEAR        = new UnsupportedOperationException("Invalid year specified");


    /** The time it took to make a database connection - for search only */
    public    long     time_dbConnection = 0;
    /** The time it took to execute and retrieve search results. Should be the
     *  total time of the time_prepare+time_execute+time_retrieve.
     */
    public    long     time_search       = 0;
    /** The time it took to prepare the search query */
    public    long     time_prepare      = 0;
    /** The time it took to execute the search query */
    public    long     time_execute      = 0;
    /** The time it took to retrieve the search query results */
    public    long     time_retrieve     = 0;
    /** Total query time duration  */
    public    long     time_duration     = 0;

    /** Client ID to search */
    public    String   clientId          = null;

    /** Current tax year */
    public    String   year              = null;
    /** Sets the current tax year
     *  @param year current tax year
     */
    public    Search   setYear(String year) {	this.year = year; return this; }

    /** Account statuses to exclude from the search results */
    public    String   exclusions     = null;
    /** Sets the exclusion statuses to exclude from the search results
     *  @param exclusions the list of statuses to exclude, should be in the format of 'XA' or 'XA','XD'
     */
    public    Search   setExclusions(String exclusions) { this.exclusions = exclusions; return this; }

    /** Primary search type to perform */
    public    int      searchType     = NO_SEARCH_REQUEST;
    /** Primary search criteria */
    public    String   criteria       = null;

    /** Sets primary search criteria
     *  @param clientId the client id to search against
     *  @param exclusions the account status exclusion list
     *  @param criteria the owner name search criteria
     */
    public Search setCriteria(String clientId, String criteria) {
        this.clientId      = clientId;
        this.criteria      = criteria;
        return this;
    }


    /** Alternate search criteria */
    public    String   altCriteria    = null;

    /** Sets alternate search criteria
     *  @param altSearchType the search type of the secondary criteria
     *  @param altCriteria the secondary search criteria
     */
    public Search setAltCriteria(String altCriteria) {
        this.altCriteria   = altCriteria;
        return this;
    }


    /** Maximum number of search records to return */
    public    int      maxRecords     = 100;
    /** Sets the maximum number of search records to return
     *  @param maxRecords the number of records
     */
    public    Search   setMaxRecords(int maxRecords) { this.maxRecords = maxRecords; return this; }


    /** Flag to denote whether restricted accounts are to be included in the search results */
    public    boolean  showRestrictedAccounts = false;
    /**
     * Sets whether restricted accounts are included in the search results 
     * @param includeOrNot true to include restricted accounts, false to exclude
     */
    public    Search   showRestrictedAccounts(boolean includeOrNot) { 
        showRestrictedAccounts = includeOrNot; 
        return this;
    }
    /**
     * Returns whether restricted accounts are included in the search results 
     * @return true if restricted accounts are included, false if excluded
     */
    public    boolean  showRestrictedAccounts() {
        return showRestrictedAccounts; 
    }


    /** Flag to denote whether outstanding amount due is to be included in the search results */
    public    boolean  includeAmountDue = false;
    /** Flags the search to include outstanding amount due in the search results -- may cause the query to take a long time 
     *  @param includeStatus boolean indicating whether to include levy balance or not in the query
     */
    public    Search   includeAmountDue(boolean includeAmount) { includeAmountDue = includeAmount; return this; }
    /** Returns whether the account amount due is included in the search 
     *  @return true or false whether the account amount due is included in the query or not 
     */
    public    boolean  includeAmountDue() { return includeAmountDue == true; }



    /** Flag to denote whether outstanding balance status is to be included in the search results */
    public    boolean  includeBalanceStatus = false;
    /** Flags the search to include outstanding balance status in the search results -- may cause the query to take a long time 
     *  @param includeStatus boolean indicating whether to include levy balance or not in the query
     */
    public    Search   includeBalanceStatus(boolean includeStatus) { includeBalanceStatus = includeStatus; return this; }
    /** Returns whether the account balance status is included in the search 
     *  @return true or false whether the account balance status is included in the query or not 
     */
    public    boolean  includeBalanceStatus() { return includeBalanceStatus == true; }



    /** Debug buffer used to review and track what occurs during the search execution */
    public    StringBuffer trace      = new StringBuffer();

    /** Holds the account records matching the search query */
    protected SearchAccount []  accounts = null;

    /** Returns the account records matching the search query */
    public    SearchAccount []  getAccounts() { return accounts; }



    /** The search query used to retrieve the search results */
    public    String   query            = null;

    /** Denotes whether the search query should be updated with the parameter values */
    public    boolean  setQueryValues   = false;

    /** Flags the search to replace query place holders with the actual parameter values in the recorded query string
     *  @param setQueryValues boolean indicating whether to replace parameters or not in the query
     */
    public    Search   setQueryValues(boolean setQueryValues) {
        this.setQueryValues = setQueryValues; 
        return this;
    }

    /**
     * Returns whether or not the query parameters are replaced in the debug version of
     * the query string with the actual values
     *  @return true if the query parameters are replaced with values, false otherwise
     */
    public    boolean  setQueryValues() { return setQueryValues; }

    /** Flags the search to save only the raw SQL used for the prepare statement in the recorded query string */
    public    Search   ignoreQueryValues() { setQueryValues = false; return this; }



    /** Replaces the next PreparedStatement parameter field with the specified value and returns the new query String.
     *  @param query the query string to modify
     *  @param value the parameter value to replace the parameter field holder "?" in the query string
     *  @return the updated query string with the replaced value
     */
    public    String   replaceQueryParameterField(String query, String value) {
        if ( notDefined(query) ) return query;

        int idx = query.indexOf("?");
        if ( idx < 0 ) return query;

        if ( value != null ) {
            value = value.replaceAll("'","''");
        }

        return query.substring(0,idx) + (value == null ? null : "'" + value + "'") + query.substring(idx+1);
    }

    /** Replaces the next PreparedStatement parameter field with the specified value and returns the new query String.
     *  @param query the query string to modify
     *  @param value the parameter value to replace the parameter field holder "?" in the query string
     *  @return the updated query string with the replaced value
     */
    public    String   replaceQueryParameterField(String query, double value) {
        if ( notDefined(query) ) return query;

        int idx = query.indexOf("?");
        if ( idx < 0 ) return query;

        return query.substring(0,idx) + value + query.substring(idx+1);
    }

    /** Replaces the next PreparedStatement parameter field with the specified value and returns the new query String.
     *  @param query the query string to modify
     *  @param value the parameter value to replace the parameter field holder "?" in the query string
     *  @return the updated query string with the replaced value
     */
    public    String   replaceQueryParameterField(String query, long value) {
        if ( notDefined(query) ) return query;

        int idx = query.indexOf("?");
        if ( idx < 0 ) return query;

        return query.substring(0,idx) + value + query.substring(idx+1);
    }


    /** Returns first non-null value, otherwise "" is returned
     *  @param values values to check 
     *  @return first non-null value or "" if all values are null
     */
    public String nvl(String... values) {
        if ( values != null ) {
            for ( String value : values ) {
                if ( value != null ) return value;
            }
        }
        return "";
    }

    /** Returns whether the specified value is defined.
     *  A value is considered defined if it is not null and is not an empty string
     *  @param val the value to check whether it is defined or not
     *  @return true if the value is defined, otherwise false
     */
    public boolean isDefined(String val) { return val != null && val.length() > 0; }
    /** Returns whether the specified value is undefined.
     *  A value is considered undefined if it is null or it is an empty string
     *  @param val the value to check whether it is defined or not
     *  @return true if the value is undefined, otherwise false
     */
    public boolean notDefined(String val) { return val == null || val.length() == 0; }

    public Exception extend(Exception e, String message) throws Exception {
        return e.getClass().getConstructor(new Class[]{(new String()).getClass()}).newInstance((Object[])(new String[]{message + ". " + e.getMessage()}));
    }
    public SQLException extend(SQLException e, String message) {
        return new SQLException(String.format("%s. %s", message, e.getMessage()));
    }


    /** Modifies the search criteria to adjust for search type flags 
     *  @param altSearchType the search type of the secondary criteria
     *  @param altCriteria the secondary search criteria
     *  @return criteria adjusted for search flags
     */
    public String adjustCriteria(int searchType, String criteria) {
        if ( notDefined(criteria) ) return criteria;

        if ( (searchType & ALLOW_WILDCARD_SEARCH) == 0 )
            criteria = criteria.replaceAll("([%_])","\\\\$1");

        // Adjust for any possible punctuation that may not actually exist in the data
        if ( (searchType & NUMERIC_ONLY_SEARCH) != 0 ) {
            criteria = criteria.replaceAll("[^0-9%_]","");
        //} else {
        //	criteria = criteria.replaceAll("([\'])","\\\\$1").trim();
        }

        if ( (searchType & STARTS_WITH_SEARCH) == 0 ) criteria = "%" + criteria;
        if ( (searchType & ENDS_WITH_SEARCH) == 0 ) criteria = criteria + "%";
        if ( (searchType & WORD_SEARCH) != 0 ) criteria = criteria.replaceAll(" ","%");

        // Final clean up of the criteria. All searches should be upper case
        criteria = criteria.replaceAll("%[%]{1,}","%").toUpperCase();

        return criteria;
    }


    /** Executes the search results and returns the results.
     *  @param datasource the database datasource to execute the search on
     *  @return array of search results
     */
    public SearchAccount[] search(String datasource) throws Exception {
        // Resets our timings
        time_dbConnection = 0;
        accounts = null;
        searchException = null;

        long start = (new java.util.Date()).getTime();
        try ( Connection con = Connect.open(datasource) ) {
            time_dbConnection = (new java.util.Date()).getTime() - start;
            time_duration += time_dbConnection;

            accounts = search(con);
        } catch (Exception e) {
            searchException = e;
            throw extend(e,"Retrieve accounts");
        }

        return accounts;
    }


    /** Executes the search results and returns the results.
     *  @param con the database connection to execute the search on
     *  @return array of search results
     */
    public SearchAccount[] search(Connection con) throws Exception {
        // Resets our timings
        time_duration = time_search = time_retrieve = time_execute = time_prepare = 0;
        accounts = null;
        searchException = null;

        long start = (new java.util.Date()).getTime();
        try { 
            accounts = executeSearch(con);
        } catch (Exception e) {
            searchException = e;
            throw extend(e,"Executing search");
        } finally {
            time_duration = time_search = (new java.util.Date()).getTime() - start;
        }

        return accounts;
    }



    /** Returns the search SQL with line breaks
     *  @returns Search SQL query
     *  @throws Exception if an error occurs building the SQL string
     */
    public String getPrintableQueryString() throws Exception {
        return nvl(query,getQueryString()).replaceAll("([ ]{2,})","\n$1");
    }


    /** Logs message to the exception log
     *  @param e Exception to log
     *  @param message additional message to log
     */
    public void logException(String message) {
        act.log.ErrorLog.severe(message);
        return;
    }
    /** Logs exceptions to the exception log
     *  @param e Exception to log
     */
    public void logException(Exception e) {
        act.log.ErrorLog.severe(e,this.getClass().getName());
        return;
    }
    /** Logs exceptions to the exception log
     *  @param e Exception to log
     *  @param message additional message to log
     */
    public void logException(Exception e, String message) {
        act.log.ErrorLog.severe(e,message);
        return;
    }



    public static void GetAmountOwed(Connection con, String clientId, String year, SearchAccount [] accounts) throws Exception {
        try ( PreparedStatement ps = con.prepareStatement(
                                  "with parameter (clientId, account, ownerno, year) as "
                                + "         (select ?, ?, ?, ? from dual), "
                                + "     preference (taxYear) as "
                                + "         (select coalesce(clientId,act_utilities.get_client_prefs(clientId,'INTERNET_CURR_YEAR'),act_utilities.get_client_prefs(clientId,'CURR_YEAR')) "
                                + "            from parameter) "
                                + "select website.levydue(clientId, account, ownerno)-website.levydue(clientId, account, ownerno,taxYear+1) as \"amountDue\" "
                                + "  from parameter, preference"
                                ); ) {
            ps.setString(1,clientId);
            ps.setString(4,year);

            for ( int i=0; i < accounts.length; i++ ) {
                ps.setString(2,accounts[i].can);
                ps.setString(3,accounts[i].ownerno);

                try ( ResultSet rs = ps.executeQuery(); ) {
                    rs.next();
                    accounts[i].amountDue = Double.parseDouble(rs.getString("amountDue"));
                    accounts[i].hasBalanceDue = accounts[i].amountDue > 0;
                }
            }
        }

        return;
    }


    /** The balance_mv materialized view only reports the levy amount due, no penalty/interest/etc is included in the total due. */
    public static void GetLevyOwed(Connection con, String clientId, String year, SearchAccount [] accounts) throws Exception {
        try ( PreparedStatement ps = con.prepareStatement(
                                  "with parameter (clientId, account, ownerno, year) as "
                                + "         (select ?, ?, ?, ? from dual), "
                                + "     preference (taxYear) as "
                                + "         (select coalesce(clientId,act_utilities.get_client_prefs(clientId,'INTERNET_CURR_YEAR'),act_utilities.get_client_prefs(clientId,'CURR_YEAR')) "
                                + "            from parameter) "
                                + "select nvl(sum(balance),0) as \"levyDue\" "
                                + "  from balance_mv, parameter, preference "
                                + " where client_id=clientId and can=account and balance_mv.year <= taxYear and balance > 0 "
                                ); ) {
            ps.setString(1,clientId);
            ps.setString(4,year);

            for ( int i=0; i < accounts.length; i++ ) {
                ps.setString(2,accounts[i].can);
                ps.setString(3,accounts[i].ownerno);

                try ( ResultSet rs = ps.executeQuery(); ) {
                    rs.next();
                    accounts[i].amountDue = Double.parseDouble(rs.getString("levyDue"));
                    accounts[i].hasBalanceDue = accounts[i].amountDue > 0;
                }
            }
        }

        return;
    }


    public static void CheckBalanceDue(Connection con, String clientId, String year, SearchAccount [] accounts) throws Exception {
        try ( PreparedStatement ps = con.prepareStatement(
                                  "with parameter (clientId, account, year) as "
                                + "         (select ?, ?, ? from dual), "
                                + "     preference (taxYear) as "
                                + "         (select coalesce(clientId,act_utilities.get_client_prefs(clientId,'INTERNET_CURR_YEAR'),act_utilities.get_client_prefs(clientId,'CURR_YEAR')) "
                                + "            from parameter) "
                                + "select count(*) from balance_mv, parameter, preference "
                                + " where client_id = parameter.clientId and can = parameter.account "
                                + "   and year <= preference.taxyear "
                                + "   and balance > 0"
                                ); ) {
            ps.setString(1,clientId);
            ps.setString(3,year);

            for ( int i=0; i < accounts.length; i++ ) {
                ps.setString(2,accounts[i].can);

                try ( ResultSet rs = ps.executeQuery(); ) {
                    rs.next();
                    accounts[i].hasBalanceDue = rs.getInt(1) > 0;
                }
            }
        }

        return;
    }



    /** Creates a JSON formatted string of the public fields of the specified object.
     *  Returns a JSON String of the public fields for this class. Field names
     *  are the used as the Element names with the current values used as the
     *  values.
     */
    public String toJson() {
        return toJson(accounts);
    }

    /** Creates a JSON formatted string of the public fields of the specified object.
     *  Returns a JSON String of the public fields for this class. Field names
     *  are the used as the Element names with the current values used as the
     *  values.
     */
    public String toJson(Object obj) {
        boolean isClassArray = false;
        StringBuffer arrBuffer = new StringBuffer();
        StringBuffer buffer = new StringBuffer();
        Object arrayElement = null;
        int    arrayLength  = 0;

        if ( obj == null ) return ("");

        // If this is an array we need to handle each element individually
        if ( obj.getClass().isArray() ) {
            arrayLength = java.lang.reflect.Array.getLength(obj);
            buffer.append("[ ");
            if ( arrayLength > 0 ) {
                arrayElement = java.lang.reflect.Array.get(obj, 0);
                buffer.append(toJson(arrayElement));
                for ( int j=1; j < arrayLength; j++ ) {
                    buffer.append(",\n");
                    arrayElement = java.lang.reflect.Array.get(obj, j);
                    buffer.append(toJson(arrayElement));
                }
            }
            buffer.append(" ]");
            return buffer.toString();
        }


        Field [] fields = obj.getClass().getDeclaredFields();
        for ( int i=0; i < fields.length; i++ ) {
            if ( fields[i].getModifiers() != Modifier.PUBLIC ) continue;

            Class  classType  = fields[i].getType();

            String fieldName  = fields[i].getName();
            String fieldValue = "";

            try {
                if ( fields[i].get(obj) != null ) {
                    if ( fields[i].getType().equals(java.lang.Integer.TYPE) ) {
                        fieldValue = ""+fields[i].getInt(obj);
                    } else if ( fields[i].getType().equals(java.lang.Boolean.TYPE) ) {
                        fieldValue = ""+fields[i].getBoolean(obj);
                    } else if ( fields[i].getType().equals(java.lang.Long.TYPE) ) {
                        fieldValue = ""+fields[i].getLong(obj);
                    } else if ( fields[i].getType().equals(java.lang.Double.TYPE) ) {
                        fieldValue = ""+fields[i].getDouble(obj);
                    } else if ( fields[i].getType().getName().equals("java.lang.String") ) {
                        fieldValue = "\"" + nvl((String)fields[i].get(obj)).replaceAll("\\\"","\\\\\"") + "\"";
                    } else {
                        if ( fields[i].get(obj).getClass().isArray() ) {
                            arrBuffer.setLength(0);
                            isClassArray = fields[i].getType().toString().startsWith("class [L") 
                                    && ! classType.getName().equals("[Ljava.lang.String;");
                            arrayLength = java.lang.reflect.Array.getLength(fields[i].get(obj));
                            for (int j = 0; j < arrayLength; j++) {
                                arrayElement = java.lang.reflect.Array.get(fields[i].get(obj), j);
                                arrBuffer.append("\n { \"" + fieldName.replaceAll("s$","") + "\": ");
                                arrBuffer.append((isClassArray ? "\n" + toJson(arrayElement) : "\"" + arrayElement + "\""));
                                arrBuffer.append(" } ");
                                if ( j < arrayLength-1 ) arrBuffer.append(",");
                                arrBuffer.append(" ");
                            }
                            fieldValue = "[ " + arrBuffer.toString() + " ]\n";
                            arrBuffer.setLength(0);
                        } else {
                            // We'll assume this is a simple Class
                            if ( fields[i].get(obj) != this ) fieldValue = "\n" + toJson(fields[i].get(obj));
                        }
                    }
                }
            } catch (Exception e) {
            fieldValue = e.toString();
            }

            if ( buffer.length() > 0 ) buffer.append(", ");
            buffer.append( "\"" + fieldName + "\": " + (fieldValue.length() == 0 ? "\"\"" : fieldValue));
        }

        return "{ " + buffer.toString() + " }";
    }

}
