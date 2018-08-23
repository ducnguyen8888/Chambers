package act.app.security;

import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;

import java.util.*;
import java.sql.*;

import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import javax.sql.DataSource;


public class ApplicationUser extends Properties implements User {
    public ApplicationUser(String id, String key) {
        this(id, key, null);
    }

    public ApplicationUser(String id, String key, String code) {
        this.id     = id;
        this.key    = key;
        this.code   = code;

        validateUser(id, key, code);
    }

    public ApplicationUser(HttpServletRequest request) {
        if ( request == null ) return;
        registerRequest(request);
        request(request);

        this.id                 = request.getParameter(nvl(idParameter,"id"));
        this.key                = request.getParameter(nvl(keyParameter,"key"));
        this.code               = request.getParameter(nvl(codeParameter,"code"));

        validateUser(id, key, code);
        if ( ! isValid ) {
            request.setAttribute("user-login-failure", (failureReason == null ? "unspecified reason" : failureReason.getMessage()));
        }
    }

    public ApplicationUser(HttpServletRequest request, String configuration) {
        if ( request == null ) return;
        registerRequest(request);
        request(request);

        String genericErrorMessage = null;

        try {
            this.loadPropertiesFile(request, configuration);

            String idParameter      = getProperty("id-parameter");
            String keyParameter     = getProperty("key-parameter");
            String codeParameter    = getProperty("code-parameter");
            genericErrorMessage     = getProperty("generic-error-message");

            accessCode              = (isDefined(codeParameter) ? getProperty(codeParameter) : accessCode);
            this.datasource         = nvl(request.getParameter("datasource"),datasource);

            this.id                 = request.getParameter(nvl(idParameter,"id"));
            this.key                = request.getParameter(nvl(keyParameter,"key"));
            this.code               = request.getParameter(nvl(codeParameter,"code"));


            validateUser(id, key, code);
            if ( ! isValid ) {
                request.setAttribute("user-login-failure", ndef(genericErrorMessage,(failureReason == null ? "unspecified reason" : failureReason.getMessage())));
            }
        } catch (Exception exception) {
            failureReason = exception;
            request.setAttribute("user-login-failure", ndef(genericErrorMessage, exception.toString()));
        }
    }
    protected boolean isValidCode(String code) {
        boolean isValidCode = false;
        if ( notDefined(accessCode) )
            isValidCode = true;
        else if ( isDefined(code) ) {
            try {
                // We'll assume that the value is a hash 
                isValidCode = Integer.parseInt(accessCode) == this.code.hashCode();
            } catch (NumberFormatException nfe) {
                // Value is not a hash, we'll assume it's a literal
                isValidCode = accessCode.equals(code);
            } catch (Exception e) {
            }
        }

        return isValidCode;
    }

    protected void validateUser(String id, String key, String code) {
        isValid = false;

        if ( isDefined(id) && isDefined(key) ) {
            if ( ! isValidCode(code) ) {
                failureReason = new Exception("Invalid access code");
            } else {
                try ( Connection connection=open(datasource, id, key); ) {
                    isValid = true;
                } catch (Exception exception) {
                    failureReason = exception;
                }
            }
        } else {
            failureReason = new Exception("User credentials were not defined");
        }
    }
    public void invalidate() {
        isValid = false;
    }

    public boolean isValid() {
        return isValid;
    }

    public void registerRequest(HttpServletRequest request) {
        if ( request == null ) return;
        this.remoteAddr     = request.getRemoteAddr();
        this.remoteHost     = request.getRemoteHost();
        this.forwardedFor   = request.getHeader("X-Forwarded-For");
    }
    public boolean isSameIPAddress(HttpServletRequest request) {
        return request != null 
                && (notDefined(remoteAddr)   || remoteAddr.equals(request.getRemoteAddr()))
                && (notDefined(forwardedFor) || forwardedFor.equals(request.getHeader("X-Forwarded-For")));
    }


    public boolean isAllowedAccess(HttpServletRequest request) {
        return true; // default to all access
    }


    public              String      accessCode          = null;
    public              String      datasource          = "jdbc:oracle:thin:@ares:1521:actd";

    protected           String      idParameter         = "id";
    protected           String      keyParameter        = "key";
    protected           String      codeParameter       = "code";

    public              String      id                  = null;
    public              String      key                 = null;
    public              String      code                = null;


    protected           String      remoteAddr          = null;
    protected           String      remoteHost          = null;
    protected           String      forwardedFor        = null;

    protected           boolean     isValid             = false;
    public              Exception   failureReason       = null;

    public Exception getFailureReason() { return failureReason; }

    public static boolean isDefined(String val) { return val != null && val.length() > 0; }
    public static boolean notDefined(String val) { return val == null || val.length() == 0; }
    public static String nvl(String... values) {
        if ( values != null ) {
            for ( String value : values ) {
                if ( value != null ) return value;
            }
        }

        return "";
    }
    public static String ndef(String... values) {
        if ( values != null ) {
            for ( String value : values ) {
                if ( value != null && value.length() > 0 ) return value;
            }
        }

        return "";
    }



