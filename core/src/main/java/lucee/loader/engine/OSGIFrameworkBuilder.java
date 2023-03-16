package lucee.loader.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import lucee.loader.util.Util;
import lucee.loader.osgi.LoggerImpl;

public class OSGIFrameworkBuilder {
	private LoggerImpl logger;
	private Map<String, Object> config;
    private File cacheDirectory;

    public OSGIFrameworkBuilder(){
        this.config = new HashMap<String, Object>();
    }

    /**
     * Add the given OSGI configuration.
     * 
     * @param config
     * @return
     */
    public OSGIFrameworkBuilder withConfig( Map<String, Object> config ){
        this.config.putAll( config );
        return this;
    }

    /**
     * Set the file location for the felix cache by reading from the <code>felix.cache.rootdir</code> config setting
     * 
     * @param cacheDirectory
     * @return
     */
    public OSGIFrameworkBuilder withCacheDirectory( File cacheDirectory ){
        this.cacheDirectory = cacheDirectory;
        return this;
    }

    /**
     * Set the logger that the OSGI framework should use.
     * 
     * @param logger Instance of OSGI {@see org.apache.felix.framework.Logger} class.
     */
    public OSGIFrameworkBuilder withLogger( LoggerImpl logger ){
        this.logger = logger;
		return this;
    }

    /**
	 * Build the Felix framework for managing OSGI bundles.
	 * 
	 * @return A started OSGI framework - i.e. Felix.
	 * @throws BundleException
	 */
	public Felix build() throws BundleException {
        Boolean isNewCacheDirectory = false;
		if (Util.isEmpty((String) config.get("felix.cache.rootdir"))) {
			if (!cacheDirectory.exists()) {
				cacheDirectory.mkdirs();
				isNewCacheDirectory = true;
			}
			if (cacheDirectory.isDirectory()) {
                config.put("felix.cache.rootdir", cacheDirectory.getAbsolutePath());
            }
		}

        int logLevel = getLogLevel();
		if (logger != null) {
			if (logLevel == 2) logger.setLogLevel(Logger.LOG_WARNING);
			else if (logLevel == 3) logger.setLogLevel(Logger.LOG_INFO);
			else if (logLevel == 4) logger.setLogLevel(Logger.LOG_DEBUG);
			else logger.setLogLevel(Logger.LOG_ERROR);
		}

		if (logger != null) {
			if (logLevel == 2) logger.setLogLevel(Logger.LOG_WARNING);
			else if (logLevel == 3) logger.setLogLevel(Logger.LOG_INFO);
			else if (logLevel == 4) logger.setLogLevel(Logger.LOG_DEBUG);
			else logger.setLogLevel(Logger.LOG_ERROR);
		}
		if (logger != null) config.put("felix.log.logger", logger);

		config.put("felix.log.level", "" + logLevel );

		// Allow felix.cache.locking to be overridden by env var (true/false)
		// Enables or disables bundle cache locking, which is used to prevent concurrent access to the
		// bundle cache.

		extend("felix.cache.locking", null, false);
		extend("org.osgi.framework.executionenvironment", null, false);
		extend("org.osgi.framework.storage", null, false);
		extend("org.osgi.framework.storage.clean", Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT, false);
		extend(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK, false);
		extend(Constants.FRAMEWORK_BOOTDELEGATION, null, true);
		extend(Constants.FRAMEWORK_SYSTEMPACKAGES, null, true);
		extend(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, null, true);
		extend("felix.cache.filelimit", null, false);
		extend("felix.cache.bufsize", null, false);
		extend("felix.bootdelegation.implicit", null, false);
		extend("felix.systembundle.activators", null, false);
		extend("org.osgi.framework.startlevel.beginning", null, false);
		extend("felix.startlevel.bundle", null, false);
		extend("felix.service.urlhandlers", null, false);
		extend("felix.auto.deploy.dir", null, false);
		extend("felix.auto.deploy.action", null, false);
		extend("felix.shutdown.hook", null, false);

		// remove any empty record, this can produce trouble
        Map<String, Object> finalConfig = config.entrySet().stream()
            .filter( item -> item.getValue() != null )
            .filter( item -> !item.getValue().toString().isEmpty() )
            .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));

        if (logger != null) logger.log(Logger.LOG_INFO, "Loading felix with config:" + finalConfig.toString());

		Felix felix = new Felix(finalConfig);
		try {
			felix.start();
		}
		catch (BundleException be) {
			// this could be cause by an invalid felix cache, so we simply delete it and try again
			if (!isNewCacheDirectory && "Error creating bundle cache.".equals(be.getMessage())) {
				Util.deleteContent(this.cacheDirectory, null);

			}

		}

		return felix;
	}

    /**
     * Extend the OSGI config with this additional parameter
     * @param name System property or env var to check
     * @param defaultValue Value to use if not found in the system properties / env vars
     * @param add Can't figure out what this does. Boolean
     */
	private void extend(String name, String defaultValue, boolean add) {
		String addional = CFMLEngineFactory.getSystemPropOrEnvVar(name, null);
		if (Util.isEmpty(addional, true)) {
			if (Util.isEmpty(defaultValue, true)) return;
			addional = defaultValue.trim();
		}
		if (add) {
			String existing = (String) config.get(name);
			if (!Util.isEmpty(existing, true)) config.put(name, existing.trim() + "," + addional.trim());
			else config.put(name, addional.trim());
		}
		else {
			config.put(name, addional.trim());
		}
	}

    /**
     * Read the log level from the <code>felix.log.level</code> System property or environment variable and parse it to an integer.
     * 
     * @return Integer-based OSGI log level, where 1 = error, 2 = warning, 3 = information, and 4 = debug
     */
    private int getLogLevel(){
		int logLevel = 1;
		String strLogLevel = CFMLEngineFactory.getSystemPropOrEnvVar("felix.log.level", null);
		if (Util.isEmpty(strLogLevel)) strLogLevel = (String) config.get("felix.log.level");

		if (!Util.isEmpty(strLogLevel)) {
			if ("0".equalsIgnoreCase(strLogLevel)) logLevel = 0;
			else if ("error".equalsIgnoreCase(strLogLevel) || "1".equalsIgnoreCase(strLogLevel)) logLevel = 1;
			else if ("warning".equalsIgnoreCase(strLogLevel) || "2".equalsIgnoreCase(strLogLevel)) logLevel = 2;
			else if ("info".equalsIgnoreCase(strLogLevel) || "information".equalsIgnoreCase(strLogLevel) || "3".equalsIgnoreCase(strLogLevel)) logLevel = 3;
			else if ("debug".equalsIgnoreCase(strLogLevel) || "4".equalsIgnoreCase(strLogLevel)) logLevel = 4;
		}
        return logLevel;
    }
}