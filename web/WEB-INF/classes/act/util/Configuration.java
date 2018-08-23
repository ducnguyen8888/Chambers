package act.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import java.nio.file.Files;

import java.nio.file.Paths;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;

public class Configuration extends Properties {
    public static final String configurationDirectory = "WEB-INF/configuration/";

    public String getConfigurationDirectory() {
        return configurationDirectory;
    }

    public static final String configurationExtension = ".cfg";

    public String getConfigurationExtension() {
        return configurationExtension;
    }
    public Configuration() {
    }

    public Configuration(PageContext pageContext) throws IOException {
        setRootDirectory(pageContext);
    }
    public Configuration(PageContext pageContext, String filename) throws IOException {
        load(pageContext, filename);
    }

    public Configuration(PageContext pageContext, String filename, Properties defaults) throws IOException {
        super(defaults);
        load(pageContext, filename);
    }

    public Configuration(ServletContext application) throws IOException {
        setRootDirectory(application);
    }
    public Configuration(ServletContext application, String filename) throws IOException {
        load(application, filename);
    }

    public Configuration(ServletContext application, String filename, Properties defaults) throws IOException {
        super(defaults);
        load(application, filename);
    }

    protected void setRootDirectory(PageContext pageContext) throws IOException {
        try {
            ServletContext application = pageContext.getServletContext();
            this.baseDirectory = (application.getRealPath("") + "/");
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
    }

    protected void setRootDirectory(ServletContext application) throws IOException {
        try {
            this.baseDirectory = (application.getRealPath("") + "/");
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
    }

    public String getRootDirectory() throws IOException {
        return getBaseDirectory() + getConfigurationDirectory();
    }

    protected String propertyFilename = null;

    public String getFilename() {
        return this.propertyFilename;
    }

    public void setFilename(String filename) throws IOException {
        if ( notDefined(filename) ) {
            throw new IOException("Unable to set filename: no filename specified");
        }
        this.propertyFilename = filename;
    }

    public void load(PageContext pageContext, String name) throws IOException {
        setRootDirectory(pageContext);
        this.propertyFilename = name;
        loadFile(getRootDirectory() + (name.indexOf(".") >= 0 ? name 
                                                              : new StringBuilder().append(name)
                                                                                   .append(getConfigurationExtension())
                                                                                   .toString()));
    }

    public void load(ServletContext application, String name) throws IOException {
        setRootDirectory(application);
        this.propertyFilename = name;
        try {
        loadFile(getRootDirectory() + (name.indexOf(".") >= 0 ? name 
                                                              : new StringBuilder().append(name)
                                                                                   .append(getConfigurationExtension())
                                                                                   .toString()));
        } catch (Exception e) {
            throw new IOException(String.format("Failed to load file: (%s)",
            getRootDirectory() + (name.indexOf(".") >= 0 ? name 
                                                                          : new StringBuilder().append(name)
                                                                                               .append(getConfigurationExtension())
                                                                                               .toString())
                                              )
                );
        }
    }

    public void store() throws IOException {
        if ((this.propertyFilename == null) || (this.propertyFilename.length() == 0)) {
            throw new IOException("Unable to store file: no filename defined");
        }
        storeFile(getRootDirectory() +
                  (this.propertyFilename.indexOf(".") >= 0 ? this.propertyFilename 
                                                           : new StringBuilder().append(this.propertyFilename)
                                                                                .append(getConfigurationExtension())
                                                                                .toString()));
    }

    public void store(String name) throws IOException {
        this.propertyFilename = name;
        storeFile(getRootDirectory() + (name.indexOf(".") >= 0 ? name : new StringBuilder().append(name)
                                                                                           .append(getConfigurationExtension())
                                                                                           .toString()));
    }

    public void load(InputStream inStream) throws IOException {
        super.load(inStream);
        updateFieldValues();
    }

    public void load(Reader reader) throws IOException {
        super.load(reader);
        updateFieldValues();
    }

    public void loadFromXML(InputStream inStream) throws IOException {
        super.loadFromXML(inStream);
        updateFieldValues();
    }

    protected String baseDirectory = null;

    public void updateFieldValues() {}

    public String getBaseDirectory() throws IOException {
        if (this.baseDirectory == null) {
            throw new IOException("Base directory is undefined");
        }
        return this.baseDirectory;
    }

    public void verifyFilePath(String path) {
        if (path == null) {
            return;
        }
        path = path.replaceAll("\\\\", "/");
        if ((path.indexOf("/") > 0) && (!path.endsWith("/"))) {
            path = path.substring(0, path.lastIndexOf("/"));
        }
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void setDefaults(Properties newDefaults) {
        this.defaults = newDefaults;
    }

    public void removeDefaults() {
        this.defaults = null;
    }

    public Properties getDefaults() {
        return (Properties) (this.defaults == null ? null : this.defaults.clone());
    }

    protected String filepath = null;

    public String getFilepath() {
        return this.filepath;
    }

    protected void loadFile(String fullFilepath) throws IOException {
        InputStream in = null;
        try {
            this.loadError = null;
            if ((fullFilepath != null) && (fullFilepath.startsWith("file:/"))) {
                fullFilepath = fullFilepath.substring(5);
            }
            if ((fullFilepath == null) || (fullFilepath.length() == 0)) {
                throw new IOException("Unable to load file: No name specified");
            }
            File fileToLoad = new File(fullFilepath);
            if (!fileToLoad.exists()) {
                throw new IOException("Unable to load file: File does not exist");
            }
            load(in = new FileInputStream(fileToLoad));
            this.filepath = fileToLoad.getAbsolutePath();
        } catch (IOException e) {
            this.loadError = ("Failed to load file (" + fullFilepath + ")\n" + e.toString());
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
                in = null;
            }
        }
    }

    protected void storeFile(String fullFilepath) throws IOException {
        OutputStream out = null;
        try {
            this.saveError = null;
            if ((fullFilepath != null) && (fullFilepath.startsWith("file:/"))) {
                fullFilepath = fullFilepath.substring(5);
            }
            if ((fullFilepath == null) || (fullFilepath.length() == 0)) {
                throw new IOException("Unable to store file: No name specified");
            }
            if ((this.baseDirectory == null) || (!fullFilepath.startsWith(this.baseDirectory))) {
                throw new IOException("Unable to store file: Base directory is undefined");
            }
            File fileToStore = new File(fullFilepath);

            super.store(out = new FileOutputStream(fileToStore), null);
        } catch (IOException e) {
            this.saveError = ("Failed to save file (" + fullFilepath + ")\n" + e.toString());
            throw e;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {}
                out = null;
            }
        }
    }

    protected String loadError = null;

    public String getLoadError() {
        return this.loadError;
    }

    protected String saveError = null;

    public String getSaveError() {
        return this.saveError;
    }

    public String getString(String property) {
        return getString(property, "");
    }

    public String getString(String property, String def) {
        String value = getProperty(property);
        return value == null ? def : value;
    }

    public int getInt(String property) {
        return getInt(property, 0);
    }

    public int getInt(String property, int def) {
        try {
            return Integer.parseInt(getProperty(property));
        } catch (Exception e) {}
        return def;
    }

    public long getLong(String property) {
        return getLong(property, 0L);
    }

    public long getLong(String property, long def) {
        try {
            return Long.parseLong(getProperty(property));
        } catch (Exception e) {}
        return def;
    }

    public double getDouble(String property) {
        return getDouble(property, 0.0D);
    }

    public double getDouble(String property, double def) {
        try {
            return Double.parseDouble(getProperty(property));
        } catch (Exception e) {}
        return def;
    }

    public boolean getBoolean(String property, boolean def) {
        if ( notDefined(property) ) return def;

        return isTrue(property);
    }

    public boolean getBoolean(String property) {
        return isTrue(property);
    }

    public boolean isTrue(String property) {
        return ("true".equalsIgnoreCase(getProperty(property))) || ("Y".equalsIgnoreCase(getProperty(property)));
    }

    public boolean isFalse(String property) {
        return !isTrue(getProperty(property));
    }

    public boolean notDefined(String val) {
        return (val == null) || (val.length() == 0);
    }

    public String getNvl(String property1, String property2) {
        return getProperty(property1) == null ? getProperty(property2) : getProperty(property1);
    }

    public String[] getKeys() {
        String[] keys = (String[]) keySet().toArray(new String[size()]);
        Arrays.sort(keys);
        return keys;
    }

    protected static SimpleDateFormat externalDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String datetimeToExternal(long datetime) {
        return datetimeToExternal(new Date(datetime));
    }

    public static String datetimeToExternal(Date datetime) {
        if (datetime == null) {
            return "";
        }
        return externalDatetimeFormat.format(datetime);
    }

    public long decodeDatetime(String datetime) throws Exception {
        return getDatetime(datetime).getTime();
    }

    public static Date getDatetime(String datetime) throws Exception {
        if (datetime == null) {
            return null;
        }
        if (datetime.matches("[0-9]{1,}")) {
            return new Date(Long.parseLong(datetime));
        }
        for (int formatIdx = 0; formatIdx < datetimeFormats.length; formatIdx++) {
            if (datetime.matches(datetimeFormats[formatIdx][1])) {
                return new SimpleDateFormat(datetimeFormats[formatIdx][0]).parse(datetime);
            }
        }
        throw new Exception("Invalid format");
    }

    protected static String[][] datetimeFormats = {
        { "y-M-d h:m a", "(20){0,1}[12][0-9]-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "y-M-d H:m", "(20){0,1}[12][0-9]-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}" },
        { "y-M-d h:m:s a", "(20){0,1}[12][0-9]-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "y-M-d H:m:s", "(20){0,1}[12][0-9]-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}" },
        { "M-d-y", "[01]{1}[0-9]{1}-[0123]{1}[0-9]{1}-(20){1}[12][0-9]" },
        { "M-d-y", "[01]{1}[0-9]{1}[0123]{1}[0-9]{1}(20){1}[12][0-9]" },
        { "y-M-d", "(20){1}[12][0-9]-[0-9]{1,2}-[0-9]{1,2}" },
        { "M/d/y h:m a", "[0-9]{1,2}/[0-9]{1,2}/(20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "M/d/y H:m", "[0-9]{1,2}/[0-9]{1,2}/(20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}" },
        { "M/d/y h:m:s a", "[0-9]{1,2}/[0-9]{1,2}/(20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "M/d/y H:m:s", "[0-9]{1,2}/[0-9]{1,2}/(20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}" },
        { "M/d/y", "[0-9]{1,2}/[0-9]{1,2}/(20){0,1}[12][0-9]" },
        { "y-MMM-d h:m a", "(20){0,1}[12][0-9]-[A-Za-z]{3,}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "y-MMM-d H:m", "(20){0,1}[12][0-9]-[A-Za-z]{3,}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}" },
        { "y-MMM-d h:m:s a", "(20){0,1}[12][0-9]-[A-Za-z]{3,}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "y-MMM-d H:m:s", "(20){0,1}[12][0-9]-[A-Za-z]{3,}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}" },
        { "y-MMM-d", "(20){0,1}[12][0-9]-[A-Za-z]{3,}-[0-9]{1,2}" },
        { "MMM d, y h:m a", "[A-Za-z]{3,} [0-9]{1,2}, (20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "MMM d, y H:m", "[A-Za-z]{3,} [0-9]{1,2}, (20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}" },
        { "MMM d, y h:m:s a",
          "[A-Za-z]{3,} [0-9]{1,2}, (20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2} [APap][mM]" },
        { "MMM d, y H:m:s", "[A-Za-z]{3,} [0-9]{1,2}, (20){0,1}[12][0-9] [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}" },
        { "MMM d, y", "[A-Za-z]{3,} [0-9]{1,2}, (20){0,1}[12][0-9]" },
        { "E MMM d H:m:s z y",
          "[A-Za-z]{3,} [A-Za-z]{3,} [0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2} [A-Z]{3} (20){0,1}[12][0-9]" }
    };

    public Configuration setProperty(String key, String value) {
        super.setProperty(key,value);
        return this;
    }
    public Configuration setProperty(String key, long value) {
        this.setProperty(key,""+value);
        return this;
    }
    public Configuration setProperty(String key, double value) {
        super.setProperty(key,""+value);
        return this;
    }
    public Configuration setProperty(String key, boolean value) {
        super.setProperty(key,(value ? "true" : "false"));
        return this;
    }

    public String[] listAvailableConfigurations() {
        ArrayList<String> files = new ArrayList<String>();
        try {
            Files.list(Paths.get(String.format("%s%s",getBaseDirectory(),getConfigurationDirectory())))
            .filter(Files::isRegularFile)
            .forEach(filePath -> {  String name = filePath.getFileName().toString();
                                    if ( ! name.endsWith(getConfigurationExtension()) ) return;
                                    files.add(name.replaceAll(getConfigurationExtension(),""));
                                }
                    );
        } catch (Exception e) {
        }

        return files.toArray(new String[0]);
    }
}
