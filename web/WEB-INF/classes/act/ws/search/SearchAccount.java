package act.ws.search;

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

public class SearchAccount {
    public SearchAccount() {}

    public SearchAccount( String can, String ownerno,
                    String nameline1, String nameline2, String nameline3, String nameline4,
                    String city, String state, String zipcode,
                    String pnumber, String pstrname, String aprdistacc,
                    String legal1, String legal2, String legal3, String legal4, String legal5 ) {
        this.can           = can;
        this.ownerno       = ownerno;
        this.aprdistacc    = aprdistacc;

        this.setOwner(nameline1, nameline2, nameline3, nameline4, city, state, zipcode)
            .setPropertyAddress(pnumber, pstrname)
            .setLegal(legal1, legal2, legal3, legal4, legal5);
    }

    public SearchAccount( String can, String ownerno, boolean special_status, String acctstatus,
                    String nameline1, String nameline2, String nameline3, String nameline4,
                    String city, String state, String zipcode,
                    String pnumber, String pstrname, String aprdistacc,
                    String legal1, String legal2, String legal3, String legal4, String legal5 ) {

        this( can, ownerno,
                    nameline1, nameline2, nameline3, nameline4,
                    city, state, zipcode,
                    pnumber, pstrname, aprdistacc,
                    legal1, legal2, legal3, legal4, legal5 );

        this.specialStatus = special_status;
        this.acctstatus    = acctstatus;
    }



    /**Sets the public fields from the property values.
     * Only the base type values are set, int, long, double, boolean, and String
     * @throws Exception if a processing error occurs
     */
    public SearchAccount(ResultSet rs) throws Exception {
        Field [] fields = this.getClass().getDeclaredFields();
        for ( int i=0; i < fields.length; i++ ) {
            if ( (fields[i].getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC ) continue;
            Class  classType  = fields[i].getType();
            String fieldName  = fields[i].getName();
            String fieldValue = null;

            // Field may not be part of result set
            try {
                fieldValue = rs.getString(fieldName);
            } catch (Exception e) {
                continue;
            }

            try {
                if ( classType.equals(java.lang.Integer.TYPE) ) {
                    fields[i].setInt(this,getInt(fieldValue,0));
                } else if ( classType.equals(java.lang.Boolean.TYPE) ) {
                    fields[i].setBoolean(this,isTrue(fieldValue));
                } else if ( classType.equals(java.lang.Long.TYPE) ) {
                    fields[i].setLong(this,getLong(fieldValue,0L));
                } else if ( classType.equals(java.lang.Double.TYPE) ) {
                    fields[i].setDouble(this,getDouble(fieldValue,0.0));
                } else if ( classType.getName().equals("java.lang.String") ) {
                    fields[i].set(this,getString(fieldValue,""));
                }
            } catch (Exception e) {
                throw e;
            }
        }
        if ( ! this.hasBalanceDue && this.amountDue > 0 ) this.hasBalanceDue = true;
    }



    public SearchAccount setLegal( String legal1, String legal2, String legal3, String legal4, String legal5 ) {
        this.legal1        = legal1;
        this.legal2        = legal2;
        this.legal3        = legal3;
        this.legal4        = legal4;
        this.legal5        = legal5;

        return this;
    }

    public SearchAccount setPropertyAddress( String pnumber, String pstrname ) {
        this.pnumber       = pnumber;
        this.pstrname      = pstrname;

        return this;
    }

    public SearchAccount setOwner( String nameline1, String nameline2, String nameline3, String nameline4,
                    String city, String state, String zipcode ) {
        this.nameline1     = nameline1;
        this.nameline2     = nameline2;
        this.nameline3     = nameline3;
        this.nameline4     = nameline4;

        this.city          = city;
        this.state         = state;
        this.zipcode       = zipcode;

        return this;
    }



    public String    nameline1     = null,
                     nameline2     = null,
                     nameline3     = null,
                     nameline4     = null,

                     city          = null,
                     state         = null,
                     zipcode       = null,

                     can           = null,
                     ownerno       = null,

                     legal1        = null,
                     legal2        = null,
                     legal3        = null,
                     legal4        = null,
                     legal5        = null,

                     pnumber       = null,
                     pstrname      = null,
                     aprdistacc    = null,
                     acctstatus    = null;

    public boolean   specialStatus = false;
    public boolean   hasBalanceDue = false;

    public double    amountDue     = 0.0;



    /** Returns whether this account is a restricted account base on the special_status field = 'Y'
     *  @returns true if this account is restricted, otherwise false
     */
    public boolean isRestricted() { return specialStatus; }


    /** Returns whether this account has a balance due or not
     *  @returns true if this account has a levy balance due, otherwise false
     */
    public boolean hasBalanceDue() { return hasBalanceDue || amountDue > 0; }

    public boolean isDefined(String val) { return val != null && val.length() > 0; }

    public String getProperty() {
        StringBuffer buffer = new StringBuffer();

        if ( isDefined(pnumber) ) {
            buffer.append(pnumber);
            buffer.append(" ");
        }

        if ( isDefined(pstrname) ) {
            buffer.append(pstrname);
            buffer.append("<br>\n");
        }

        return buffer.toString().trim();
    }

    public String getLegal() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(legal1);
        buffer.append("<br>\n");

        if ( isDefined(legal2) ) {
            buffer.append(legal2);
            buffer.append("<br>\n");
        }

        if ( isDefined(legal3) ) {
            buffer.append(legal3);
            buffer.append("<br>\n");
        }
        if ( isDefined(legal4) ) {
            buffer.append(legal4);
            buffer.append("<br>\n");
        }
        if ( isDefined(legal5) ) {
            buffer.append(legal5);
            buffer.append("<br>\n");
        }

        return buffer.toString();
    }

