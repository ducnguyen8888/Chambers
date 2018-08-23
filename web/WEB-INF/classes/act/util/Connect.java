package act.util;

import java.util.*;
import java.sql.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;


/**
 * Provides convenience methods to open database connections and retrieve basic connection information.
 * <p>
 * This class provides four main database function groups:
 * </p>
 * <br>
 * <ul>
 * <li> Open database connection, either by JNDI name or JDBC:thin connection information </li>
 * <li> Returns the database name (from global__name) of the connection </li>
 * <li> Returns whether the database connection user has an specific role granted (from user_role_privs table) </li>
 * <li> Returns a list of roles granted to the the database connection user (from user_role_privs table) </li>
 * </ul>
 * <p>
 * The method function groups support calls with the use of a JNDI data source name using the default user and
 * a JNDI data source name with specific user/password. All method function groups, with the exception of the 
 * open connection function group, also allow the use of an existing open java.sql.Connection.
 *  </p>
 * 
 * <p> Important: Do not specify a username/password for JNDI dataources. WebLogic will return any available
 * connection that is already open regardless of the username. If a connection isn't already available then
 * WebLogic will open a new connection using the specified username/password but then will return the still
 * open connection to the general use pool for anyone else to use. This creates a potential security issue
 * when relying on the user roles and permissions to control access. This also creates a potential technical
 * problem when the connection returned is a different user with different permissions/schema than what was
 * expected
 * </p>
 * 
 * <pre>
 * 
 * try ( Connection con = Connect.open("jdbc/development"); ) {
 *
 *       String dbName = null;
 *       try {
 *              dbName = Connect.getName(conn);
 *       } catch (Exception e) {
 *              throw Connect.extendException(e,"Failed to retrieve database name");
 *       }
 *
 *       try ( Statement stmt = con.createStatement(); 
 *             ResultSet rs   = stmt.executeQuery("select count(*) from dual"); 
 *             ) {
 *             int count = rs.getInt(1);
 *       } catch (Exception e) {
 *            throw Connect.extendException(e,"Failed to retrieve data");
 *       }
 *
 *       ... other code ...
 *
 * } catch (Exception e) {
 *       log.error(e);
 * }
 * </pre>
 */
public class Connect {
    public Connect() { super(); }


