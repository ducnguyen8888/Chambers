<%@ page import="act.util.*,java.util.*,java.util.logging.*,java.sql.*,java.net.*,java.io.*,java.math.*" 
%><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");

    // Add formats

    ClientWebsiteConfiguration configuration    = new ClientWebsiteConfiguration(request);

    String  dataSource                          = configuration.dataSource;
    String  clientId                            = configuration.clientId;

    int     timeZoneOffset                      = configuration.timeZoneOffset;
    int     maximumSearchRecords                = configuration.maximumSearchRecords;

	boolean allowRestrictedAccounts             = configuration.allowRestrictedAccounts;

    String  reportServerURI                     = configuration.reportServerURI;


%><%!













Exception INVALID_REQUEST = new Exception("Invalid Request");


boolean isDefined(Object object) { return object != null; }
boolean notDefined(Object object) { return object == null; }

boolean isDefined(String value) { return value != null && value.length() > 0; }
boolean notDefined(String value) { return value == null || value.length() == 0; }


boolean isDefined(String... values) {
    if ( values == null ) return false;
    for ( String value : safe(values) ) {
        if ( value == null || value.length() == 0 ) return false;
    }
    return true;
}


String[] safe(String[] values)       { return (values != null ? values : new String[0]); }
String ndef(String... values) {
    for ( String value : safe(values) ) {
        if ( value != null && isDefined(value) ) return value;
    }
    return null;
}

String nvl(String... values) {
    for ( String value : safe(values) ) {
        if ( value != null ) return value;
    }
    return "";
}
String nvl(String defaultValue, String[] values) {
    for ( String value : safe(values) ) {
        if ( value != null ) return value;
    }
    return defaultValue;
}
String nvl(String[] values, String defaultValue) {
    for ( String value : safe(values) ) {
        if ( value != null ) return value;
    }
    return defaultValue;
}

int nvl(int defaultValue, String... values) {
    for ( String value : safe(values) ) {
        try {
            return Integer.parseInt(value); 
        } catch (Exception ignore) {
        }
    }
    return defaultValue;
}
int nvl(String value, int defaultValue) {
    try {
        return Integer.parseInt(value); 
    } catch (Exception ignore) {
    }
    return defaultValue;
}

long nvl(long defaultValue, String... values) {
    for ( String value : safe(values) ) {
        try {
            return Long.parseLong(value); 
        } catch (Exception ignore) {
        }
    }
    return defaultValue;
}
long nvl(String value, long defaultValue) {
    try {
        return Long.parseLong(value); 
    } catch (Exception ignore) {
    }
    return defaultValue;
}

double nvl(double defaultValue, String... values) {
    for ( String value : safe(values) ) {
        try {
            return Double.parseDouble(value); 
        } catch (Exception ignore) {
        }
    }
    return defaultValue;
}
double nvl(String value, double defaultValue) {
    try {
        return Double.parseDouble(value); 
    } catch (Exception ignore) {
    }
    return defaultValue;
}

%><%!

public InterruptedException extend(InterruptedException exception, String message) throws SQLException {
    return new InterruptedException(String.format("%s: %s",message,exception.getMessage()));
}
public SQLException extend(SQLException exception, String message) throws SQLException {
    return new SQLException(String.format("%s: %s",message,exception.getMessage()));
}
public Exception extend(Exception exception, String message) throws Exception {
    return exception.getClass().getConstructor(new Class[]{(new String()).getClass()}).newInstance((Object[])(new String[]{message + ": " + exception.getMessage()}));
}

void logException(String msg) { act.log.ErrorLog.severe(msg); }
void logException(Exception exception) { logException(exception.toString()); }
void logException(String summary, String detail) { logException(String.format("%s\n%s", summary, "\n\n\t" + detail.replaceAll("\n","\n\t"))); }
void logException(javax.servlet.http.HttpServletRequest request, String summary) {
    logException(String.format("%s\n\tPage: %s", summary, request.getRequestURI()));
}
void logException(javax.servlet.http.HttpServletRequest request, String summary, String detail) {
    logException(String.format("%s\n\tPage: %s\n%s", summary, request.getRequestURI(), "\n\n\t" + detail.replaceAll("\n","\n\t")));
}
%>