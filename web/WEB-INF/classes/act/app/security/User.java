package act.app.security;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

public interface User {
    public boolean isValid();
    public void invalidate();
    public void request(HttpServletRequest request);
    public Exception getFailureReason();
    public Connection getConnection() throws Exception;
}