    /** Opens a new database connection to the specified data source.
     * @param datasource a jdbc data source or JNDI named data source 
     * @return a new Connection to the database specified by data source using the default user
     * @throws Exception if an error occurs opening the connection
     */
    public static Connection open(String datasource) throws Exception {
        return open(datasource,null,null);
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
        try ( Connection con=openJNDI(datasource) ) {
            return con.getMetaData().getURL();
        }
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
     * @param databaseURL a jdbc database URL (i.e. jdbc:oracle:thin:@ares:1521:actd)
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

    /** Returns the name of the database as defined in the global_name table in the database.
     *  <p>
     *  A connection to the database is temporarily created to retrieve the name.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @return the name of the database specified by data source
     * @throws Exception if an error occurs opening the connection or retrieving the name
     */
    public static String getName(String datasource) throws Exception {
        return getName(datasource,null,null);
    }


    /** Returns the name of the database as defined in the global_name table in the database.
     *  <p>
     *  A connection to the database is temporarily created to retrieve the name.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @return the name of the database specified by data source
     * @throws Exception if an error occurs opening the connection or retrieving the name
     */
    public static String getName(String datasource, String user, String password) throws Exception {
        try ( Connection connection = (user != null ? open(datasource,user,password) : open(datasource)); ) {
            return getName(connection);
        }
    }


    /** Returns the name of the database as defined in the global_name table in the database
     * @param connection an open database connection
     * @return the name of the database connected to
     * @throws Exception if an error occurs retrieving the name
     */
    public static String getName(Connection connection) throws Exception {
        try ( Statement statement = connection.createStatement(); 
              ResultSet resultSet = statement.executeQuery("select global_name from global_name");
            ) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }


    /** Returns the user name of the database connection user.
     *  <p>
     *  A connection to the database is temporarily created to retrieve the user name.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @return the name of the database specified by data source
     * @throws Exception if an error occurs opening the connection or retrieving the name
     */
    public static String getUser(String datasource) throws Exception {
        try ( Connection connection = open(datasource); ) {
            return getUser(connection);
        }
    }

    /** Returns the user name of the database connection user.
     * @param connection an open database connection
     * @return the name of the database connected to
     * @throws Exception if an error occurs retrieving the name
     */
    public static String getUser(Connection connection) throws Exception {
        try ( Statement statement = connection.createStatement(); 
              ResultSet resultSet = statement.executeQuery("select user from dual");
            ) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }


    /** Returns the schema name the data source connects to
     *  <p>
     *  A connection to the database is temporarily created to retrieve the schema name.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @return the schema name the datasource user is connected to by default
     * @throws Exception if an error occurs opening the connection or retrieving the name
     */
    public static String getSchema(String datasource) throws Exception {
        return getSchema(datasource, null, null);
    }

    /** Returns the schema name the data source connects to when logged in as
     *  the specified user
     *  <p>
     *  A connection to the database is temporarily created to retrieve the name.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @return the schema name the datasource/user connects to by default
     * @throws Exception if an error occurs opening the connection or retrieving the name
     */
    public static String getSchema(String datasource, String user, String password) throws Exception {
        try ( Connection connection = (user != null ? open(datasource,user,password) : open(datasource)); ) {
            return getSchema(connection);
        }
    }

    /** Returns the current schema name of the database connection
     * @param connection an open database connection
     * @return the schema name the connection is using
     * @throws Exception if an error occurs retrieving the name
     */
    public static String getSchema(Connection connection) throws Exception {
        try ( Statement statement = connection.createStatement(); 
              ResultSet resultSet = statement.executeQuery("select sys_context('userenv','current_schema') schema from dual");
            ) {
            resultSet.next();
            return resultSet.getString("schema");
        }
    }


    /** Returns whether the user the datasource connects as has been granted the specified user entitlement.
     *  <p>
     *  User entitlement grants are stored in the user_security table.
     *  A connection to the database is temporarily created to determine whether 
     *  the entitlement has been granted or not.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param entitlement  entitlement name to checked whether it is granted or not
     * @return true if the entitlement has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the entitlement
     */
    public static boolean hasEntitlement(String datasource, String clientId, String entitlement) throws Exception {
        return hasEntitlement(datasource, null, null, clientId, entitlement);
    }


    /** Returns whether the user the datasource connects as has been granted the specified user entitlement.
     *  <p>
     *  User entitlement grants are stored in the user_security table.
     *  A connection to the database is temporarily created to determine whether 
     *  the entitlement has been granted or not.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @param entitlement  entitlement name to checked whether it is granted or not
     * @return true if the entitlement has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the entitlement
     */
    public static boolean hasEntitlement(String datasource, String user, String password, String clientId, String entitlement) throws Exception {
        try ( Connection connection = (user != null ? open(datasource,user,password) : open(datasource)); ) {
            return hasEntitlement(connection, clientId, entitlement);
        }
    }


    /** Returns whether the database connected user has been granted the specified user entitlement.
     *  <p>
     *  User entitlement grants are stored in the user_security table.
     *  </p>
     * @param connection an open database connection
     * @param entitlement  entitlement name to checked whether it is granted or not
     * @return true if the entitlement has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the entitlement
     */
    public static boolean hasEntitlement(Connection connection, String clientId, String entitlement) throws Exception {
        try ( PreparedStatement preparedStatement = connection.prepareStatement(
                             "select 1 from user_security "
                            + " where client_id=nvl(?,client_id) and username=user "
                            + "   and form_name=upper(?) and allow='Y'"
                                                                ); 
            ) {
            preparedStatement.setString(1,clientId);
            preparedStatement.setString(2,entitlement);
            try ( ResultSet resultSet = preparedStatement.executeQuery(); ) {
                resultSet.next();
                return (resultSet.getInt(1) > 0);
            }
        }
    }


    /** Returns whether the database connection user has been granted the specified database role.
     *  <p>
     *  User roles are determined by the granted_role column of the user_role_privs table.
     *  A connection to the database is temporarily created to determine whether the role has been granted or not.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param role  role name to checked whether it is granted or not
     * @return true if the role has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the role
     */
    public static boolean hasRole(String datasource, String role) throws Exception {
        return hasRole(datasource, null, null, role);
    }


    /** Returns whether the database connection user has been granted the specified database role.
     *  <p>
     *  User roles are determined by the granted_role column of the user_role_privs table.
     *  A connection to the database is temporarily created to determine whether the role has been granted or not.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @param role  role name to checked whether it is granted or not
     * @return true if the role has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the role
     */
    public static boolean hasRole(String datasource, String user, String password, String role) throws Exception {
        try ( Connection connection = (user != null ? open(datasource,user,password) : open(datasource)); ) {
            return hasRole(connection, role);
        }
    }


    /** Returns whether the database connection user has been granted the specified database role.
     *  <p>
     *  User roles are determined by the granted_role column of the user_role_privs table.
     *  </p>
     * @param connection an open database connection
     * @param role  role name to checked whether it is granted or not
     * @return true if the role has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the role
     */
    public static boolean hasRole(Connection connection, String role) throws Exception {
        try ( PreparedStatement preparedStatement = connection.prepareStatement(
                             "select 1 from user_role_privs where username=user and upper(granted_role)=upper(?)"
                                                                ); 
            ) {
            preparedStatement.setString(1,role);
            try ( ResultSet resultSet = preparedStatement.executeQuery(); ) {
                resultSet.next();
                return (resultSet.getInt(1) > 0);
            }
        }
    }


    /** Returns a list of roles granted to the data source user.
     *  <p>
     *  User roles are retrieved from the granted_role column of the user_role_privs table.
     *  A connection to the database is temporarily created to retrieve the granted roles.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @return String array of roles granted to the data source user
     * @throws Exception if an error occurs opening the connection or retrieving roles
     */
    public static String [] getRoles(String datasource) throws Exception {
        return getRoles(datasource, null, null);
    }

    /** Returns a list of roles granted to the specified user.
     *  <p>
     *  User roles are retrieved from the granted_role column of the user_role_privs table.
     *  A connection to the database is temporarily created to retrieve the granted roles.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @return String array of roles granted to the specified user
     * @throws Exception if an error occurs opening the connection or retrieving roles
     */
    public static String [] getRoles(String datasource, String user, String password) throws Exception {
        try ( Connection connection = (user != null ? open(datasource,user,password) : open(datasource)); ) {
            return getRoles(connection);
        }
    }

    /** Returns a list of roles granted to the connection user.
     *  <p>
     *  User roles are retrieved from the granted_role column of the user_role_privs table.
     *  </p>
     * @param connection an open database connection
     * @return String array of roles granted to the specified user
     * @throws Exception if an error occurs opening the connection or retrieving roles
     */
    public static String [] getRoles(Connection connection) throws Exception {
        ArrayList         roles   = new ArrayList();

        try ( Statement statement = connection.createStatement(); 
              ResultSet resultSet = statement.executeQuery("select granted_role from user_role_privs "
                                                            + " where username=user order by granted_role"
                                                           );
            ) {
            resultSet.next();
            roles.add(resultSet.getString(1));
        }

        return (String []) roles.toArray(new String[0]);
    }


    /**Returns whether the user can log into the database successfully or not
     * <p>
     * A connection to the database is temporarily created to determine whether the user can log in or not
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @return true if the user can log into the database
     * @throws Exception if an error other than invalid user/password occurs opening the connection 
     */
    public static boolean isDatabaseUser(String datasource, String user, String password) throws Exception {
        boolean    isValidUser   = false;

        try ( Connection connection = open(datasource,user,password); ) {
            isValidUser = true;
        } catch (SQLException exception) {
            // If not invalid user/password throw error
            if ( exception.getMessage().indexOf("ORA-01017:") < 0 ) throw exception;
        }

        return isValidUser;
    }

    /** Returns whether the specified database user has an Oracle account and has 
     *  been granted the specified database role.
     *  <p>
     *  User roles are determined by the granted_role column of the user_role_privs table.
     *  A connection to the database is temporarily created to determine whether the role has been granted or not.
     *  </p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @param role  role name to checked whether it is granted or not
     * @return true if the user is a valid database user and the role has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the role
     */
    public static boolean isValidUser(String datasource, String user, String password, String role) throws Exception {
        boolean    isValidUser   = false;

        try ( Connection connection = open(datasource,user,password); ) {
            isValidUser = hasRole(connection, role);
        } catch (Exception exception) {
            // If not invalid user/password throw error
            if ( exception.getMessage().indexOf("ORA-01017:") < 0 ) throw exception;
        }

        return isValidUser;
    }

    /** Returns whether the specified database user has an Oracle account and has 
     *  been granted the specified database role or user entitlement.
     *  <p>User roles are determined by the granted_role column of the user_role_privs table.</p>
     *  <p>User entitlement grants are stored in the user_security table.</p>
     * @param datasource a jdbc data source or JNDI named data source
     * @param user user to connect as
     * @param password user's password to connect
     * @param role  role name to checked whether it is granted or not
     * @return true if the user is a valid database user and the role has been granted to the user, false otherwise
     * @throws Exception if an error occurs opening the connection or verifying the role
     */
    public static boolean isValidUser(String datasource, String user, String password,
                                        String clientId, String roleOrEntitlement) throws Exception {
        boolean    isValidUser   = false;

        try ( Connection connection = open(datasource,user,password); ) {
            isValidUser = hasRole(connection,roleOrEntitlement) || hasEntitlement(connection,clientId,roleOrEntitlement);
        } catch (Exception exception) {
            // If not invalid user/password throw error
            if ( exception.getMessage().indexOf("ORA-01017:") < 0 ) throw exception;
        }

        return isValidUser;
    }

    /** Throws a new exception of the same type with the provided message prefixed to the existing exception message.
     *  <p>
     *  A new exception is created of the same type as the specified exception. The exception message remains the
     *  same but is prefixed by the user specified message. Useful to provide code location information in an exception.
     *  </p>
     *  <p>
     *  Calling this function throws the new exception, it does not return the exception object.
     *  <pre>
     *       try {
     *              ...
     *       } catch (Exception e) {
     *              throw Connect.extendException(e,"Some additional message");
     *       }
     *  </pre>
     * @param e Exception to extend
     * @param message message to prefix to existing exception message
     * @throws Exception newly created exception of the same class type with the same exception message prefixed by the specified message
     */
    public static void extend(Exception e, String message) throws Exception {
        throw (Exception) e.getClass().getConstructor(new Class[]{(new String()).getClass()}).newInstance((Object[])(new String[]{message + ". " + e.getMessage()}));
    }

    /** Returns a new exception of the same type with the provided message prefixed to the existing exception message.
     *  <p>
     *  A new exception is created of the same type as the specified exception. The exception message remains the
     *  same but is prefixed by the user specified message. Useful to provide code location information in an exception.
     *  </p>
     *  <pre>
     *       try {
     *              ...
     *       } catch (Exception e) {
     *              throw Connect.extendException(e,"Some additional message");
     *       }
     *  </pre>
     * @param e Exception to extend
     * @param message message to prefix to existing exception message
     * @throws Exception newly created exception of the same class type with the same exception message prefixed by the specified message
     */
    public static Exception extendException(Exception e, String message) throws Exception {
        return (Exception) e.getClass().getConstructor(new Class[]{(new String()).getClass()}).newInstance((Object[])(new String[]{message + ". " + e.getMessage()}));
    }
    public static SQLException extendException(SQLException e, String message) throws Exception {
        return new SQLException(message + ". " + e.getMessage());
    }
    public static InterruptedException extendException(InterruptedException e, String message) throws Exception {
        return new InterruptedException(message + ". " + e.getMessage());
    }

    public static boolean isDefined(String val) { return val != null && val.length() > 0; }
}