    public String getOwner() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(nameline1);
        buffer.append("<br>\n");

        if ( isDefined(nameline2) ) {
            buffer.append(nameline2);
            buffer.append("<br>\n");
        }
        if ( isDefined(nameline3) ) {
            buffer.append(nameline3);
            buffer.append("<br>\n");
        }
        if ( isDefined(nameline4) ) {
            buffer.append(nameline4);
            buffer.append("<br>\n");
        }

        if ( isDefined(city) ) buffer.append(city);
        if ( isDefined(city) && isDefined(state) ) buffer.append(", ");
        if ( isDefined(state) ) buffer.append(state);
        if ( isDefined(city) || isDefined(state) ) buffer.append("  ");
        if ( isDefined(zipcode) ) buffer.append(zipcode);

        return buffer.toString();
    }

    public String toString() { return getOwner(); }


    /** Returns a property value as an integer value, returns default value if value isn't an integer value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public String getString(String value, String def) {
        return (value == null ? def : value);
    }


    /** Returns a property value as an integer value, returns default value if value isn't an integer value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public int getInt(String property, int def) {
        try {
            return Integer.parseInt(property);
        } catch (Exception e) {
        }
        return def;
    }

    
    /** Returns a property value as a long value, returns default value if value isn't a long value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public long getLong(String property, long def) {
        try {
            return Long.parseLong(property);
        } catch (Exception e) {
        }
        return def;
    }


    /** Returns a property value as a double value, returns default value if value isn't a double value
     * @param property property to return the value of
     * @param def value to return if property value is null
     * @returns property value or default value if value is null
     */
    public double getDouble(String property, double def) {
        try {
            return Double.parseDouble(property);
        } catch (Exception e) {
        }
        return def;
    }


    /** Returns a property value as a boolean true/false value, value must be "true" or "Y" to return true
     * @param property property to return the boolean value of
     * @returns true if property value is "TRUE" or "Y", false otherwise
     */
    public boolean isTrue(String property) {
        return "true".equalsIgnoreCase(property) || "Y".equalsIgnoreCase(property);
    }




    /** Sorts the search accounts array by owner name and address
     * @param Array of SearchAccount to sort
     */
    public static void sortOnOwner(SearchAccount[] accounts) { Arrays.sort(accounts,sortOnOwner); }

    /** A Comparator class instance that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by owner name and address
     */
    public static final Comparator<SearchAccount> sortOnOwner = new OwnerSort();

    /** A Comparator class that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by owner name and address
     */
    protected static class OwnerSort implements Comparator<SearchAccount> {
        public int compare(SearchAccount a, SearchAccount b) {
            if ( a == null && b == null ) return 0;
            if ( a == null && b != null ) return -1;
            if ( a != null && b == null ) return 1;

            int order = 0;
            for ( String[] field : new String[][] { { a.nameline1, b.nameline1 },
                                                    { a.nameline2, b.nameline2 },
                                                    { a.nameline3, b.nameline3 },
                                                    { a.nameline4, b.nameline4 },
                                                    { a.can, b.can }
                                                    } ) {
                order = nvl(field[0]).compareTo(field[1]);
                if ( order != 0 ) break;
            }
            return order;
        }
    }

    /** Sorts the search accounts array by Account number
     * @param Array of SearchAccount to sort
     */
    public static void sortOnAccount(SearchAccount[] accounts) { Arrays.sort(accounts,sortOnAccount); }

    /** A Comparator class instance that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by account number
     */
    public static final Comparator<SearchAccount> sortOnAccount = new AccountSort();

    /** A Comparator class that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by account number
     */
    protected static class AccountSort implements Comparator<SearchAccount> {
        public int compare(SearchAccount a, SearchAccount b) {
            if ( a == null && b == null ) return 0;
            if ( a == null && b != null ) return -1;
            if ( a != null && b == null ) return 1;

            return nvl(a.can).compareTo(b.can);
        }
    }


    /** Sorts the search accounts array by Aprdistacc (CAD) number
     * @param Array of SearchAccount to sort
     */
    public static void sortOnAprdistacc(SearchAccount[] accounts) { Arrays.sort(accounts,sortOnAprdistacc); }

    /** A Comparator class instance that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by aprdistacc (CAD) number
     */
    public static final Comparator<SearchAccount> sortOnAprdistacc = new AprdistaccSort();

    /** A Comparator class that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by aprdistacc (CAD) number
     */
    protected static class AprdistaccSort implements Comparator<SearchAccount> {
        public int compare(SearchAccount a, SearchAccount b) {
            if ( a == null && b == null ) return 0;
            if ( a == null && b != null ) return -1;
            if ( a != null && b == null ) return 1;

            return nvl(a.aprdistacc).compareTo(b.aprdistacc);
        }
    }


    /** Sorts the search accounts array by property address
     * @param Array of SearchAccount to sort
     */
    public static void sortOnProperty(SearchAccount[] accounts) { Arrays.sort(accounts,sortOnProperty); }

    /** A Comparator class instance that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by property address
     */
    public static final Comparator<SearchAccount> sortOnProperty = new PropertySort();

    /** A Comparator class that can be used by the Arrays.sort(SearchAccount[],Comparator) 
     * method to sort the SearchAccount array by property address
     */
    protected static class PropertySort implements Comparator<SearchAccount> {
        public int compare(SearchAccount a, SearchAccount b) {
            if ( a == null && b == null ) return 0;
            if ( a == null && b != null ) return -1;
            if ( a != null && b == null ) return 1;

            String propertyA = String.format("%s %s", nvl(a.pnumber), nvl(a.pstrname)).trim();
            String propertyB = String.format("%s %s", nvl(b.pnumber), nvl(b.pstrname)).trim();
            return propertyA.compareTo(propertyB);
        }
    }



    public static String nvl(String... values) {
        if ( values == null ) return "";
        for ( String value : values ) {
            if ( value != null ) return value;
        }
        return "";
    }


}
