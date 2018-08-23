package act.util;

import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

public class ClientWebsiteConfiguration extends Configuration {
    public ClientWebsiteConfiguration(HttpServletRequest request) throws IOException {
        applicationName = request.getServletPath().replaceAll("^/([^/]*)/.*","$1");
        load(request.getServletContext(), applicationName);
        this.setInstanceValues();
    }

    public ClientWebsiteConfiguration(PageContext pageContext) throws IOException {
        super(pageContext);
    }
    public ClientWebsiteConfiguration(ServletContext application) throws IOException {
        super(application);
    }


    public static final String configurationDirectory = String.format("%s/%s", 
                                                                      Configuration.configurationDirectory, 
                                                                      "clientWebsites/");
    public String getConfigurationDirectory() {
        return configurationDirectory;
    }

    public ClientWebsiteConfiguration(ServletContext application, String name) throws IOException {
        applicationName = name;
        load(application, applicationName);
        this.setInstanceValues();
    }

    public void ssload(PageContext pageContext, String name) throws IOException {
        super.load(pageContext, String.format("clientWebsites/%s",name));
    }
    public void ssload(ServletContext application, String name) throws IOException {
        super.load(application, String.format("clientWebsites/%s",name));
    }
    public void ssstore(String name) throws IOException {
        super.store(String.format("clientWebsites/%s",name));
    }

    public static final ConcurrentHashMap<String,ClientWebsiteConfiguration> configurationCache = new ConcurrentHashMap<String,ClientWebsiteConfiguration>();

    public static ClientWebsiteConfiguration get(HttpServletRequest request) throws IOException {
        String applicationName = request.getServletPath().replaceAll("^/([^/]*)/.*","$1");
        if ( ! configurationCache.containsKey(applicationName) ) {
           configurationCache.putIfAbsent(applicationName,new ClientWebsiteConfiguration(request));
        }
        return configurationCache.get(applicationName);
    }

    public static void clear(HttpServletRequest request) throws IOException {
        String applicationName = request.getServletPath().replaceAll("^/([^/]*)/.*","$1");
        configurationCache.remove(applicationName);
        return;
    }
    public static void clear(String applicationName) throws IOException {
        if ( applicationName != null ) configurationCache.remove(applicationName);
        return;
    }
    public static void clearAll() throws IOException {
        configurationCache.clear();
        return;
    }





    public String   applicationName             = null;

    public String   dataSource                  = null;
    public String   clientId                    = null;
    public int      client_id                   = 0;

    public int      timeZoneOffset              = 0;
    public int      maximumSearchRecords        = 100;

    public boolean  allowRestrictedAccounts     = false;

    public String   reportServerURI             = null;


    protected void setInstanceValues() {
        dataSource                  = getString("dataSource");
        clientId                    = getString("clientId");
        client_id                   = getInt("clientId");

        timeZoneOffset              = getInt("timeZoneOffset");
        maximumSearchRecords        = getInt("maximumSearchRecords");

        allowRestrictedAccounts     = getBoolean("allowRestrictedAccounts");

        reportServerURI             = getString("reportServerURI");
    }
}