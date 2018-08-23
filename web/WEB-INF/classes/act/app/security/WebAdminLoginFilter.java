package act.app.security;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import act.app.security.User;

import java.time.LocalDateTime;

import java.util.*;
import javax.servlet.annotation.*;

@WebFilter(     filterName="webAdminLoginFilter",
                urlPatterns={"/admin/restricted/*"},
                initParams={
                    @WebInitParam(name="userAttrName", 
                                  value="WebAdminUser"),
                    @WebInitParam(name="userClassName", 
                                  value="act.app.security.ApplicationUser"),
                    @WebInitParam(name="loginPage", 
                                  value="/admin/restricted/login.jsp")
                }
           )
public class WebAdminLoginFilter implements Filter {
    @Override
    public void init(FilterConfig config) throws ServletException {
        filterName              = config.getFilterName();
        servletContext          = config.getServletContext();

        try {
            hostName = java.net.InetAddress.getLocalHost().toString().replaceAll("([^/]*)(.*)","$1");
        } catch (Exception e) {
        }
        restrictToIntranetOnly  = isTrue(config.getInitParameter("restrictToIntranetOnly"));
        requireHostNameToAccess = isTrue(config.getInitParameter("requireHostNameToAccess"));


        // If there are any <init-param> in web.xml get them here
        userAttrName            = nvl(config.getInitParameter("userAttrName"),userAttrName);
        userClassName           = nvl(config.getInitParameter("userClassName"));
        userConfigurationFile   = nvl(config.getInitParameter("userConfigurationFile"));
        accessUrlPrefix         = nvl(config.getInitParameter("accessUrlPrefix"));

        loginPage               = nvl(config.getInitParameter("loginPage"));
        loginPageDispatcher     = servletContext.getRequestDispatcher(accessUrlPrefix + loginPage);

        logoutPage              = nvl(config.getInitParameter("logoutPage"),loginPage);
        logoutPageDispatcher    = servletContext.getRequestDispatcher(accessUrlPrefix + logoutPage);

        if ( isDefined(userClassName) ) {
            try {
                Class requestClass = Class.forName("javax.servlet.http.HttpServletRequest");
                userClassConstructor = Class.forName(userClassName)
                                            .getConstructor(requestClass, String.class);
                attemptAutoVerify  = true;
            } catch (Exception e) {
            }
        }
    }

    String              filterName              = null;
    ServletContext      servletContext          = null;

    String              userAttrName            = "WebAdminUser";
    String              userClassName           = null;
    String              userConfigurationFile   = null;
    String              accessUrlPrefix         = null;

    String              loginPage               = null;
    RequestDispatcher   loginPageDispatcher     = null;

    String              logoutPage              = null;
    RequestDispatcher   logoutPageDispatcher    = null;

    Constructor         userClassConstructor    = null;
    boolean             attemptAutoVerify       = false;

    boolean             restrictToIntranetOnly  = false;
    boolean             requireHostNameToAccess = false;
    String              hostName                = null;