    public StringBuffer access = new StringBuffer();
    public void comment(String comment) {
        if ( comment == null ) return;
        access.append(String.format("%s\n%s\n", 
                                    (new java.util.Date()).toString(),
                                    comment
                                    )
                        );
    }
    public void request(HttpServletRequest request) {
        if ( request == null ) return;
        access.append(String.format("%s\nPage: %s\n", 
                                    (new java.util.Date()).toString(),
                                    request.getServletPath()
                                    )
                        );
    }

    public Connection getConnection() throws Exception {
        if ( ! isValid ) throw new SQLException("Invalid user");
        return open(datasource, id, key);
        
    }


    /** Opens a new database connection to the specified data source connecting as the specified user.
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @return a new Connection to the database specified by data source
     * @throws Exception if an error occurs opening the connection
     */
    public static Connection open(String datasource, String user, String password) throws Exception {
        Connection     connection      = null;

        if ( datasource == null ) throw new SQLException("Data source not specified");

        if ( datasource.startsWith("jdbc:") ) {
            connection = openURL(datasource, user, password);
        } else {
            connection = (isDefined(user) ? openURL(getDatabaseURL(datasource), user, password)
                                          : openJNDI(datasource)
                                          );
        }

        return connection;
    }

    /** Returns the database URL for a specified datasource
     * @param datasource a JNDI named data source
     * @return the database URL of the datasource
     * @throws Exception if an error occurs retrieving the URL
     */
    public static String getDatabaseURL(String datasource) throws Exception {
        if ( datasource == null || datasource.startsWith("jdbc:") ) return datasource;

        String databaseURL = null;
        try ( Connection con=openJNDI(datasource) ) {
            databaseURL = con.getMetaData().getURL();
        }

        return databaseURL;
    }

    /** Opens a new database connection to the specified JNDI data source
     * @param datasource a JNDI named data source
     * @return a new Connection to the database specified by data source
     * @throws Exception if an error occurs opening the connection
     */
    public static Connection openJNDI(String datasource) throws Exception {
        if ( datasource == null ) throw new SQLException("Data source not specified");

        // Due to issues with how WebLogic handles connections we do not want to 
        // specify a username/password when using a JNDI datasource directly. These
        // connections are pooled and will cause problems if a user is specified.
        //
        // WebLogic ignores any specified username/password if a connection
        // is already available in the connection pool. If a connection is not
        // already open then WebLogic will open a new connection using the 
        // specified username/password and then return that connection to the 
        // general use pool when done. This creates a security issue where the
        // expected login user is not the actual login user that is connected.
        //
        // Since WebLogic ignores the specified username/password if a connection
        // is already available we can't rely on the creation of a connection to 
        // validate provided database user credentials.

        return ((DataSource) (new InitialContext()).lookup(datasource)).getConnection();
    }

    /** Opens a new database connection to the specified database URL as the specified user.
     * @param datasource a jdbc database URL (i.e. jdbc:oracle:thin:@ares:1521:actd)
     * @param user user to connect as
     * @param password user's password to connect
     * @return a new Connection to the database specified by data source
     * @throws Exception if an error occurs opening the connection
     */
    public static Connection openURL(String databaseURL, String user, String password) throws Exception {
        if ( databaseURL == null ) throw new SQLException("Database URL not specified");
        if ( user == null || password == null ) throw new SQLException("User not specified");

        // Note from Oracle OTN WebLogic thread
        // https://community.oracle.com/thread/688456?start=0&tstart=0
        //
        // Avoid calling DriverManager in a multithreaded application.
        // That call is class synchronized, as are several other DriverManager
        // calls that JDBC drivers and SQLExceptions call internally, often,
        // so one long-running getConnection() call can halt all other JDBC in the
        // JVM. Call Driver.connect() directly. That's what DriverManager does anyway
        //
        // try {
        // 	Class.forName("oracle.jdbc.driver.OracleDriver"); 
        // } catch ( ClassNotFoundException e ) {
        // 	throw new SQLException("Unable to load database driver");
        // }
        //
        // connection = java.sql.DriverManager.getConnection(datasource, user, password);
        Driver driver = (Driver)(Class.forName("oracle.jdbc.OracleDriver").newInstance());
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        return driver.connect(databaseURL, props);
    }


    public void loadPropertiesFile(ServletRequest request, String configuration) throws IOException {
        String configurationFilename = "/WEB-INF/configuration/" + configuration + ".ini";
        String filepath = request.getServletContext().getRealPath(configurationFilename);

        try ( InputStream in = new FileInputStream(filepath); ) {
            this.load(in);
        }

        return;
    }

}