    protected boolean isUserOnIntranetIP(HttpServletRequest request) {
        String remoteAddr   = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");

        return (isDefined(forwardedFor) ? (forwardedFor.matches("192\\.168\\..*") || forwardedFor.matches("0:0:0:0:0:0:.*"))
                                        : (notDefined(remoteAddr) || remoteAddr.matches("192\\.168\\..*") || remoteAddr.matches("0:0:0:0:0:0:.*"))
                                        );
    }
    protected boolean isRequestForHostName(HttpServletRequest request) {
        String serverName   = request.getRequestURL().toString().replaceAll("(http://|https://)([^:/]*)(.*)","$2");
        return (hostName == null || hostName.equalsIgnoreCase(serverName));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest  request     = (HttpServletRequest) req;
        HttpServletResponse response    = (HttpServletResponse) res;
        HttpSession         session     = request.getSession(true);


        session.removeAttribute("loginFilterException");
        request.setAttribute("accessUrlPrefix", accessUrlPrefix);


        if ( requireHostNameToAccess && ! isRequestForHostName(request) ) {
            session.setAttribute("loginFilterException", new Exception("Requested host does not match host name"));
            response.sendError(HttpServletResponse.SC_NOT_FOUND); 
            return;
        }

        if ( restrictToIntranetOnly && ! isUserOnIntranetIP(request) ) {
            session.setAttribute("loginFilterException", new Exception("User's IP is not an intranet IP"));
            response.sendError(HttpServletResponse.SC_NOT_FOUND); 
            return;
        }

        // Determine what page is being asked for
        String requestedPage = request.getServletPath();
        boolean isLoginPage  = requestedPage.equals(loginPage);
        boolean isLogoutPage = requestedPage.equals(logoutPage);


        User user = (User) session.getAttribute(userAttrName);
        if ( user != null ) user.request(request);


        request.setAttribute("accessUrlPrefix",accessUrlPrefix);
        request.setAttribute("loginPage",loginPage);
        request.setAttribute("logoutPage",logoutPage);
        request.setAttribute("requestedPage",requestedPage);

        // If the user specifies just the control directory we'll just
        // forward the request to the login page.
        if ( requestedPage.matches("/[^/]*/{0,1}") ) {
            response.sendRedirect(String.format("%s%s",
                                                request.getContextPath(),
                                                loginPage
                                                )
                                    );
            //loginPageDispatcher.forward(request, response);
            return;
        }


        // If the user has already been validated then let the request proceed
        //
        // Leave this check outside any try/catch block to allow execution
        // errors on the target page to be handled by the default exception
        // processing. If we don't then the exception will be returned to this
        // page for handling.
        // Since we are pre-processing the request any exceptions that do
        // occur will include additional calls within the stack trace.
        // Any exceptions thrown by the target page will be wrapped in
        // a java.lang.reflect.InvocationTargetException exception.
        if ( user != null && user.isValid() ) {

            // If this is the login/logout page we'll automatically invalidate the user
            if ( isLoginPage || isLogoutPage ) {
                user.invalidate();
            }

            // If utilizing URL re-write:
            //servletContext.getRequestDispatcher(accessUrlPrefix + requestedPage).forward(request,response);
            // If using normal file system URL:
            //chain.doFilter(request, response);

            servletContext.getRequestDispatcher(accessUrlPrefix + requestedPage).forward(request,response);
            return;
        }


        // If we have the user class information we'll try to auto-verify 
        // the user before determining what to do
        if ( attemptAutoVerify ) {
            try {
                if ( isDefined(userConfigurationFile) )
                    user = (User) userClassConstructor.newInstance(request, userConfigurationFile);
                else
                    user = (User) userClassConstructor.newInstance(request);
            } catch (Exception userInstantiationException) {
                session.setAttribute("loginFilterException", userInstantiationException);
            }

            if ( user != null ) {
                session.setAttribute(userAttrName,user);

                if ( user.isValid() ) {
                    servletContext.getRequestDispatcher(accessUrlPrefix + requestedPage).forward(request,response);
                    return;
                }
            }
        }


        // User is either undefined or unverified, either way
        // we want them to go to the login/logout page
        if ( isLogoutPage ) {
            logoutPageDispatcher.forward(request, response);
            return;
        }

        loginPageDispatcher.forward(request, response);
        return;
    }

    @Override
    public void destroy() {
        // If you have assigned any expensive resources as fields of
        // this Filter class, then you could clean/close them here.
    }


    public boolean isDefined(String val) {
        return val != null && val.length() > 0; 
    }
    public boolean notDefined(String val) {
        return val == null || val.length() == 0; 
    }
    public String nvl(String val, String def) {
        return (val != null && val.length() > 0 ? val.trim() : def); 
    }
    public String nvl(String val) {
        return (val != null && val.length() > 0 ? val.trim() : ""); 
    }
    public boolean isTrue(String val) {
        return (val != null && "true".equalsIgnoreCase(val));
    }
}
/*
SERVLET CONTEXT
There are two main ways we can get the servlet context:
    1) from within the FilterConfig object in "init()"
        servletContext = config.getServletContext();
    2) from the request in "doFilter()"
        session        = request.getSession(true);
        servletContext = session.getServletContext();

The servlet context is needed if we are to re-route the
request. To re-route the request we use the RequestDispatcher:
        RequestDispatcher dispatcher = servletContext.getRequestDispatcher("somefile.jsp");
        dispatcher.forward(request, response);

To just allow the request to continue un-impeded we trigger the
next Filter in the chain from FilterChain in "doFilter()"
        filterChain.doFilter(request, response);

To output to the response stream:
        response.getWriter().write("text to output");

*/